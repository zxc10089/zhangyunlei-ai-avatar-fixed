# 使用 GitHub Actions 自动构建并发布 APK

## 📋 步骤

### 第一步：创建 GitHub Actions 工作流

1. 在 GitHub 仓库页面，点击 **Actions** 标签
2. 点击 **New workflow**
3. 在搜索框输入 "Android" 或选择 "Simple workflow"
4. 复制下面的 YAML 内容：

```yaml
name: Build and Release APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    name: Build APK
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'gradle'

    - name: Grant execute permission to gradlew
      run: chmod +x gradlew

    - name: Build Debug APK
      run: ./gradlew assembleDebug --no-daemon

    - name: Upload APK artifact
      uses: actions/upload-artifact@v4
      with:
        name: debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk
        retention-days: 30

    - name: Create Release
      if: github.event_name == 'workflow_dispatch' || (github.event_name == 'push' && github.ref == 'refs/heads/main')
      uses: softprops/action-gh-release@v2
      with:
        tag_name: v2.0-fixed
        name: 兰兰 AI 助手 v2.0 修复版
        body: |
          ## 🔧 本版本修复内容
          
          ### 问题修复
          - ✅ 修复图片发送400错误
          - ✅ 修复深度思考功能
          - ✅ 修复多附件发送问题
          - ✅ 优化内存使用，避免OOM
          - ✅ 修复线程安全问题
          - ✅ 改善流式输出体验
          - ✅ 优化UI设计
          
          ### DeepSeek API 调用问题
          - 修复图片上传时的模型选择逻辑
          - 添加 Content-Type charset 支持
          - 修复深度思考（DeepThink）功能
          
          ### 内存优化
          - 头像图片使用采样压缩
          - 背景图按屏幕尺寸采样
          - Bitmap 内存及时回收
          
          ### 功能增强
          - 支持发送多个图片
          - 支持图片和文件同时发送
          - 复用流式视图，避免闪烁
          
          ## 📦 下载 APK
          
          点击下方的 Assets 下载 `app-debug.apk`
          
          ## ⚙️ 配置要求
          
          - Android 5.0 (API 21) 或更高版本
          - DeepSeek API Key
          - 网络连接（用于API调用）
          
          ## 📄 许可证
          
          本项目仅供学习和参考使用。请遵守 DeepSeek 的使用条款。
        files: app/build/outputs/apk/debug/app-debug.apk
        draft: false
        prerelease: false
```

### 第二步：保存并提交

1. 点击 **Start commit**
2. 填写提交信息：`Add GitHub Actions to build and release APK`
3. 选择 **Commit directly to the main branch**
4. 点击 **Commit changes**

### 第三步：手动触发构建

1. 提交后，工作流会自动触发
2. 或者在 **Actions** 页面：
   - 选择 **Build and Release APK** 工作流
   - 点击 **Run workflow**
   - 选择 **main** 分支
   - 点击 **Run workflow** 按钮

### 第四步：下载 APK

构建成功后，你有两种方式获取APK：

#### 方式一：从 Release 下载
1. 点击 **Releases** 标签
2. 找到最新 Release：`v2.0-fixed`
3. 在 **Assets** 中下载 `app-debug.apk`

#### 方式二：从 Artifacts 下载
1. 在 **Actions** 页面，点击最新的运行
2. 在页面底部的 **Artifacts** 区域
3. 点击 **debug-apk** 下载

## 🎯 已修复的问题总结

### 1. DeepSeek API 调用问题
- ✅ 修复图片上传模型选择逻辑
- ✅ 添加 Content-Type charset 支持
- ✅ 修复深度思考功能

### 2. 线程与内存泄漏
- ✅ OkHttpClient 读超时修复
- ✅ Handler 消息清理
- ✅ ResponseBody 正确关闭
- ✅ Activity 状态检查

### 3. 图片与内存优化
- ✅ 头像图片采样压缩
- ✅ 背景图按屏幕尺寸采样
- ✅ 发送图片压缩
- ✅ Base64 大小限制
- ✅ Bitmap 内存回收

### 4. 功能增强
- ✅ 支持发送多个图片
- ✅ 支持图片和文件同时发送
- ✅ 修复流式视图闪烁

## 📱 APK 使用说明

1. 将下载的 APK 文件传输到手机
2. 在手机上点击安装
3. 如果提示"来源未知"，在设置中允许
4. 安装并打开应用
5. 输入你的 DeepSeek API Key
6. 开始使用！

## 🔧 常见问题

### Q: GitHub Actions 构建失败？
A: 检查 Actions 运行日志，查看具体错误。通常是依赖下载问题，重试即可。

### Q: 如何设置 API Key 权限？
A: 无需特别设置，Release action 不需要额外权限。

### Q: 可以手动创建 Release 吗？
A: 可以。构建成功后：
1. 点击 **Releases** → **Draft a new release**
2. 下载 Artifacts 中的 APK
3. 上传到 Release 中
