#!/bin/bash
set -e

echo "🚀 Setting up Android SDK in Codespaces..."

# Update
sudo apt-get update -qq

# Install Java
echo "📦 Installing Java 17..."
sudo apt-get install -y openjdk-17-jdk > /dev/null 2>&1
java -version

# Create directories
mkdir -p ~/android-sdk
cd ~/android-sdk

# Download SDK Tools
echo "⬇️  Downloading Android SDK Command-line Tools..."
if [ ! -f "commandlinetools-linux-*_latest.zip" ]; then
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
fi

# Unzip
echo "📦 Extracting..."
unzip -q commandlinetools-linux-*_latest.zip || true

# Organize structure
mkdir -p cmdline-tools/latest
if [ -d "cmdline-tools/bin" ]; then
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
fi

# Set environment
echo "🔧 Setting environment variables..."
if ! grep -q "ANDROID_SDK_ROOT" ~/.bashrc; then
    cat >> ~/.bashrc << 'EOF'

# Android SDK
export ANDROID_SDK_ROOT=$HOME/android-sdk
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_SDK_ROOT/platform-tools
export PATH=$PATH:$ANDROID_SDK_ROOT/emulator
EOF
fi

source ~/.bashrc

# Accept licenses
echo "⚖️  Accepting licenses..."
yes | sdkmanager --licenses > /dev/null 2>&1

# Install SDKs
echo "📥 Installing Android SDK 34..."
sdkmanager "platforms;android-34" > /dev/null 2>&1
sdkmanager "build-tools;34.0.0" > /dev/null 2>&1
sdkmanager "platform-tools" > /dev/null 2>&1

echo "✅ Android SDK ready!"
echo ""
echo "Installed:"
adb version | head -1
sdkmanager --version | head -1
java -version 2>&1 | head -1
