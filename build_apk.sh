#!/bin/bash

echo "正在构建 APK..."

# 使用系统 Gradle 或 gradlew
if command -v gradle &> /dev/null; then
    echo "使用系统 Gradle..."
    gradle assembleDebug --no-daemon
else
    echo "使用 Gradle Wrapper..."
    chmod +x gradlew
    ./gradlew assembleDebug --no-daemon
fi

if [ -f app/build/outputs/apk/debug/app-debug.apk ]; then
    echo ""
    echo "✅ APK 构建成功！"
    echo "📁 文件位置: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "正在复制到项目根目录..."
    cp app/build/outputs/apk/debug/app-debug.apk ./zhangyunlei-ai-avatar-fixed.apk
    echo "✅ 已复制为: zhangyunlei-ai-avatar-fixed.apk"
else
    echo ""
    echo "❌ APK 构建失败"
    exit 1
fi
