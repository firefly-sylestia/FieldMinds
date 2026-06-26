You are an expert Android/Kotlin engineer. Build a single APK that contains both Controller and Target modes in one app. When launched, the user picks their mode from a simple home screen. This is Android-to-Android mirroring over WiFi with minimal latency and proper guided setup.

PROJECT GOAL
Single app module. One APK. Install on two Android devices on same WiFi. One device becomes Target (controlled), one becomes Controller (controls). Simple UI with step-by-step guides for setup and connection.

TECH STACK — MANDATORY

Language: Kotlin only (no Java)
Min SDK: 26 (Android 8.0), Target SDK: 34
UI: Plain XML layouts + ViewBinding — NO Jetpack Compose
Networking: Raw java.net.Socket only — NO Retrofit, OkHttp, Ktor, WebSocket libraries
Video codec: MediaCodec H.264 hardware encoder/decoder
Screen capture: MediaProjection API
Input injection: AccessibilityService (no root required)
Async: Kotlin Coroutines with Dispatchers.IO
Build: Gradle with Kotlin DSL (build.gradle.kts)


ARCHITECTURE OVERVIEW
[Controller Device]                    [Target Device]
┌────────────────────┐                ┌────────────────────┐
│  ControllerActivity│                │  TargetActivity    │
│  MirrorSurfaceView │  ◄──H.264───   │  ScreenCapture     │
│  (display + touch) │                │  Service           │
│                    │                │                    │
│  InputSenderSvc    │                │  InputInjector     │
│  (send tap/key)    │   ─touch/key─► │  + AccessService   │
│                    │                │                    │
│  StreamReceiverSvc │  ◄─TCP:9001─   │  ScreenCaptureSvc  │
│  (decode video)    │                │  (H.264 stream)    │
└────────────────────┘                │                    │
                                      │  InputReceiverSvc  │
                                      │  ◄─TCP:9002────   │
                                      │  (recv touch/key)  │
                                      │                    │
                                      │  LatencyPingSvc    │
                                      │  ◄─TCP:9003────   │
                                      │  (measure latency) │
                                      └────────────────────┘

UI FLOW
Screen 1: Home Screen (HomeActivity.kt)
Layout: activity_home.xml
┌─────────────────────────────┐
│                             │
│      📱 AndroidMirror       │
│       v1.0                  │
│                             │
│  ┌─────────────────────┐   │
│  │  🎯 BE CONTROLLED   │   │
│  │  (This is target)   │   │
│  │  Open me on the     │   │
│  │  device you want to │   │
│  │  control            │   │
│  └─────────────────────┘   │
│                             │
│  ┌─────────────────────┐   │
│  │  🕹️ TAKE CONTROL    │   │
│  │  (This is controller)   │
│  │  Use this to mirror     │
│  │  and control another    │
│  │  device                 │
│  └─────────────────────┘   │
│                             │
│  © 2026 AndroidMirror      │
│                             │
└─────────────────────────────┘
Implementation:

Two large buttons: btnBeControlled and btnTakeControl
btnBeControlled.setOnClickListener { startActivity(Intent(..., TargetActivity::class.java)) }
btnTakeControl.setOnClickListener { startActivity(Intent(..., ControllerActivity::class.java)) }
No animations, minimal layout — just buttons and text


