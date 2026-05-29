#!/bin/bash

# =========================================
# 兰兰 AI 助手 - 一键构建脚本 v2.0
# =========================================

set -e

echo "========================================="
echo "  兰兰 AI 助手 v2.0 修复版构建脚本"
echo "========================================="
echo ""

# 检查必要的工具
check_dependencies() {
    echo "🔍 检查构建环境..."
    
    if ! command -v java &> /dev/null; then
        echo "❌ 错误: 未找到 Java，请先安装 JDK 17+"
        echo "   下载地址: https://adoptium.net/"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo "⚠️  警告: Java 版本低于 17，建议使用 JDK 17 或更高版本"
    fi
    
    echo "✅ Java 版本: $(java -version 2>&1 | head -n 1)"
    echo ""
}

# 清理旧构建
clean_build() {
    echo "🧹 清理旧的构建文件..."
    rm -rf app/build
    rm -rf .gradle
    echo "✅ 清理完成"
    echo ""
}

# 尝试构建
build_apk() {
    echo "🚀 开始构建 APK..."
    echo ""
    
    # 检查是否有 gradlew
    if [ -f "./gradlew" ]; then
        chmod +x gradlew
        echo "使用 Gradle Wrapper..."
        ./gradlew assembleDebug --no-daemon
    elif command -v gradle &> /dev/null; then
        echo "使用系统 Gradle..."
        gradle assembleDebug --no-daemon
    else
        echo "❌ 错误: 未找到 Gradle"
        echo "   请安装 Gradle 或使用 Android Studio"
        echo "   下载地址: https://gradle.org/install/"
        exit 1
    fi
}

# 验证构建结果
verify_and_copy() {
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        echo ""
        echo "========================================="
        echo "  ✅ APK 构建成功！"
        echo "========================================="
        echo ""
        echo "📁 APK 位置: $APK_PATH"
        echo "📊 APK 大小: $(du -h "$APK_PATH" | cut -f1)"
        echo ""
        
        # 复制到项目根目录
        cp "$APK_PATH" ./zhangyunlei-ai-avatar-fixed.apk
        echo "✅ 已复制为: ./zhangyunlei-ai-avatar-fixed.apk"
        echo ""
        
        # 显示SHA256
        echo "🔐 SHA256: $(sha256sum "$APK_PATH" | cut -d' ' -f1)"
        echo ""
        
        echo "========================================="
        echo "  🎉 构建完成！"
        echo "========================================="
        echo ""
        echo "下一步:"
        echo "1. 将 APK 文件传输到手机"
        echo "2. 安装并打开应用"
        echo "3. 配置 DeepSeek API Key"
        echo "4. 开始使用！"
        echo ""
    else
        echo ""
        echo "========================================="
        echo "  ❌ APK 构建失败"
        echo "========================================="
        echo ""
        echo "请检查:"
        echo "1. 网络连接是否正常"
        echo "2. 是否安装了必要的 SDK"
        echo "3. Android SDK 环境变量是否正确设置"
        echo ""
        echo "常见问题解决:"
        echo "- 设置 ANDROID_HOME 环境变量"
        echo "- 确保安装了 Android SDK Build-Tools"
        echo "- 确保安装了 Android Platform API"
        echo ""
        exit 1
    fi
}

# 主流程
main() {
    check_dependencies
    clean_build
    build_apk
    verify_and_copy
}

# 运行
main "$@"
