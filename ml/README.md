# VeggieLens 模型训练

`train_veggielens.ipynb` 用 MediaPipe Model Maker 训练 25 类蔬菜识别模型，导出 App 可直接加载的 `.tflite`。

## 当前模型：v2（2026-10-31）

App 现在加载的是 `veggielens_classifier_v2.tflite`（`RecognitionConfig.MODEL_ASSET`）。

| 项目 | 结果 |
| --- | --- |
| 骨干网络 | EfficientNet-Lite0，整网微调 15 轮，输入 224×224 |
| 训练 / 验证 / 测试 | 24787 / 4657 / 4656 张（验证、测试已去掉与训练集重复的 897 张） |
| 测试集 Top-1 | 浮点 98.35%，int8 98.52%（量化不掉点） |
| 测试集 Top-3 | 99.9% |
| 体积 | int8 4.0 MB（App 用这个），浮点 12.9 MB |

需要注意：

- **测试成绩偏乐观**：数据集里 nutrionix 的 10 类大多是预先增强过的图片（`aug_` 开头），翻转、旋转后的副本去重查不出来，仍可能同时出现在训练集和测试集。真机效果要用手机实拍测试集验证。
- **Onion 是短板**：召回率 74%，测试集 158 张里 28 张被认成 Onion_Red、7 张认成 Garlic；Onion_Red 的精确率也只有 85.6%。可能是数据集 `Onion` 文件夹里混有红洋葱，重训前建议先清理。其余各类都在 97% 以上。
- **阈值没用笔记本的建议值**：这次没有 “其他” 类和手机实拍集，步骤 7 只能在测试集上标定，结果三个阈值都是 5，不能用。App 沿用 70 / 40 / 25（自动 / 手动 / 提示），对应测试集上的准确率 99.7% / 98.9% / 98.6%，出结果的比例 94.8% / 99.4% / 99.9%。这些数字没有包含非蔬菜画面，自动识别的误触发率还不知道。
- 训练用了 `label_smoothing=0.1`，模型最高分很少超过 0.9，阈值设到 90 以上几乎不出结果（90 时覆盖率 47%，95 时 6%）。

## 运行环境：本地 WSL2 + NVIDIA GPU

Model Maker 依赖 TensorFlow < 2.16，**只能在 Python 3.9–3.11 下运行**，而且必须在 Linux 里跑：
它依赖的 `tensorflow-text` 没有 Windows 版，Windows 原生的 TensorFlow 也不支持 GPU。
所以在 Windows 上要用 **WSL2（Ubuntu）**，只要 Windows 端的 NVIDIA 驱动够新、WSL 里能运行 `nvidia-smi`。

官方已将 Model Maker 标为“仍可用，但不再积极维护”。

### 1. 环境：conda 环境 `tf216`

用 WSL 里已有的 conda 环境 `tf216`（`~/miniconda3/envs/tf216`）。名字叫 tf216，实际装的是能跑 Model Maker 的版本：

| 包 | 版本 |
| --- | --- |
| Python | 3.10 |
| tensorflow（含 CUDA 的 pip 包） | 2.15.1 |
| mediapipe-model-maker | 0.2.1.4 |
| mediapipe | 0.10.21 |
| tensorflow-text / tf-models-official | 2.15.0 |

笔记本还需要 `imagehash`（去重用），第一次用之前装一下：

```bash
~/miniconda3/envs/tf216/bin/pip install imagehash
```

如果 pip 一直 `Read timed out`（WSL 里直连不通），让它走 Windows 上的代理，端口换成你自己的：`HTTPS_PROXY=http://127.0.0.1:7897 ~/miniconda3/envs/tf216/bin/pip install imagehash`。
`.wslconfig` 里是 `networkingMode=mirrored` 时，WSL 可以直接用 `127.0.0.1` 访问 Windows 上的代理。

### 2. 打开笔记本

任选一种：

- **浏览器**：在 WSL 里运行下面的命令，再在 Windows 浏览器里打开它打印的 `http://localhost:8888/...` 链接
- **VS Code**：装 WSL 扩展，`WSL: Connect to WSL` 后打开仓库，打开笔记本，内核选 `tf216` 的 Python（`~/miniconda3/envs/tf216/bin/python`）

```bash
cd /mnt/d/Code/Android/VeggieLens/ml && export HTTPS_PROXY=http://127.0.0.1:7897 && ~/miniconda3/envs/tf216/bin/jupyter lab
```

训练第一次运行时要从 TF Hub 下载预训练权重，所以上面先设了代理；能直连就去掉 `export HTTPS_PROXY=...`。

### 常见报错与警告

