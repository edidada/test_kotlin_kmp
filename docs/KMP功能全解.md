# Kotlin Multiplatform（KMP）功能全解

> 本文档配套本仓库的演示代码：`src/commonMain`、`src/jvmMain`、`src/jsMain`、`src/commonTest`。
> 每个小节都标注了对应的演示文件，可运行 `./gradlew runDemo`（JVM）或 `./gradlew jsNodeTest`（JS）实际验证。

## 1. 什么是 KMP

Kotlin Multiplatform 让业务逻辑写在一处（common 代码），编译产物同时覆盖 JVM、JS/Wasm、Android、iOS/Linux/Windows 等原生平台；平台强相关部分通过 `expect/actual` 机制由各平台分别实现。与"一套代码跨平台运行"的 Flutter 类框架不同，KMP 编译到每个平台的原生产物，UI 和平台 API 可以各自保留原生方案，也可以只共享逻辑层。

## 2. 构建配置（build.gradle）

本项目使用 `org.jetbrains.kotlin.multiplatform` 插件（2.1.10），关键块：

```groovy
kotlin {
    jvmToolchain(17)        // JVM 字节码目标 JDK
    jvm()                   // JVM 目标
    js { nodejs() }         // Kotlin/JS 目标，跑在 Node.js

    sourceSets {
        commonMain.dependencies {
            implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0'
            implementation 'org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3'
        }
        commonTest.dependencies {
            implementation kotlin('test')
        }
    }
}
```

要点：

- 一个 `kotlin {}` 扩展里声明所有目标；每加一个目标（如 `mingwX64()`），Gradle 会自动生成对应的 `compileKotlin<Native>`、`<native>Test` 等任务。
- 依赖写在**源集**（source set）上而不是全局 `dependencies {}`；`commonMain` 的多平台依赖会自动解析到各目标对应变体（如 coroutines 在 JVM 解析成 `-jvm` artifact，在 JS 解析成 `-js` artifact）。
- 本项目在 `build.gradle` 末尾还注册了一个 `runDemo` JavaExec 任务，直接演示如何从 KMP 的 jvm 编译产物取 classpath 运行程序。
- 国内网络下 Gradle Plugin Portal（plugins.gradle.org）直连不稳定，且 Kotlin 插件 jar 从 repo.maven.apache.org 下载也会因代理握手中断失败。本项目实测方案：`settings.gradle` 的 `pluginManagement` 与 `build.gradle` 的 `repositories` 都优先使用阿里云镜像（`maven.aliyun.com/repository/gradle-plugin`、`/public`、`/central`），再回退 `mavenCentral()`/`gradlePluginPortal()`。
- 模板自带的 `org.gradle.toolchains.foojay-resolver-convention`（用于自动下载 JDK）只发布在 Plugin Portal，已移除——本机已有 JDK 17（`java -version` 确认），`jvmToolchain(17)` 会自动探测本地 JDK，无需下载。

## 3. 源集结构与目标层级（Target Hierarchy）

KMP 的目录约定（本项目采用的结构）：

```
src/
├── commonMain/kotlin/     平台无关代码（expect 声明 + 通用逻辑）
├── commonTest/kotlin/     平台无关测试（在所有平台各跑一遍）
├── jvmMain/kotlin/        JVM actual 实现 + JVM 入口
├── jvmTest/kotlin/        JVM 专属测试（可用 JUnit、JDK API）
├── jsMain/kotlin/         JS actual 实现 + JS 入口
└── jsTest/kotlin/         JS 专属测试
```

Kotlin 1.9+ 默认启用 **Default Target Hierarchy**：声明多个目标后，IDE/Gradle 会自动生成中间层源集，无需手工接线，例如：

- `nativeMain`：所有 Kotlin/Native 目标共享
- `appleMain`：iOS/macOS/tvOS/watchOS 共享
- `jvmAndAndroidMain`（需手动 extends `jvmMain` 或 `commonMain` 语义上复用）

自定义接线的写法（在 `kotlin {}` 中）：

```groovy
applyDefaultHierarchyTemplate()          // 默认模板
// 或自定义：
targetHierarchy.get().addGroupedSourceSet("jvmAndNative", "中间层源集")
```

规则：任何源集只能依赖"更通用"的源集（commonMain ← 中间层 ← 平台层），反向不可见。这正是编译期隔离平台 API 的机制——在 `commonMain` 里调用 JDK 类会直接编译失败。