Screen 2: Target Mode (TargetActivity.kt)
Layout: activity_target.xml
┌─────────────────────────────┐
│  ← [Back]  🎯 TARGET MODE  │
│                             │
│  ┌─────────────────────┐   │
│  │  YOUR IP ADDRESS    │   │
│  │  ┌───────────────┐  │   │
│  │  │ 192.168.1.45  │  │   │  ← tvIpAddress (tap to copy)
│  │  └───────────────┘  │   │
│  └─────────────────────┘   │
│                             │
│  PORT: 9001 (video)        │
│  PORT: 9002 (input)        │
│                             │
│  ┌────── HOW TO ──────┐    │
│  │ CONNECT:           │    │
│  │                    │    │
│  │ 1. Install this    │    │
│  │    app on the      │    │
│  │    controller      │    │
│  │    device too      │    │
│  │                    │    │
│  │ 2. Make sure both  │    │
│  │    devices are on  │    │
│  │    SAME WiFi       │    │
│  │                    │    │
│  │ 3. On controller,  │    │
│  │    tap "Take       │    │
│  │    Control"        │    │
│  │                    │    │
│  │ 4. Enter the IP    │    │
│  │    shown above     │    │
│  │                    │    │
│  │ 5. Tap START below │    │
│  │                    │    │
│  └────────────────────┘    │
│                             │
│  ┌─────────────────────┐   │
│  │  ▶ START HOSTING    │   │  ← btnStartHosting
│  └─────────────────────┘   │
│                             │
│  Status: ● Waiting...       │  ← tvStatus (updates live)
│  Sessions: 0 connected      │  ← tvSessions
│  FPS: --                    │  ← tvFps
│                             │
│  ┌─────────────────────┐   │
│  │  ▢ STOP HOSTING     │   │  ← btnStopHosting (hidden until started)
│  └─────────────────────┘   │
│                             │
└─────────────────────────────┘
Implementation details:
kotlinclass TargetActivity : AppCompatActivity() {
    
    private lateinit var tvIpAddress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnStartHosting: Button
    private lateinit var btnStopHosting: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)
        
        // Get local IP
        val ip = getLocalIpAddress() // utility function
        tvIpAddress.text = ip
        
        // Copy to clipboard on tap
        tvIpAddress.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("IP", ip))
            Toast.makeText(this, "IP copied!", Toast.LENGTH_SHORT).show()
        }
        
        // Start button
        btnStartHosting.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                showAccessibilityPermissionDialog()
            } else {
                startMediaProjectionFlow()
            }
        }
        
        // Stop button
        btnStopHosting.setOnClickListener {
            stopAllServices()
            btnStartHosting.visibility = View.VISIBLE
            btnStopHosting.visibility = View.GONE
        }
        
        // Back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        stopAllServices()
        finish()
        return true
    }
    
    private fun startMediaProjectionFlow() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_CODE_MEDIA_PROJECTION
        )
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MEDIA_PROJECTION && resultCode == RESULT_OK) {
            // User granted permission
            startService(Intent(this, ScreenCaptureService::class.java).apply {
                putExtra("resultCode", resultCode)
                putExtra("data", data)
            })
            startService(Intent(this, InputReceiverService::class.java))
            startService(Intent(this, LatencyPingService::class.java))
            
            btnStartHosting.visibility = View.GONE
            btnStopHosting.visibility = View.VISIBLE
            updateStatus("● Waiting for controller...")
        }
    }
    
    private fun showAccessibilityPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Accessibility Permission Required")
            .setMessage("To inject touch input from the controller device, you must enable this app in Accessibility Settings.\n\nSteps:\n1. Tap OPEN SETTINGS\n2. Scroll down, tap AndroidMirror\n3. Toggle ON\n4. Come back and try again")
            .setPositiveButton("OPEN SETTINGS") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        return enabledServices.contains("com.mirror.app/com.mirror.app.ControlAccessibilityService")
    }
    
    private fun stopAllServices() {
        stopService(Intent(this, ScreenCaptureService::class.java))
        stopService(Intent(this, InputReceiverService::class.java))
        stopService(Intent(this, LatencyPingService::class.java))
    }
    
    private fun updateStatus(status: String) {
        runOnUiThread { tvStatus.text = status }
    }
}

// Utility
private fun getLocalIpAddress(): String {
    return try {
        NetworkInterface.getNetworkInterfaces().asSequence()
            .flatMap { it.inetAddresses.asSequence() }
            .first { !it.isLoopbackAddress && it is Inet4Address }
            .hostAddress
    } catch (e: Exception) {
        "0.0.0.0"
    }
}

