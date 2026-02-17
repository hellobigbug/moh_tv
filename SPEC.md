# MOH TV - 安卓电视直播客户端

## 1. 项目概述

**项目名称**: MOH TV  
**项目类型**: Android TV 直播应用  
**核心功能**: 基于IPTV协议的电视直播播放器，支持多源直播源管理、频道分类、收藏、播放控制等  
**目标用户**: Android TV/智能电视/机顶盒用户  
**当前版本**: v1.0.0

## 2. 技术选型

### 框架与语言
- **语言**: Kotlin 1.9.x
- **UI框架**: Jetpack Compose (Material 3)
- **最小SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)

### 核心库
- **播放器**: Media3 ExoPlayer 1.2.x
- **网络**: Retrofit2 + OkHttp4
- **JSON解析**: Kotlinx Serialization
- **依赖注入**: Hilt
- **异步**: Kotlin Coroutines + Flow
- **本地存储**: Room Database + DataStore
- **图片加载**: Coil

### 架构模式
- **MVVM** + Clean Architecture
- **单Activity** + Compose Navigation

## 3. 功能列表

### 3.1 直播播放
- [x] M3U8/HLS流播放
- [x] 多码率自适应
- [x] 硬件加速解码
- [x] 播放控制（播放/暂停/音量/快退快进）
- [x] 频道切换（上/下一个）
- [x] 清晰度选择（自动/标清/高清/全高清/4K）
- [x] 播放失败自动重试（最多3次，间隔3秒）
- [x] 网络超时优化（15秒连接/读取超时）

### 3.2 频道管理
- [x] 频道分类（央视/卫视/地方台/体育/影视等）
- [x] 频道收藏
- [x] 频道搜索
- [x] 最近观看（最多20条记录）

### 3.3 直播源管理
- [x] GitHub源自动同步
- [x] 多源配置
- [x] 增量更新
- [x] 手动更新触发
- [x] 直播源验证
- [x] 源备份与恢复
- [x] 直播源质量检测（响应时间、频道数量、质量评分）
- [x] GitHub IPTV源搜索
- [x] 二维码配置导入/导出
- [x] 内置默认源（IPTV-org、Free-TV）

### 3.4 用户界面
- [x] TV遥控器全操作支持
- [x] 横向分类导航
- [x] 纵向频道列表
- [x] 全屏播放界面
- [x] 设置界面
- [x] Apple TV风格深色主题
- [x] 聚焦动画与视觉反馈
- [x] 启动画面

### 3.5 系统功能
- [x] 自动更新（每日凌晨2点）
- [x] 网络异常重连
- [x] 播放失败重试
- [x] 状态恢复
- [x] 首次启动自动检测最佳源

## 4. UI/UX 设计方向

### 视觉风格
- Apple TV Style 设计语言
- Material Design 3 组件
- 深色主题为主（适配电视观看）
- 简洁清爽无广告

### 颜色方案
- 主色: #2997FF (Apple Blue)
- 背景: #000000 (纯黑)
- 表面: #1C1C1E (深灰)
- 文字: #FFFFFF / #EBEBF5

### 聚焦效果
- 聚焦缩放: 1.05x - 1.08x
- 聚焦边框: 3-4dp 蓝色边框
- 聚焦发光: 半透明蓝色光晕
- 动画时长: 150-200ms

### 布局结构
- 主界面：左侧分类栏 + 右侧频道网格
- 播放界面：全屏视频 + 底部控制栏
- 设置界面：列表式布局

### 遥控器支持
- 方向键：导航
- 确认键：选择/播放
- 返回键：退出
- 菜单键：显示选项
- 音量键：调节音量

## 5. 项目结构

```
app/src/main/java/com/moh/tv/
├── data/                    # 数据层
│   ├── local/              # 本地存储
│   │   ├── AppDatabase.kt  # Room 数据库
│   │   ├── Converters.kt   # 类型转换器
│   │   ├── UserPreferences.kt
│   │   ├── dao/            # DAO 接口
│   │   └── entity/         # 实体类
│   ├── model/              # 数据模型
│   ├── remote/             # 远程数据源
│   │   ├── AutoSourceDetector.kt    # 源质量检测
│   │   ├── EpgManager.kt            # EPG 管理
│   │   ├── GithubSourceSearcher.kt  # GitHub 源搜索
│   │   ├── IptvParser.kt            # M3U 解析
│   │   └── SourceSyncManager.kt     # 源同步管理
│   └── repository/         # 数据仓库
├── di/                      # Hilt 依赖注入
├── player/                  # 播放器
│   └── PlayerManager.kt     # ExoPlayer 管理
├── ui/                      # UI 层
│   ├── components/         # 可复用组件
│   │   ├── AppleTVComponents.kt
│   │   ├── TVAnimationUtils.kt
│   │   ├── TVComponents.kt
│   │   └── QuickSearch.kt
│   ├── navigation/         # 导航
│   ├── screen/             # 页面
│   ├── theme/              # 主题
│   └── viewmodel/          # ViewModel
├── util/                    # 工具类
└── worker/                  # 后台任务
    └── SourceUpdateWorker.kt
```

## 6. 开发进度

| 阶段 | 状态 | 说明 |
|------|------|------|
| 项目初始化 | ✅ 完成 | 基础架构搭建 |
| 播放器集成 | ✅ 完成 | ExoPlayer + HLS 支持 |
| 频道管理 | ✅ 完成 | 分类、收藏、搜索 |
| 直播源管理 | ✅ 完成 | 多源、检测、同步 |
| UI 优化 | ✅ 完成 | Apple TV 风格 |
| 代码优化 | ✅ 完成 | 合并重复代码、统一播放器 |

## 7. 后续规划

- [ ] EPG 电子节目单集成
- [ ] 频道Logo自动获取
- [ ] 播放历史记录
- [ ] 家长控制功能
- [ ] 多语言支持