| 输出 | 说明 |
| --- | --- |
| `ValueError: Setting hub.KerasLayer.trainable = True is unsupported when loading from the TF1 Hub format` | EfficientNet-Lite 在 TF Hub 上是 TF1 格式，Model Maker 默认不能微调它。笔记本训练格已改用 Model Maker 自带的 `HubKerasLayerV1V2` 解决；如果还报这个错，说明那一格不是最新版本 |
| `Gradients do not exist for variables [...moving_mean...]` | 正常：`HubKerasLayerV1V2` 不更新 BatchNorm 的滑动均值/方差，可忽略 |
| `` `period` argument is deprecated `` | Model Maker 内部的过时参数提示，可忽略 |
| `PNG warning: iCCP: known incorrect sRGB profile` | 个别 PNG 的色彩配置有问题，不影响读取，可忽略 |
| `Unable to register cuDNN / cuFFT / cuBLAS factory` | TensorFlow 2.15 启动时的已知提示，不影响使用 GPU |

## 数据准备

全部放在 WSL 的 `~/Data/vegetables/` 下（在 Windows 资源管理器里是 `\\wsl.localhost\Ubuntu-22.04\home\<用户名>\Data\vegetables`），放在 WSL 自己的文件系统里比放在 `/mnt/d` 下读写快得多：

```
~/Data/vegetables/
├── Vegetable_Images/       训练集：已合并好的 25 类
├── other/                  （强烈建议）“其他”类
├── real_photos/            （强烈建议）手机实拍测试集
└── veggielens_build/       笔记本自动生成的合并数据，每次运行都会重建
```

路径在笔记本“步骤 1”的 `DATA_HOME` / `DATA_SOURCES` 里改。

1. **训练集 `Vegetable_Images/`**：由 Vegetable Image Dataset（15 类）和 vegetables nutrionix（10 类）合并而成，下面是 train / validation / test，每个下面每类一个文件夹。文件夹名写 `Onion Red` 或 `Onion_Red` 都行，笔记本会统一成 App 用的类名。原始数据只读不改，去重等操作都在 `veggielens_build/` 的副本上做。
2. **“其他”类**：1000–2000 张非目标图片：水果、手、桌面、厨房背景、包装袋、超市货架等。不用分子目录，笔记本会自动划分。没有这个类，模型对着任何画面都会硬猜一种蔬菜。
3. **手机实拍测试集**：每类 30–50 张自己拍的照片，每类一个文件夹，文件夹名用英文类名（如 `Sweet_Potato`）。只用来测试，是最接近真机效果的指标，阈值也优先按它标定。也可以加一个 `Other/` 文件夹放真实背景照片。

`other/`、`real_photos/` 不存在或为空时笔记本会当作未提供。使用前请确认两份数据集的许可证允许用于你的 App。

## 产出（`ml/veggielens_export/`）

| 文件 | 用途 |
| --- | --- |
| `veggielens_classifier_v2.tflite` | int8 量化模型，放进 App |
| `veggielens_classifier_v2_float.tflite` | 浮点模型，用来对比量化是否掉点 |
| `confusion_matrix.png` | 混淆矩阵 |
| `training_report.json` | 标签、各类数量、准确率、建议阈值。各类数量是去重前的 |
| `metadata.json` | 模型元数据（输入归一化、标签文件），已打包进 `.tflite` |
| `checkpoint/`、`summaries/` | 训练检查点和 TensorBoard 日志，约 600 MB；不需要续训可以删 |

整个 `ml/veggielens_export/` 不进 git，`confusion_matrix.png` 和 `training_report.json` 建议另外保存，方便和下一版对比。
合并后的训练数据在 `~/Data/vegetables/veggielens_build/`，每次运行都会重建。

## 放进 App

1. 运行笔记本最后一格，把 int8 模型复制到 `app/src/main/assets/`
2. 改 `app/src/main/java/com/example/veggielens/viewmodel/RecognitionConfig.kt` 里的 `MODEL_ASSET`
3. 阈值：只有提供了 “其他” 类或手机实拍测试集时，才采用笔记本步骤 7 输出的建议值；否则看标定表手动选（见上面“当前模型”）
4. 运行 `./gradlew test`。`VegetableMatcherTest` 检查的是标签匹配规则（如 `Onion Red` 不会被认成 `Onion`），它不读模型文件；新模型的类别有增减时，要同步更新测试里的标签列表
5. 真机验证后，删除旧的 `veggielens_classifier_v1.tflite`（目前 assets 里还保留着它）

**加入 “Other” 类之前要先改 App**：`VegetableMatcher.findBestMatch` 遇到匹配不到蔬菜的标签会跳过，接着看下一个候选。所以 top-1 是 `Other`、第二名的蔬菜分数又达到阈值时，App 仍会给出这个蔬菜。要让 `Other` 起到 “未识别” 的作用，需要在 top-1 是 `Other` 时直接返回 null。