Screen 3: Controller Mode (ControllerActivity.kt)
Layout: activity_controller.xml
Before connect:
┌─────────────────────────────┐
│  ← [Back]  🕹️ CONTROLLER   │
│                             │
│  ┌─────────────────────┐   │
│  │  TARGET IP ADDRESS  │   │
│  │  ┌───────────────┐  │   │
│  │  │ 192.168.1._   │  │   │  ← etTargetIp (numeric keyboard)
│  │  └───────────────┘  │   │
│  └─────────────────────┘   │
│                             │
│  ┌────── HOW TO ──────┐    │
│  │ CONNECT:           │    │
│  │                    │    │
│  │ 1. On the target   │    │
│  │    device, open    │    │
│  │    this app        │    │
│  │                    │    │
│  │ 2. Tap "Be         │    │
│  │    Controlled"     │    │
│  │                    │    │
│  │ 3. Note the IP     │    │
│  │    shown there     │    │
│  │                    │    │
│  │ 4. Enter IP above  │    │
│  │                    │    │
│  │ 5. Tap CONNECT     │    │
│  │                    │    │
│  │ 6. BOTH devices    │    │
│  │    must be on      │    │
│  │    SAME WiFi       │    │
│  │                    │    │
│  └────────────────────┘    │
│                             │
│  ┌─────────────────────┐   │
│  │  🔗 CONNECT         │   │  ← btnConnect
│  └─────────────────────┘   │
│                             │
│  Status: ○ Not connected    │  ← tvStatus
│  Latency: -- ms             │  ← tvLatency
│                             │
└─────────────────────────────┘
After connect (full screen):
┌─────────────────────────────┐
│ FPS: 58  Lat: 45ms  ✕      │  ← status bar (40dp)
│ ┌───────────────────────────┐│
│ │                           ││
│ │   MIRROR SURFACE VIEW     ││  ← MirrorSurfaceView
│ │   (fills rest of screen)  ││
│ │                           ││
│ │   Touch here to control   ││
│ │   target device           ││
│ │                           ││
│ └───────────────────────────┘│
└─────────────────────────────┘
Implementation:
kotlinclass ControllerActivity : AppCompatActivity() {
    
    private lateinit var etTargetIp: EditText
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvLatency: TextView
    private lateinit var mirrorView: MirrorSurfaceView
    private lateinit var statusBar: FrameLayout
    
    private var inputSenderService: InputSenderService? = null
    private var streamReceiverService: StreamReceiverService? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controller)
        
        etTargetIp = findViewById(R.id.etTargetIp)
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvLatency = findViewById(R.id.tvLatency)
        mirrorView = findViewById(R.id.mirrorView)
        statusBar = findViewById(R.id.statusBar)
        
        // Load last IP from SharedPreferences
        val prefs = getSharedPreferences("mirror_prefs", MODE_PRIVATE)
        etTargetIp.setText(prefs.getString("last_ip", ""))
        
        btnConnect.setOnClickListener {
            val ip = etTargetIp.text.toString().trim()
            if (ip.isEmpty()) {
                Toast.makeText(this, "Enter target IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Save IP
            prefs.edit().putString("last_ip", ip).apply()
            
            // Start connection
            tvStatus.text = "● Connecting..."
            connect(ip)
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun connect(ip: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Start InputSenderService
                inputSenderService = InputSenderService(ip, 9002)
                inputSenderService!!.start()
                
                // Start StreamReceiverService
                streamReceiverService = StreamReceiverService(ip, 9001, mirrorView)
                streamReceiverService!!.start()
                
                // On successful connect, switch to full-screen mode
                runOnUiThread {
                    hideConnectUI()
                    tvStatus.text = "● Connected"
                }
                
            } catch (e: Exception) {
                runOnUiThread {
                    tvStatus.text = "○ Error: ${e.message}"
                    Toast.makeText(this@ControllerActivity, "Connect failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun hideConnectUI() {
        etTargetIp.visibility = View.GONE
        btnConnect.visibility = View.GONE
        tvStatus.visibility = View.GONE
        tvLatency.visibility = View.VISIBLE
        mirrorView.visibility = View.VISIBLE
        statusBar.visibility = View.VISIBLE
    }
    
    private fun showConnectUI() {
        etTargetIp.visibility = View.VISIBLE
        btnConnect.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvLatency.visibility = View.GONE
        mirrorView.visibility = View.GONE
        statusBar.visibility = View.GONE
    }
    
    override fun onSupportNavigateUp(): Boolean {
        disconnect()
        finish()
        return true
    }
    
    private fun disconnect() {
        inputSenderService?.stop()
        streamReceiverService?.stop()
        showConnectUI()
    }
}

SERVICES — DETAILED IMPLEMENTATION
SERVICE 1: ScreenCaptureService.kt (Target device)
Purpose: Capture device screen at 60fps, encode H.264, stream over TCP:9001
kotlinclass ScreenCaptureService : Service() {
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var encoder: MediaCodec? = null
    private var encoderThread: Thread? = null
    private var socketServer: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isRunning = false
    
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val resultCode = intent.getIntExtra("resultCode", 0)
        val data = intent.getParcelableExtra<Intent>("data")!!
        
        startForeground(1, createNotification("Hosting screen..."))
        
        scope.launch {
            startScreenCapture(resultCode, data)
            startInputServer()
        }
        
        return START_STICKY
    }
    
    private suspend fun startScreenCapture(resultCode: Int, data: Intent) {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        
        val screenWidth = 720
        val screenHeight = 1280
        
        // Start socket server for video
        socketServer = ServerSocket(9001)
        scope.launch {
            try {
                clientSocket = socketServer!!.accept() // blocking
                clientSocket!!.tcpNoDelay = true
                clientSocket!!.setPerformancePreferences(0, 1, 0)
                
                // Send handshake: width, height, version
                val output = DataOutputStream(clientSocket!!.outputStream)
                output.writeInt(screenWidth)
                output.writeInt(screenHeight)
                output.writeInt(1) // protocol version
                output.flush()
                
                startVideoEncoding(screenWidth, screenHeight)
            } catch (e: Exception) {
                Log.e("ScreenCapture", "Socket error", e)
            }
        }
    }
    
    private fun startVideoEncoding(width: Int, height: Int) {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 4_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 60)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            setInteger(MediaFormat.KEY_LATENCY, 0)
            setInteger(MediaFormat.KEY_PRIORITY, 0)
            setInteger("repeat-previous-frame-after", 100_000) // microseconds
            if (Build.VERSION.SDK_INT >= 30) {
                setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
        }
        
        encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        encoder!!.setCallback(EncoderCallback())
        encoder!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        
        val surface = encoder!!.createInputSurface()
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            width, height, 1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface, null, null
        )
        
        encoder!!.start()
        
        // Boost thread priority
        encoderThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
        }
    }
    
    private inner class EncoderCallback : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {}
        
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            val buffer = codec.getOutputBuffer(index) ?: return
            
            if (info.size > 0) {
                val data = ByteArray(info.size)
                buffer.get(data)
                
                // Send over socket
                try {
                    if (clientSocket?.isConnected == true) {
                        val output = DataOutputStream(clientSocket!!.outputStream)
                        output.writeInt(data.size) // 4-byte frame size
                        output.write(data)
                        output.flush()
                    }
                } catch (e: Exception) {
                    Log.e("ScreenCapture", "Send error", e)
                }
            }
            
            codec.releaseOutputBuffer(index, false)
        }
        
        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Log.e("ScreenCapture", "Encoder error", e)
        }
        
        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {}
    }
    
    private fun startInputServer() {
        // Delegated to InputReceiverService
    }
    
    override fun onDestroy() {
        isRunning = false
        encoder?.stop()
        encoder?.release()
        virtualDisplay?.release()
        mediaProjection?.stop()
        clientSocket?.close()
        socketServer?.close()
        scope.cancel()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent) = null
    
    private fun createNotification(text: String) =
        NotificationCompat.Builder(this, "mirror_channel")
            .setContentTitle("AndroidMirror")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
}

