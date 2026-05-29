@echo off
chcp 65001 > nul
REM =========================================
REM 兰兰 AI 助手 - 一键构建脚本 v2.0 (Windows)
REM =========================================

echo =========================================
echo   兰兰 AI 助手 v2.0 修复版构建脚本
echo =========================================
echo.

REM 检查Java
java -version > nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到 Java，请先安装 JDK 17+
    echo    下载地址: https://adoptium.net/
    pause
    exit /b 1
)

echo ✅ Java 版本:
java -version 2>&1 | findstr /R "version"
echo.

REM 清理旧构建
echo 🧹 清理旧的构建文件...
if exist app\build rmdir /s /q app\build
if exist .gradle rmdir /s /q .gradle
echo ✅ 清理完成
echo.

REM 检查是否有 gradlew.bat
if exist gradlew.bat (
    echo 使用 Gradle Wrapper...
    call gradlew.bat assembleDebug --no-daemon
) else (
    echo 使用系统 Gradle...
    gradle assembleDebug --no-daemon
)

REM 检查结果
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
if exist "%APK_PATH%" (
    echo.
    echo =========================================
    echo   ✅ APK 构建成功！
    echo =========================================
    echo.
    echo 📁 APK 位置: %APK_PATH%
    echo 📊 APK 大小:
    dir "%APK_PATH%" | findstr /R "^[0-9]"
    echo.
    copy "%APK_PATH%" "zhangyunlei-ai-avatar-fixed.apk" > nul
    echo ✅ 已复制为: zhangyunlei-ai-avatar-fixed.apk
    echo.
    echo =========================================
    echo   🎉 构建完成！
    echo =========================================
    echo.
    echo 下一步:
    echo 1. 将 APK 文件传输到手机
    echo 2. 安装并打开应用
    echo 3. 配置 DeepSeek API Key
    echo 4. 开始使用！
    echo.
) else (
    echo.
    echo =========================================
    echo   ❌ APK 构建失败
    echo =========================================
    echo.
    echo 请检查:
    echo 1. 网络连接是否正常
    echo 2. 是否安装了必要的 SDK
    echo 3. Android SDK 环境变量是否正确设置
    echo.
    pause
    exit /b 1
)

pause
