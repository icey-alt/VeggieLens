# VeggieLens 模型训练

`train_veggielens.ipynb` 用 MediaPipe Model Maker 训练 25 类蔬菜识别模型，导出 App 可直接加载的 `.tflite`。

## 在哪里运行

| 环境 | 说明 |
| --- | --- |
| **Kaggle Notebook（推荐）** | 两份数据集本来就在 Kaggle 上，在右侧 “Add Input” 挂载即可；Settings 里打开 GPU |
| Colab | 把数据集放到 Google Drive，挂载后改笔记本“步骤 1”的路径 |
| 本地 | 需要 Python 3.9–3.11 和 NVIDIA GPU；Apple Silicon 上 TensorFlow 相关依赖可能装不上 |

Model Maker 依赖 TensorFlow < 2.16，**只能在 Python 3.9–3.11 下运行**。笔记本第一格会检查版本。
官方已将 Model Maker 标为“仍可用，但不再积极维护”。

## 数据准备

1. **两份 Kaggle 数据集**：Vegetable Image Dataset（15 类）和 vegetables nutrionix（10 类），保持各自的 train / validation / test 目录结构，不用改文件夹名。
2. **“其他”类（强烈建议）**：一个文件夹，放 1000–2000 张非目标图片：水果、手、桌面、厨房背景、包装袋、超市货架等。不用分子目录，笔记本会自动划分。没有这个类，模型对着任何画面都会硬猜一种蔬菜。
3. **手机实拍测试集（强烈建议）**：每类 30–50 张自己拍的照片，每类一个文件夹，文件夹名用英文类名（如 `Sweet_Potato`）。只用来测试，是最接近真机效果的指标，阈值也优先按它标定。

使用前请确认两份数据集的许可证允许用于你的 App。

## 产出（`veggielens_export/`）

| 文件 | 用途 |
| --- | --- |
| `veggielens_classifier_v2.tflite` | int8 量化模型，放进 App |
| `veggielens_classifier_v2_float.tflite` | 浮点模型，用来对比量化是否掉点 |
| `confusion_matrix.png` | 混淆矩阵 |
| `training_report.json` | 标签、各类数量、准确率、建议阈值 |

## 放进 App

1. 把 `veggielens_classifier_v2.tflite` 复制到 `app/src/main/assets/`
2. 按笔记本最后输出的代码，更新 `app/src/main/java/com/example/veggielens/viewmodel/RecognitionConfig.kt`
3. 运行 `./gradlew test`，`VegetableMatcherTest` 会检查每个模型标签都能对应到正确的蔬菜
4. 真机验证后，删除旧的 `veggielens_classifier_v1.tflite`

如果加入了 “Other” 类，App 不需要改代码：`Other` 匹配不到任何蔬菜，会被当作“未识别”处理。