SERVICE 2: InputReceiverService.kt (Target device)
Purpose: Listen on TCP:9002 for touch/key input, inject via AccessibilityService
kotlinclass InputReceiverService : Service() {
    
    private var socketServer: ServerSocket? = null
    private var clientSocket: Socket? = null
    private lateinit var inputInjector: InputInjector
    
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        inputInjector = InputInjector(this)
        
        scope.launch {
            socketServer = ServerSocket(9002)
            try {
                clientSocket = socketServer!!.accept()
                clientSocket!!.tcpNoDelay = true
                
                handleInputStream()
            } catch (e: Exception) {
                Log.e("InputReceiver", "Error", e)
            }
        }
        
        return START_STICKY
    }
    
    private suspend fun handleInputStream() {
        val input = DataInputStream(clientSocket!!.inputStream)
        
        while (isActive) {
            try {
                val type = input.readByte().toInt()
                
                when (type) {
                    0x01 -> { // touch event
                        val x = input.readFloat()
                        val y = input.readFloat()
                        val pressure = input.readFloat()
                        val action = input.readInt()
                        
                        inputInjector.injectTouchEvent(x, y, pressure, action)
                    }
                    0x02 -> { // key event
                        val keyCode = input.readInt()
                        inputInjector.injectKeyEvent(keyCode)
                    }
                    0x03 -> { // scroll
                        val dx = input.readFloat()
                        val dy = input.readFloat()
                        inputInjector.injectScroll(dx, dy)
                    }
                }
            } catch (e: EOFException) {
                break
            }
        }
    }
    
    override fun onDestroy() {
        clientSocket?.close()
        socketServer?.close()
        scope.cancel()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent) = null
}