## 4. expect / actual —— KMP 核心机制

演示文件：`commonMain/.../Platform.kt`（声明）、`jvmMain/.../Platform.jvm.kt`、`jsMain/.../Platform.js.kt`（实现）。

支持的 expect 形态及对应演示：

| 形态 | 演示 | 注意事项 |
|---|---|---|
| `expect val` 属性 | `platformName`、`pathSeparator` | actual 侧类型必须兼容 |
| `expect fun` 函数 | `nowMillis()`、`formatNow()` | 不能是 inline（除非 expect 就声明 inline） |
| 带默认参数的函数 | `repeatText(text, times = 2)` | 默认值**只能写在 expect 侧** |
| `expect class` + companion | `PlatformInfo.describe()` | actual 类必须逐成员 `actual` 对齐 |
| `expect object` 单例 | `BuildConfig.flavor` | 每个平台一个单例实现 |
| `expect annotation class` | `@PlatformMarker` | 各平台可映射不同保留策略/目标 |

其他规则要点：

- actual 可以放在任意"依赖该平台源集"的位置，但惯例是 `<platform>Main/.../<Name>.<platform>.kt`。
- expect/actual 之外还有 **optional expectation**（`@OptionalExpectation`）：某平台不提供实现也能编译（例如 `@JvmInline` 在 common 就是 optional 的，但需要显式 `import kotlin.jvm.JvmInline`——实测 commonMain 裸写报 Unresolved reference）。
- expect **类**（class/object/annotation/interface）在 Kotlin 2.1 仍是 Beta，编译伴随警告；本项目在 `build.gradle` 用 `compilerOptions { freeCompilerArgs.add('-Xexpect-actual-classes') }` 对所有目标演示了统一抑制。
- expect 声明的类不能出现在公共 API 之外的"跨平台签名擦除"问题中——发布库时元数据会完整保留映射关系。
- 只有 KMP 项目（或开启 `expect-actual-classes` 的 Android/KMM 配置）可用 expect/actual；纯 JVM 插件下不可用。

## 5. 平台专属 API 与互操作

### JVM 侧（`jvmMain/Platform.jvm.kt`）

直接使用 JDK：`System.getProperty("os.name")`、`SimpleDateFormat`、`java.util.Date`、`runBlocking` 等。JVM 目标还能无缝调用任何 Java 库和 Android API。

### JS 侧（`jsMain/Platform.js.kt`）

三种互操作方式：

1. `js("...")` 内联表达式（本项目演示）：`js("process.version")`、`js("require('path').sep")`，返回 `dynamic` 可隐式转换。**实测坑**：`js()` 必须直接写在函数体内，不能用于顶层属性初始化（报 "The `js` function must be called inside a function"），本项目用 `private fun nodeVersion(): String = js("...")` 包一层解决。
2. `external` 接口声明：编译期映射到已存在的 JS 对象，不生成实现。
3. `@JsModule` / `@JsFun` 注解绑定 npm 模块；配合 `kotlin-js-store/yarn.lock` 可把 npm 依赖直接接进 Gradle 构建。

### Native 侧（本项目未启用，示例配置见 build.gradle 注释）

- 平台 API：`platform.posix`、`platform.Foundation` 等由系统库自动生成，无需配置。
- **cinterop**：在 `.def` 文件里声明 C 头文件，即可在 Kotlin 中调用 C/ObjC 库（KMP 独有的原生互操作能力）。
- `objc`/`SKIE` 方向：Kotlin 编译成 XCFramework 供 Swift/ObjC 调用。

## 6. 协程（kotlinx.coroutines）多平台

演示：`commonMain/.../CoroutinesDemo.kt`。

- `suspend` 函数、`delay`、`Flow`、`Channel`、`Mutex` 等全部在 common 可用。
- 差异点：`runBlocking` 只在 JVM/Native 存在；JS 不能阻塞事件循环，改用 `MainScope().launch`（对比 `jvmMain/Main.kt` 与 `jsMain/MainJs.kt` 两个入口的写法）。
- `Dispatchers.Main` 需要各平台对应扩展（JVM 需额外 artifact；Native 需 `-native-main` 配置思路类似）。

## 7. kotlinx.serialization 多平台

演示：`commonMain/.../SerializationDemo.kt`。

