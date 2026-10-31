# VeggieLens 蔬菜智能识别软件 V1.0

> 一款基于端侧深度学习模型的蔬菜智能识别 Android 应用。
> 拍照或选取相册图片即可离线识别蔬菜品种，自动匹配中文名称与分类，并生成营养知识与 AI 描述，所有识别记录本地留存。

---

## 一、软件简介

VeggieLens（以下简称"本软件"）是一款面向家庭厨房与生鲜采购场景的蔬菜识别工具。用户通过手机摄像头对准蔬菜，软件在**不联网的情况下**即可完成品种识别，并即时给出中文名称、所属分类、置信度以及营养与食用知识；同时可调用云端大模型生成更丰富的品种描述。所有识别历史保存在本机数据库，用户可在"我的"页面查看识别总量、品种覆盖度与连续使用天数等统计信息。

## 二、主要功能

| 模块 | 功能说明 |
| --- | --- |
| 拍照识别 | 基于 CameraX 实时取景，支持**自动识别**（画面稳定后自动触发，约 1.2 秒一次）与**手动识别**（点击快门）两种模式 |
| 相册识别 | 从系统相册选取本地图片进行识别 |
| 蔬菜图鉴 | 浏览可识别的全部蔬菜，查看微量营养素、时令、挑选要点、保存方法与推荐做法 |
| 品种匹配 | 将模型输出结果与内置蔬菜知识库进行别名归并与置信度筛选，输出规范的中文品种名称 |
| 结果展示 | 展示蔬菜中文名、英文名、所属科属分类、识别置信度、营养成分网格与图文描述 |
| AI 描述 | 联网时调用 DeepSeek 大模型生成品种介绍；网络异常时自动降级为本地离线描述，保证功能可用 |
| 历史记录 | 全部识别记录持久化存储于本地 Room 数据库，支持按时间浏览与回看详情 |
| 数据统计 | 统计累计识别次数、识别品种数、本月识别次数、连续使用天数 |
| 密钥安全 | 用户填写的第三方 API 密钥采用 AndroidKeyStore 硬件级密钥 + AES/GCM 加密存储，明文不落盘 |

## 三、技术栈

- **开发语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material 3
- **架构模式**：MVVM（ViewModel + StateFlow + Repository）
- **页面导航**：Navigation Compose
- **相机能力**：CameraX（camera-core / camera2 / camera-lifecycle / camera-view）
- **端侧推理**：MediaPipe Tasks Vision `ImageClassifier`，加载自训练的 25 类蔬菜识别模型（EfficientNet-Lite0 int8 量化，`veggielens_classifier_v2.tflite`，约 4 MB）离线推理
- **模型训练**：MediaPipe Model Maker 迁移学习，训练笔记本与说明见 [ml/README.md](ml/README.md)
- **本地存储**：Room（含外键约束与索引）、SharedPreferences
- **网络层**：Retrofit2 + OkHttp + Gson
- **图片加载**：Coil Compose
- **安全存储**：AndroidKeyStore + AES/GCM/NoPadding

## 四、运行环境

| 项目 | 要求 |
| --- | --- |
| 操作系统 | Android 7.0（API 24）及以上 |
| 编译版本 | compileSdk 36、targetSdk 36 |
| JDK | Java 17 |
| 构建工具 | Gradle（Kotlin DSL） |
| 硬件要求 | 带摄像头的 Android 设备；AI 描述功能需网络 |

## 五、项目结构

```
app/src/main/assets/
├── veggielens_classifier_v2.tflite   # 当前使用的识别模型
├── veggielens_classifier_v1.tflite   # 旧模型，真机验证 v2 后删除
└── vegetables/                       # 25 种蔬菜的图片

ml/                              # 识别模型训练笔记本与说明

app/src/main/java/com/example/veggielens/
├── MainActivity.kt              # 应用入口
├── navigation/                  # 路由定义与导航宿主
├── ui/
│   ├── scan/                    # 取景、快门、扫描遮罩、相册解码
│   ├── result/                  # 识别结果、营养成分、AI 描述与图鉴卡片
│   ├── guide/                   # 蔬菜图鉴列表与详情
│   ├── history/                 # 历史记录列表与条目
│   ├── profile/                 # 个人中心与统计面板
│   ├── components/              # 底部导航
│   ├── designsystem/            # 卡片、标签、空状态等通用界面组件
│   └── theme/                   # 主题配色、字体、形状与间距
├── viewmodel/                   # 扫描 / 图鉴 / 历史 / 个人中心 ViewModel、识别参数与匹配策略
├── data/
│   ├── local/                   # Room DAO
│   ├── model/                   # 实体、蔬菜知识库、数据库定义
│   └── repository/              # 数据仓库统一出口
├── description/                 # 描述生成抽象与本地降级实现
├── network/                     # DeepSeek 接口、客户端与数据模型
└── security/                    # API 密钥加密存储
```

## 六、构建与运行

```bash
# 1. 配置 Android SDK 路径（local.properties 中 sdk.dir）
# 2. 构建调试包
./gradlew assembleDebug

# 3. 安装到已连接设备
./gradlew installDebug
```

首次运行后，在「我的」页面填入第三方大模型 API 密钥即可启用 AI 描述功能；不填写不影响离线识别与本地描述。

## 七、权限说明

| 权限 | 用途 |
| --- | --- |
| `android.permission.CAMERA` | 实时取景与拍摄蔬菜照片 |
| `android.permission.INTERNET` | 调用大模型生成品种描述 |

本软件不上传用户照片至自有服务器，识别过程完全在设备端完成。

## 八、开发信息

- 开发完成日期：2026-08-26
- 版本号：V1.0（versionCode 1）
- 适用领域：家庭厨房、生鲜采购、饮食健康管理

## 九、许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE)。