SERVICE 3: InputInjector.kt (Target device)
Purpose: Use AccessibilityService to inject touch/key events
kotlinclass InputInjector(private val context: Context) {
    
    fun injectTouchEvent(x: Float, y: Float, pressure: Float, action: Int) {
        val service = ControlAccessibilityService.instance ?: return
        
        val downTime = SystemClock.uptimeMillis()
        
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val path = GestureDescription.StrokeDescription(
                    Path().apply { moveTo(x, y) },
                    0,
                    50 // 50ms touch
                )
                val gesture = GestureDescription.Builder()
                    .addStroke(path)
                    .build()
                service.dispatchGesture(gesture, null, null)
            }
            MotionEvent.ACTION_MOVE -> {
                // Handled in drag/swipe
            }
            MotionEvent.ACTION_UP -> {
                // End of gesture
            }
        }
    }
    
    fun injectKeyEvent(keyCode: Int) {
        val service = ControlAccessibilityService.instance ?: return
        
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            KeyEvent.KEYCODE_HOME -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            KeyEvent.KEYCODE_APP_SWITCH -> service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }
    }
    
    fun injectScroll(dx: Float, dy: Float) {
        val service = ControlAccessibilityService.instance ?: return
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_CUSTOM)
    }
}

SERVICE 4: ControlAccessibilityService.kt (Target device)
Purpose: System accessibility service for input injection
kotlinclass ControlAccessibilityService : AccessibilityService() {
    
    companion object {
        var instance: ControlAccessibilityService? = null
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    
    override fun onInterrupt() {}
    
    override fun onServiceConnected() {
        instance = this
    }
    
    override fun onDestroy() {
        instance = null
    }
}
AndroidManifest entry:
xml<service
    android:name=".ControlAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
File: res/xml/accessibility_service_config.xml:
xml<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_desc"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFlags="flagDefault"
    android:canPerformGestures="true"
    android:canRequestEnhancedWebAccessibility="false"
    android:notificationTimeout="100" />

SERVICE 5: StreamReceiverService.kt (Controller device)
Purpose: Connect to target, receive H.264 stream, decode, render on MirrorSurfaceView
kotlinclass StreamReceiverService(
    private val targetIp: String,
    private val port: Int,
    private val mirrorView: MirrorSurfaceView
) {
    
    private var socket: Socket? = null
    private var decoder: MediaCodec? = null
    private var decoderThread: Thread? = null
    private var isRunning = false
    
    fun start() {
        isRunning = true
        
        Thread {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
                
                socket = Socket(targetIp, port)
                socket!!.tcpNoDelay = true
                socket!!.setPerformancePreferences(0, 1, 0)
                socket!!.receiveBufferSize = 65536
                
                // Read handshake
                val input = DataInputStream(socket!!.inputStream)
                val screenWidth = input.readInt()
                val screenHeight = input.readInt()
                val protocolVersion = input.readInt()
                
                mirrorView.setTargetResolution(screenWidth, screenHeight)
                
                startVideoDecoding(screenWidth, screenHeight, input)
                
            } catch (e: Exception) {
                Log.e("StreamReceiver", "Error", e)
            }
        }.start()
    }
    
    private fun startVideoDecoding(width: Int, height: Int, input: DataInputStream) {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            if (Build.VERSION.SDK_INT >= 30) {
                setInteger(MediaFormat.KEY_LOW_LATENCY, 1)
            }
        }
        
        decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        decoder!!.setCallback(DecoderCallback(mirrorView))
        decoder!!.configure(format, mirrorView.getSurface(), null, 0)
        decoder!!.start()
        
        // Read and feed NAL units
        decoderThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            
            while (isRunning) {
                try {
                    val size = input.readInt()
                    val nal = ByteArray(size)
                    input.readFully(nal)
                    
                    val index = decoder!!.dequeueInputBuffer(5000)
                    if (index >= 0) {
                        val buffer = decoder!!.getInputBuffer(index)!!
                        buffer.put(nal)
                        decoder!!.queueInputBuffer(index, 0, nal.size, 0, 0)
                    }
                } catch (e: Exception) {
                    Log.e("StreamReceiver", "Decode error", e)
                }
            }
        }
        decoderThread!!.start()
    }
    
    private inner class DecoderCallback(private val view: MirrorSurfaceView) : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {}
        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            codec.releaseOutputBuffer(index, true)
        }
        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            Log.e("Decoder", "Error", e)
        }
        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {}
    }
    
    fun stop() {
        isRunning = false
        decoder?.stop()
        decoder?.release()
        socket?.close()
        decoderThread?.join(1000)
    }
}

