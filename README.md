# MOH TV 安卓TV直播客户端

English | [中文](./README_ZH.md)

A modern Android TV live streaming application built with Jetpack Compose and Media3 ExoPlayer.

## Features

### 直播播放
- 📺 M3U8/HLS 流媒体播放
- 🔄 多码率自适应 & 手动清晰度切换
- ⚡ 硬件加速解码
- 🔁 播放失败自动重试（最多3次）
- 🎬 播放控制（播放/暂停/音量/快退快进）

### 频道管理
- 📂 频道分类（央视/卫视/地方台/体育/影视等）
- ⭐ 频道收藏
- 🔍 频道搜索
- 📜 最近观看记录

### 直播源管理
- 🌐 GitHub 源自动同步
- 📡 多源配置与切换
- ✅ 直播源质量检测
- 🔍 GitHub IPTV 源搜索
- 📱 二维码配置导入/导出
- ⏰ 每日凌晨2点自动更新

### 用户界面
- 🎨 Apple TV 风格深色主题
- 📺 TV 遥控器全操作支持
- 🎯 聚焦动画与视觉反馈
- 🌙 护眼深色模式

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin 1.9.x |
| UI | Jetpack Compose + Material 3 |
| Player | Media3 ExoPlayer 1.2.x |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room + DataStore |
| Network | Retrofit + OkHttp4 |
| Async | Kotlin Coroutines + Flow |

## Project Structure

```
app/src/main/java/com/moh/tv/
├── data/                    # 数据层
│   ├── local/              # 本地数据库 (Room)
│   ├── model/              # 数据模型
│   ├── remote/             # 远程数据源
│   └── repository/         # 数据仓库
├── di/                      # Hilt 依赖注入模块
├── player/                  # ExoPlayer 播放器管理
├── ui/                      # UI 层
│   ├── components/         # 可复用组件
│   ├── navigation/         # 导航配置
│   ├── screen/             # 页面
│   ├── theme/              # 主题配置
│   └── viewmodel/          # ViewModel
├── util/                    # 工具类
└── worker/                  # WorkManager 后台任务
```

## Build

### Prerequisites

- JDK 17+
- Android SDK 34

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Auto Build

This project uses GitHub Actions for automatic builds. On every push to main branch, the APK will be automatically built.

## Built-in Sources

- IPTV-org 中国频道
- IPTV-org 全球频道
- Free-TV 全球频道

## License

MIT License
