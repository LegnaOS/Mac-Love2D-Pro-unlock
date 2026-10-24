# Mac Love2D Pro unlock

**LoveLive2D Mac 0.2.0 · 1603 候选适配**

[下载 macOS 版本](https://github.com/LegnaOS/Mac-Love2D-Pro-unlock/releases) · [English](README.en.md)

原生 Cocoa 界面、Java 事务后端；支持选择 Cubism `.app` 或包含 `res/` 的安装目录。
源码沿用 GPL-3.0，来源、固定上游提交见 [UPSTREAM.md](UPSTREAM.md)。

## 适配范围

| 资源 | 文件检查 | 当前验证 |
| --- | --- | --- |
| rlm1501 | 唯一资源摘要与 `com/live2d/cubism/g.class` 对应 | 合成文件的备份、应用、恢复测试；本次没有实际旧编辑器测试 |
| rlm1603 / Cubism 5.3.04 | 主 JAR、RLM JAR 双 SHA256 精确匹配；摘要类改为 `h.class` | 隔离 Java 入口启动，显示“试用版剩余 9999 天”；模型打开、保存、纹理集生成、moc3 导出 |
| 其他版本或已被其他工具修改的 1603 | 不匹配则保持文件 | 不执行猜测性替换 |

1603 固定输入：

```text
Live2D_Cubism.jar
c9bb3e9960bf49908a531b7010421c393a8dd5f9376745eb6fdb273f41a1cf5d
rlm1603.jar
f755966ea45739ef3706e324fa001b83058c8c430a44f754c0412d80403b60cd
```

**当前是候选适配，不是已完成 PRO 激活或全部功能验收。** 5.3.04 隔离副本的
原生 `CubismEditor` 启动器被本机 macOS 拦截；ad-hoc 签名后仍有该问题。
运行验证使用独立 Java 17 入口、独立 `user.home`。这与双击原生 `.app` 的验收不同。
未验证全部原生许可属性、所有编辑功能、Intel 实机及长期稳定性。

## 使用

1. 保存并退出目标 Cubism。先复制完整安装目录，保留与 `.app` 同级的 `res`、`jre`、`data` 等内容。
2. 打开 `LoveLive2D Mac.app`，选择副本并检查版本。
3. 匹配后“备份并应用”。`.lovelive2d-mac-backup` 保存原始两个 JAR 和摘要记录；已有备份时不会覆盖。
4. 验证启动、实际界面状态、模型保存和导出。工具显示 `PATCHED` 只代表文件写入及校验完成。
5. “恢复原始文件”校验备份及当前文件后还原。存在外部改动时保留现场；恢复后仍保留备份。

Mac 工具是 arm64/x86_64 通用二进制，最低 macOS 12；需要 Java 17。
部分 Cubism 随附的是精简运行时，没有 `bin/java`，此时使用系统 JDK。
软件包不包含商业编辑器、SDK、模型或用户许可文件。

## 1603 实现

- 原 `RlmLicense` 的公共/受保护方法描述符保持一致。
- 只替换 `RlmLicense.class`；其余 1603 类与 `librlm1603.jnilib` 保留。
- 修正未执行原生 checkout 时的状态、到期天数、产品名与 checkin 路径。
- 尚未覆盖的原生属性方法遇到零句柄时抛 Java 异常，避免将零句柄传进 JNI。
- 在 `h.class` 中精确替换原 RLM SHA256。改变 JAR 后移除失效签名元数据。
- ZIP 内直接逐条处理，保留大小写不同的类；不解压到 macOS 默认大小写不敏感目录。
- 写入前备份，暂存文件逐个原子替换，操作异常则恢复；两文件操作不等于文件系统级跨文件原子事务。
- 工具不修改模型、系统安全设置或全局许可目录，也不自动重新签名目标 Cubism。

## 构建与测试

在本仓库根目录（需要 Xcode Command Line Tools、JDK 17、Python 3）：

```sh
bash build.sh
python3 test.py
# 可选：对本机原始 1603 资源做临时副本测试，原资源只读
CUBISM_1603_RES="/path/to/original/res" python3 test.py
```

测试涵盖文件匹配、大小写条目、备份、重复操作、恢复、外部变更保护、零句柄与生命周期。
真实文件测试还比较每个条目、方法描述符及恢复后的 SHA256。

## 验证记录与发布

2026-10-24：原工作区内的 7 项测试通过，包括固定 1603 原始文件临时副本的修改与恢复。
默认测试不需要商业文件，会跳过真实文件测试；设置 `CUBISM_1603_RES` 后可在本地运行。
原始编辑器 JAR、角色工程和实验输出不发布。基础七部件模型的导出验证不代表动作绑定完成。

独立仓库从原工作区提交 `706c674` 中的工具模块提取，不包含其他项目的历史。
版本变更见 [CHANGELOG.md](CHANGELOG.md)。

```sh
python3 package.py
```

生成 `dist/` 下的 Mac ZIP 与 SHA256；GitHub Release 标记为预发布，保留上述验证边界。