SERVICE 6: InputSenderService.kt (Controller device)
Purpose: Connect to target port 9002, send touch/key events from MirrorSurfaceView
kotlinclass InputSenderService(
    private val targetIp: String,
    private val port: Int
) {
    
    private var socket: Socket? = null
    private var output: DataOutputStream? = null
    private var isRunning = false
    
    fun start() {
        isRunning = true
        
        Thread {
            try {
                socket = Socket(targetIp, port)
                socket!!.tcpNoDelay = true
                output = DataOutputStream(socket!!.outputStream)
            } catch (e: Exception) {
                Log.e("InputSender", "Connect error", e)
            }
        }.start()
    }
    
    fun sendTouchEvent(x: Float, y: Float, pressure: Float, action: Int) {
        try {
            synchronized(output!!) {
                output!!.writeByte(0x01) // touch type
                output!!.writeFloat(x)
                output!!.writeFloat(y)
                output!!.writeFloat(pressure)
                output!!.writeInt(action)
                output!!.flush()
            }
        } catch (e: Exception) {
            Log.e("InputSender", "Send error", e)
        }
    }
    
    fun sendKeyEvent(keyCode: Int) {
        try {
            synchronized(output!!) {
                output!!.writeByte(0x02) // key type
                output!!.writeInt(keyCode)
                output!!.flush()
            }
        } catch (e: Exception) {
            Log.e("InputSender", "Send error", e)
        }
    }
    
    fun stop() {
        isRunning = false
        socket?.close()
    }
}

SERVICE 7: LatencyPingService.kt (Target device)
Purpose: Listen on TCP:9003, echo latency ping for measurement
kotlinclass LatencyPingService : Service() {
    
    private var socketServer: ServerSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        scope.launch {
            socketServer = ServerSocket(9003)
            while (isActive) {
                try {
                    val client = socketServer!!.accept()
                    launch {
                        val input = DataInputStream(client.inputStream)
                        val output = DataOutputStream(client.outputStream)
                        
                        while (isActive && client.isConnected) {
                            try {
                                val timestamp = input.readLong()
                                output.writeLong(timestamp)
                                output.flush()
                            } catch (e: Exception) {
                                break
                            }
                        }
                        client.close()
                    }
                } catch (e: Exception) {
                    // continue
                }
            }
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        socketServer?.close()
        scope.cancel()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent) = null
}

CUSTOM VIEW: MirrorSurfaceView.kt (Controller device)
Purpose: Display decoded video, capture and forward touch events
kotlinclass MirrorSurfaceView(
    context: Context,
    attrs: AttributeSet?
) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    
    private var targetScreenWidth = 720
    private var targetScreenHeight = 1280
    private var inputSender: InputSenderService? = null
    
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    
    init {
        holder.addCallback(this)
    }
    
    fun setTargetResolution(width: Int, height: Int) {
        targetScreenWidth = width
        targetScreenHeight = height
    }
    
    fun getSurface() = holder.surface
    
    fun setInputSender(sender: InputSenderService) {
        inputSender = sender
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val (x, y) = mapCoordinates(event.x, event.y)
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                lastX = x
                lastY = y
                inputSender?.sendTouchEvent(x, y, event.pressure, event.action)
            }
            MotionEvent.ACTION_MOVE -> {
                lastX = x
                lastY = y
                inputSender?.sendTouchEvent(x, y, event.pressure, event.action)
            }
            MotionEvent.ACTION_UP -> {
                inputSender?.sendTouchEvent(x, y, event.pressure, event.action)
            }
        }
        
        return true
    }
    
    private fun mapCoordinates(viewX: Float, viewY: Float): Pair<Float, Float> {
        val scaleX = targetScreenWidth.toFloat() / width.toFloat()
        val scaleY = targetScreenHeight.toFloat() / height.toFloat()
        return Pair(viewX * scaleX, viewY * scaleY)
    }
    
    override fun surfaceCreated(holder: SurfaceHolder) {}
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
}

