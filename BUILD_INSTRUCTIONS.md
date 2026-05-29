# 在本机构建 APK

由于网络限制，无法在此环境直接构建 APK。但已为您准备好所有工具，只需在本地运行即可。

## 🚀 一键构建（推荐）

### Windows 用户
```batch
1. 下载并解压项目源码
2. 双击运行 build_apk.bat
3. 等待构建完成
4. 在项目目录找到 zhangyunlei-ai-avatar-fixed.apk
```

### Linux / macOS 用户
```bash
1. 下载并解压项目源码
2. 打开终端，进入项目目录
3. 运行: chmod +x build_apk.sh && ./build_apk.sh
4. 等待构建完成
5. 在项目目录找到 zhangyunlei-ai-avatar-fixed.apk
```

## 📋 构建要求

### 必需
- Java JDK 17 或更高版本
- 网络连接（Maven 仓库下载依赖）

### 可选（推荐）
- Android Studio（包含所有必需 SDK）
- Android SDK（如果使用命令行构建）

## 🔧 使用 Android Studio 构建

1. **安装 Android Studio**
   - 下载地址：https://developer.android.com/studio
   - 安装时选择"Custom"并勾选所有组件

2. **打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 选择项目根目录

3. **等待 Gradle 同步**
   - 首次打开会下载依赖，请耐心等待

4. **构建 APK**
   - 菜单栏：Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 或者快捷键：Ctrl+F9（编译）然后 Shift+F10（运行）

5. **找到 APK 文件**
   - 位置：`app/build/outputs/apk/debug/app-debug.apk`

## 💡 常见问题

### Q: 构建失败，提示网络错误？
**A:** 确保网络连接正常，可以访问 Maven 仓库。如果使用代理，需要在 Gradle 中配置代理。

**Linux/macOS 配置代理：**
```bash
export http_proxy=http://代理服务器:端口
export https_proxy=http://代理服务器:端口
./gradlew assembleDebug
```

**Windows 配置代理：**
```batch
set http_proxy=http://代理服务器:端口
set https_proxy=http://代理服务器:端口
gradlew.bat assembleDebug
```

### Q: 提示 "Android SDK not found"？
**A:** 设置 ANDROID_HOME 环境变量：

**Linux/macOS：**
```bash
export ANDROID_HOME=/path/to/android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

**Windows：**
```batch
set ANDROID_HOME=C:\path\to\android\sdk
set PATH=%PATH%;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools
```

### Q: Gradle 同步失败？
**A:** 
1. 清理缓存：删除 `.gradle` 文件夹
2. 重新打开项目
3. 等待完全同步完成

### Q: 构建很慢？
**A:** 
1. 首次构建需要下载依赖，请耐心等待
2. 可以配置 Gradle 使用国内镜像（已在项目中配置）
3. 确保网络稳定

### Q: 如何构建 Release 版本？
**A:**
1. 使用 Android Studio：Build → Generate Signed Bundle / APK
2. 使用命令行： `./gradlew assembleRelease`
3. 需要配置签名密钥

## 📝 手动命令行构建步骤

如果不想使用脚本，可以手动执行以下命令：

### Linux / macOS
```bash
# 1. 安装依赖
sudo apt-get install openjdk-17-jdk  # Ubuntu/Debian
# 或
brew install openjdk@17  # macOS

# 2. 克隆项目
git clone https://github.com/zxc10089/zhangyunlei-ai-avatar-fixed.git
cd zhangyunlei-ai-avatar-fixed

# 3. 构建
chmod +x gradlew
./gradlew assembleDebug

# 4. APK 位置
ls app/build/outputs/apk/debug/
```

### Windows
```cmd
# 1. 安装 JDK 17
# 下载地址: https://adoptium.net/

# 2. 克隆项目（使用 Git Bash 或 PowerShell）
git clone https://github.com/zxc10089/zhangyunlei-ai-avatar-fixed.git
cd zhangyunlei-ai-avatar-fixed

# 3. 构建
gradlew.bat assembleDebug

# 4. APK 位置
dir app\build\outputs\apk\debug\
```

## 🎯 构建成功后

1. **安装 APK**
   - 将 APK 文件传输到手机
   - 在手机上点击安装
   - 如果提示"来源未知"，需要在设置中允许

2. **首次使用**
   - 打开应用
   - 输入 DeepSeek API Key
   - 开始使用！

## 📞 获取帮助

如果在构建过程中遇到问题：
1. 查看错误信息
2. 参考上面的常见问题
3. 检查网络连接
4. 确保所有工具正确安装

祝您构建成功！🎉
