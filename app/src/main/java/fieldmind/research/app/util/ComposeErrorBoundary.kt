package fieldmind.research.app.util

import androidx.compose.runtime.Composable

/**
 * A Compose error boundary wrapper.
 *
 * **Important limitation:** Compose does not support `try-catch` around
 * `@Composable` function invocations because the Kotlin compiler transforms
 * composable functions into state-machine bytecode where the actual execution
 * is deferred. As a result, wrapping `content()` in `try { content() } catch`
 * will NOT compile (`"Try catch is not supported around composable function
 * invocations"`).
 *
 * For asynchronous recomposition crashes, Compose's internal error handler
 * and the framework's error channels handle recovery. For synchronous
 * composition-phase crashes, the [CrashReporter]'s default uncaught exception
 * handler (`Thread.setDefaultUncaughtExceptionHandler`) catches most errors.
 *
 * This wrapper exists as a structural placeholder — it delegates directly
 * to [content] and can be enhanced when Compose provides official error
 * boundary APIs (tracked in Jetpack Compose issue tracker).
 *
 * @param tag A label for crash log attribution (currently unused but reserved).
 * @param onCatch Callback invoked when an error is caught (currently unused).
 * @param content The composable content to render.
 */
@Composable
fun ComposeErrorBoundary(
    tag: String = "ComposeRoot",
    onCatch: ((Throwable) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    // Compose does not support try-catch around composable invocations.
    // All error handling is delegated to CrashReporter's uncaught
    // exception handler and the ANR watchdog.
    content()
}