XML LAYOUTS
res/layout/activity_home.xml
xml<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="📱 AndroidMirror"
        android:textSize="32sp"
        android:textStyle="bold"
        android:layout_marginBottom="48dp" />

    <Button
        android:id="@+id/btnBeControlled"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:text="🎯 BE CONTROLLED\n(Target Device)"
        android:textSize="18sp"
        android:layout_marginBottom="24dp" />

    <Button
        android:id="@+id/btnTakeControl"
        android:layout_width="match_parent"
        android:layout_height="120dp"
        android:text="🕹️ TAKE CONTROL\n(Controller Device)"
        android:textSize="18sp" />

</LinearLayout>
res/layout/activity_target.xml
xml<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="🎯 TARGET MODE"
            android:textSize="28sp"
            android:textStyle="bold"
            android:layout_marginBottom="24dp" />

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:background="#f0f0f0"
            android:padding="16dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="vertical"
                android:gravity="center">

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="YOUR IP ADDRESS"
                    android:textSize="14sp"
                    android:textColor="#666" />

                <TextView
                    android:id="@+id/tvIpAddress"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="192.168.1.45"
                    android:textSize="32sp"
                    android:textStyle="bold"
                    android:layout_marginTop="8dp" />

                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="(tap to copy)"
                    android:textSize="12sp"
                    android:textColor="#999"
                    android:layout_marginTop="4dp" />

            </LinearLayout>

        </FrameLayout>

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:background="#f9f9f9"
            android:padding="16dp">

            <TextView
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="HOW TO CONNECT:\n\n1. Install this app on the controller device\n\n2. Both devices on same WiFi\n\n3. Open app on controller, tap Take Control\n\n4. Enter the IP shown above\n\n5. Tap START HOSTING\n\n6. Grant screen capture permission\n\n7. Enable Accessibility Service when prompted"
                android:textSize="14sp"
                android:lineSpacingMultiplier="1.6" />

        </FrameLayout>

        <Button
            android:id="@+id/btnStartHosting"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="▶ START HOSTING"
            android:textSize="16sp"
            android:layout_marginTop="24dp" />

        <Button
            android:id="@+id/btnStopHosting"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="■ STOP HOSTING"
            android:textSize="16sp"
            android:layout_marginTop="16dp"
            android:visibility="gone" />

        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:background="#f0f0f0"
            android:padding="16dp">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/tvStatus"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Status: ● Waiting..."
                    android:textSize="14sp" />

                <TextView
                    android:id="@+id/tvSessions"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Sessions: 0 connected"
                    android:textSize="14sp"
                    android:layout_marginTop="8dp" />

                <TextView
                    android:id="@+id/tvFps"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="FPS: --"
                    android:textSize="14sp"
                    android:layout_marginTop="8dp" />

            </LinearLayout>

        </FrameLayout>

    </LinearLayout>

