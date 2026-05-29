# 修复版 - 兰兰 AI 助手

## 功能特点

本版本修复了以下问题：

### 1. DeepSeek API 调用问题
- ✅ 修复图片上传时的模型选择逻辑，支持新模型
- ✅ 添加 Content-Type charset 支持
- ✅ 修复深度思考（DeepThink）功能

### 2. 线程与内存泄漏问题
- ✅ OkHttpClient 添加合理的读超时（300秒）
- ✅ Handler 消息在 Activity 销毁时正确清理
- ✅ ResponseBody 正确关闭，避免资源泄漏
- ✅ 所有 UI 操作添加 Activity 状态检查

### 3. 图片与内存处理问题
- ✅ 头像图片使用采样压缩，避免 OOM
- ✅ 背景图按屏幕尺寸采样
- ✅ 发送图片先压缩再 Base64 编码
- ✅ Base64 大小限制检查（最大 10MB）
- ✅ Bitmap 内存及时回收

### 4. 多附件支持
- ✅ 支持发送多个图片
- ✅ 支持图片和文件同时发送

### 5. 流式视图优化
- ✅ 复用流式视图，避免闪烁
- ✅ 提升用户体验

## 构建 APK

### 方法一：使用 Android Studio（推荐）

1. **安装 Android Studio**
   - 下载地址：https://developer.android.com/studio
   - 安装 Android Studio 4.0 或更高版本

2. **打开项目**
   - 打开 Android Studio
   - 选择 "Open an existing Android Studio project"
   - 选择项目根目录

3. **构建 Debug APK**
   - 等待 Gradle 同步完成
   - 点击菜单栏：Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 生成的 APK 文件位于：`app/build/outputs/apk/debug/app-debug.apk`

4. **构建 Release APK**
   - 点击菜单栏：Build → Generate Signed Bundle / APK
   - 选择 APK
   - 如果已有签名密钥，选择它；如果没有，点击 "Create new"
   - 填写密钥信息并完成构建

### 方法二：使用命令行

**前提条件：**
- 安装 JDK 17 或更高版本
- 安装 Android SDK

**Linux / macOS：**
```bash
# 克隆项目
git clone https://github.com/zxc10089/zhangyunlei-ai-avatar-fixed.git
cd zhangyunlei-ai-avatar-fixed

# 方式1：使用 gradlew
chmod +x gradlew
./gradlew assembleDebug

# 方式2：使用系统 Gradle（如果已安装）
gradle assembleDebug

# 生成的 APK
ls app/build/outputs/apk/debug/
```

**Windows：**
```cmd
# 克隆项目
git clone https://github.com/zxc10089/zhangyunlei-ai-avatar-fixed.git
cd zhangyunlei-ai-avatar-fixed

# 使用 gradlew.bat
gradlew.bat assembleDebug

# 或者安装 Gradle 后使用
gradle assembleDebug
```

**或者使用提供的构建脚本：**
```bash
chmod +x build_apk.sh
./build_apk.sh
```

### 方法三：使用 GitHub Actions（自动构建）

项目已配置 GitHub Actions，但需要以下步骤启用：

1. **创建具有 workflow 权限的 Personal Access Token**
   - 访问 https://github.com/settings/tokens
   - 点击 "Generate new token (classic)"
   - 勾选 `repo` 和 `workflow` 权限
   - 生成并保存 Token

2. **手动触发构建**
   - 在 GitHub 仓库页面，点击 "Actions" 标签
   - 选择 "Build APK" 工作流
   - 点击 "Run workflow" 手动触发

3. **下载构建产物**
   - 构建完成后，在 Actions 页面下载 artifact

## 安装 APK

1. 将 APK 文件传输到手机
2. 在手机上打开 APK 文件
3. 如果提示"安装来源未知"，需要在设置中允许
4. 安装并打开应用
5. 首次使用需要配置 API Key

## 配置说明

### API Key 配置
1. 首次打开应用会跳转到设置页面
2. 输入你的 DeepSeek API Key
3. API 地址通常为：`https://api.deepseek.com/v1/chat/completions`
4. 选择模型：推荐使用 `deepseek-v4-flash` 或 `deepseek-chat`

### 功能设置
- **深度思考**：开启后 AI 会先进行推理思考
- **流式输出**：开启后逐字显示 AI 回复
- **上下文长度**：设置保留的历史消息数量
- **隐藏思考**：开启后不显示 AI 的推理过程

### 头像和背景
- 可以自定义 AI 头像和用户头像
- 可以设置聊天背景图

## 常见问题

### Q: 构建失败怎么办？
A: 确保网络畅通，Gradle 需要下载依赖。如果使用代理，请配置 Gradle 的代理设置。

### Q: APK 安装失败？
A: 检查手机设置中是否允许安装未知来源应用。确保 APK 签名正确。

### Q: 图片发送失败？
A: 确保选择的图片大小合适（建议小于 10MB）。检查 API Key 是否有图片理解权限。

### Q: 深度思考功能不工作？
A: 确保选择了支持深度思考的模型（如 deepseek-chat），并开启了深度思考开关。

## 项目结构

```
zhangyunlei-ai-avatar-fixed/
├── app/
│   ├── src/main/
│   │   ├── java/zxc10089/zyl/lanlan/
│   │   │   ├── MainActivity.java          # 主界面和聊天逻辑
│   │   │   ├── SetupActivity.java         # 初始设置
│   │   │   ├── SettingsActivity.java       # 设置页面
│   │   │   ├── AiSettingsActivity.java     # AI 设置
│   │   │   ├── AboutActivity.java          # 关于页面
│   │   │   └── Prompt.java                 # 系统提示词
│   │   └── res/
│   │       ├── layout/                    # 布局文件
│   │       ├── drawable/                  # 图形资源
│   │       └── values/                    # 字符串等资源
│   └── build.gradle
├── build.gradle                           # 根构建配置
├── settings.gradle
└── gradle.properties
```

## 许可证

本项目仅供学习和参考使用。请遵守 DeepSeek 的使用条款。

## 更新日志

### v2.0
- ✅ 修复图片发送 400 错误
- ✅ 优化 UI 设计
- ✅ 修复多附件发送问题
- ✅ 优化内存使用
- ✅ 修复线程安全问题
- ✅ 改善流式输出体验