- 构建需同时应用 `org.jetbrains.kotlin.plugin.serialization` 插件（编译器插件按目标分别生成 JVM 字节码 / JS / Native 的序列化器）。
- `@Serializable data class`、`Json.encodeToString/decodeFromString` 在 common 完整可用，本项目做了跨平台 round-trip 测试。
- Native 目标上还能用 `Json.encodeToByteArray` 的替代方案（kotlinx-serialization-cbor/protobuf 均为多平台库）。

## 8. kotlin-test —— 一套测试跑遍所有平台

演示：`commonTest/...` 下 `PlatformTest`、`FeaturesTest`、`CoroutinesTest`（同一份源码，`jvmTest` 与 `jsNodeTest` 任务各执行一次）。

- 断言 API：`assertEquals`、`assertTrue`、`assertIs`、`assertFailsWith` 等是**多平台**的，没有 JUnit 依赖。
- `@Test` 在 common 用 `kotlin.test.Test`（expect/actual 注解，JVM 映射到 JUnit5/JUnit4，JS 映射到 Mocha）。
- `kotlinx-coroutines-test` 的 `runTest` 也是多平台的（本项目演示）；`runBlocking` 版本仅 JVM/Native。
- 运行：`./gradlew allTests` 跑所有目标；`./gradlew jvmTest`、`./gradlew jsNodeTest` 单独跑。
- 平台专属测试放 `jvmTest`/`jsTest`/`nativeTest` 源集，可自由使用 JDK/Node API。

## 9. 语言特性的平台可用性矩阵（本项目已演示）

以下特性在 commonMain 全平台可用，均有对应演示文件：

| 特性 | 文件 |
|---|---|
| sealed 接口 + 穷尽 when（ADT） | `SealedDemo.kt` |
| 类型安全 DSL + `@DslMarker` | `ConfigDsl.kt` |
| `inline` + `reified` 泛型 | `GenericDemo.kt` |
| `value class`（`@JvmInline` 是 optional expectation） | `ValueClassDemo.kt` |
| data class / data object | `SealedDemo.kt`、`SerializationDemo.kt` |
| 扩展函数/属性、作用域函数 | 各演示文件通篇使用 |

仅部分平台可用的常见项：`Thread`（JVM/Native）、`synchronized`、反射（JVM 全量 / Native 受限 / JS 无）、`System.currentTimeMillis()`（应改用 expect fun，见演示）。

## 10. 入口与运行方式

- KMP 没有"一个 main 跑所有平台"的概念：**每个平台源集各有一个 `fun main()`**，互不冲突（common 里则不要写顶层 main）。
- JVM：`./gradlew runDemo`（项目注册的 JavaExec 任务）或 `jvmJar` 后 `java -jar`。
- JS：`js { nodejs() }` 下 `./gradlew jsNodeDevelopmentRun` 可执行编译出的 Node 程序；本项目在 `jsNodeTest` 中验证了全部逻辑。
- Android/iOS 集成（本演示未包含）：Android 侧通过 `androidTarget()` 目标 + AGP；iOS 侧由 Xcode 工程调用 `embedAndSignAppleFrameworkForXcode`。

## 11. 发布为库（convention plugin / publishing）

给 KMP 库加发布只需应用 `maven-publish` 插件；Gradle Metadata（`.module` 文件）会自动描述各平台变体，消费方按自己的平台解析到对应 artifact（Klib/JAR/JS）。Android 目标还需 `androidTarget { publishLibraryVariants("release") }`。这就是"kotlin multiplatform library"能被 JVM、JS、Native 三侧同时依赖的原理。

## 12. Wasm / Native 目标速览（扩展阅读）

- `webassembly { binaryen { nodejs() } }`：Kotlin/Wasm（2.x 渐稳），面向浏览器 WASM 运行时。
- `iosArm64()`、`macosArm64()`、`mingwX64()`、`linuxX64()` 等：Kotlin/Native 产物为静态/动态库或可执行文件，无需任何虚拟机。启用任一 Native 目标时 Gradle 会自动下载对应 Kotlin/Native 工具链（约 1GB），演示项目为保持轻量默认注释掉了这些声明。

## 快速验证

```bash
./gradlew build          # 编译 jvm + js，并运行 commonTest 在两个平台上的全部测试
./gradlew jvmTest jsNodeTest   # 只跑测试
./gradlew runDemo        # 运行 JVM 演示程序，打印所有特性输出
```