</ScrollView>
res/layout/activity_controller.xml
xml<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000">

    <!-- Connection UI (shown before connect) -->
    <ScrollView
        android:id="@+id/connectionUI"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="24dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🕹️ CONTROLLER MODE"
                android:textSize="28sp"
                android:textStyle="bold"
                android:layout_marginBottom="24dp" />

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="80dp"
                android:background="#f0f0f0"
                android:padding="16dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:gravity="center">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="TARGET IP ADDRESS"
                        android:textSize="14sp"
                        android:textColor="#666" />

                    <EditText
                        android:id="@+id/etTargetIp"
                        android:layout_width="match_parent"
                        android:layout_height="48dp"
                        android:inputType="numberDecimal"
                        android:hint="192.168.1._"
                        android:layout_marginTop="8dp" />

                </LinearLayout>

            </FrameLayout>

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:background="#f9f9f9"
                android:padding="16dp">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="HOW TO CONNECT:\n\n1. On target device, open this app\n\n2. Tap Be Controlled\n\n3. Note the IP shown there\n\n4. Both devices on same WiFi\n\n5. Enter IP above\n\n6. Tap CONNECT\n\n7. Touch the screen to control target"
                    android:textSize="14sp"
                    android:lineSpacingMultiplier="1.6" />

            </FrameLayout>

            <Button
                android:id="@+id/btnConnect"
                android:layout_width="match_parent"
                android:layout_height="56dp"
                android:text="🔗 CONNECT"
                android:textSize="16sp"
                android:layout_marginTop="24dp" />

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:background="#f0f0f0"
                android:padding="16dp">

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/tvStatus"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Status: ○ Not connected"
                        android:textSize="14sp" />

                    <TextView
                        android:id="@+id/tvLatency"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Latency: -- ms"
                        android:textSize="14sp"
                        android:layout_marginTop="8dp" />

                </LinearLayout>

            </FrameLayout>

        </LinearLayout>

    </ScrollView>

    <!-- Mirror View (shown after connect) -->
    <FrameLayout
        android:id="@+id/mirrorContainer"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.mirror.app.MirrorSurfaceView
            android:id="@+id/mirrorView"
            android:layout_width="match_parent"
            android:layout_height="match_parent" />

        <!-- Status Bar -->
        <FrameLayout
            android:id="@+id/statusBar"
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:background="#000"
            android:layout_gravity="top">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingHorizontal="16dp">

                <TextView
                    android:id="@+id/tvFps"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="FPS: 0"
                    android:textColor="#fff"
                    android:textSize="12sp" />

                <TextView
                    android:id="@+id/tvLatencyBar"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Lat: 0ms"
                    android:textColor="#fff"
                    android:textSize="12sp"
                    android:layout_weight="1"
                    android:gravity="center" />

                <Button
                    android:id="@+id/btnDisconnect"
                    android:layout_width="wrap_content"
                    android:layout_height="32dp"
                    android:text="✕"
                    android:textSize="16sp"
                    android:paddingHorizontal="12dp" />

            </LinearLayout>

        </FrameLayout>

    </FrameLayout>

</FrameLayout>

PERMISSIONS & MANIFEST
AndroidManifest.xml (full)
xml<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.mirror.app">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AndroidMirror">

        <activity android:name=".HomeActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity android:name=".TargetActivity" android:exported="false" />
        <activity android:name=".ControllerActivity" android:exported="false" />

        <service
            android:name=".ScreenCaptureService"
            android:foregroundServiceType="mediaProjection"
            android:exported="false" />

        <service
            android:name=".InputReceiverService"
            android:exported="false" />

        <service
            android:name=".StreamReceiverService"
            android:exported="false" />

        <service
            android:name=".LatencyPingService"
            android:exported="false" />

        <service
            android:name=".ControlAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>

    </application>

</manifest>

BUILD FILES
build.gradle.kts (root)
kotlinplugins {
    id("com.android.application") version "8.1.0" apply false
    kotlin("android") version "1.9.0" apply false
}
app/build.gradle.kts
kotlinplugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.mirror.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mirror.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}

DELIVERABLE CHECKLIST
✅ Single APK, one module

✅ Two modes (Target + Controller) selectable from home screen

✅ Simple XML layouts (no Compose)

✅ Step-by-step guides on each screen

✅ Accessibility Service setup walkthrough

✅ MediaProjection permission flow

✅ Raw socket TCP communication (ports 9001, 9002, 9003)

✅ H.264 hardware encoding (target) + decoding (controller)

✅ All latency optimizations applied

✅ Touch coordinate mapping with aspect ratio

✅ FPS counter + latency display

✅ Auto-reconnect on disconnect

✅ Save/restore last IP

✅ All services as ForegroundServices

✅ Complete wire protocol (handshake, video frames, input events, ping)

✅ Every file fully implemented — no TODOs, stubs, or placeholders

✅ Compiles on Android Studio with zero errors

✅ Runs on two devices on same WiFi with only permission grants