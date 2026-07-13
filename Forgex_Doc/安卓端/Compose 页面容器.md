# Compose 页面容器

> 分类：安卓端 / 专题
> 版本：V0.8.0
> 关联模块：`core/ui`、`core/designsystem`、`core/device`

本文说明 Forgex Android 端的设备类型识别策略（MOBILE / TABLET）、通用页面容器封装，以及主题 Token 体系。

---

## 一、设备类型识别

`core/device` 模块在运行时根据屏幕最短宽度（smallestScreenWidthDp）识别设备类型，驱动布局策略：

| 设备类型 | 判定条件 | 布局策略 |
|---|---|---|
| `MOBILE` | 最短宽度 < 600dp | 单栏布局，列表/详情分页跳转 |
| `TABLET` | 最短宽度 >= 600dp | 双栏布局（Master-Detail），列表与详情同屏 |

### 1.1 DeviceType 定义

```kotlin
enum class DeviceType { MOBILE, TABLET }
```

### 1.2 识别方式

使用 `WindowWidthSizeClass` 配合 `Configuration.smallestScreenWidthDp` 判定，避免依赖硬编码像素值：

```kotlin
@Composable
fun rememberDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        if (configuration.smallestScreenWidthDp >= 600) {
            DeviceType.TABLET
        } else {
            DeviceType.MOBILE
        }
    }
}
```

- 600dp 阈值与 Material 3 `WindowWidthSizeClass.MEDIUM` 对齐，是业界公认的平板分界线。
- `remember(configuration)` 确保屏幕旋转 / 折叠屏展开时重新计算。

## 二、通用页面容器

`core/ui` 提供一组通用 Composable 容器，feature 页面基于这些容器组装，保证一致的导航栏、状态栏与加载/错误态。

### 2.1 ForgexScaffold

顶层页面骨架，封装 TopAppBar、状态栏沉浸与内容区域：

```kotlin
@Composable
fun ForgexScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    uiState: UiState<*> = UiState.Loading,
    onRetry: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            ForgexTopAppBar(
                title = title,
                onBack = onBack,
                actions = actions,
            )
        },
    ) { padding ->
        when (uiState) {
            is UiState.Loading -> LoadingPlaceholder(padding)
            is UiState.Error -> ErrorPlaceholder(uiState.message, onRetry, padding)
            is UiState.Success -> content(padding)
        }
    }
}
```

### 2.2 容器职责约定

| 容器 | 职责 | 适用场景 |
|---|---|---|
| `ForgexScaffold` | 顶层骨架，TopBar + 内容 + 加载/错误态切换 | 所有带标题栏的页面 |
| `ForgexListPage` | 列表页容器，封装下拉刷新 + 上拉加载 + 空态 | 待办、消息、设备列表 |
| `ForgexDetailPage` | 详情页容器，TABLET 下作为右侧详情栏 | 审批详情、设备详情 |
| `LoadingPlaceholder` | 加载中占位（骨架屏） | 替代裸 ProgressBar |
| `ErrorPlaceholder` | 错误占位 + 重试按钮 | 网络/业务错误 |
| `EmptyPlaceholder` | 空数据占位 | 列表无数据 |

### 2.3 自适应布局示例

feature 页面根据 `DeviceType` 切换布局，但容器本身无感知设备类型：

```kotlin
@Composable
fun WorkflowTodoScreen() {
    val deviceType = rememberDeviceType()
    if (deviceType == DeviceType.TABLET) {
        // 平板：双栏 Master-Detail
        Row {
            WorkflowTodoList(modifier = Modifier.weight(1f))
            WorkflowTodoDetail(modifier = Modifier.weight(1.2f))
        }
    } else {
        // 手机：单栏，详情通过导航跳转
        WorkflowTodoList()
    }
}
```

## 三、主题 Token 体系

`core/designsystem` 模块集中定义主题 Token，所有 feature 禁止硬编码颜色 / 字号 / 间距。

### 3.1 Token 分类

| Token 类别 | 说明 | 示例 |
|---|---|---|
| Color | 语义化颜色（主色、背景、文字、错误、成功等） | `ForgexTheme.color.primary` |
| Typography | 字号 / 字重 / 行高 | `ForgexTheme.typography.titleLarge` |
| Spacing | 间距刻度（4/8/12/16/24/32dp） | `ForgexTheme.spacing.medium` |
| Radius | 圆角刻度 | `ForgexTheme.radius.small` |
| Elevation | 阴影高度 | `ForgexTheme.elevation.card` |

### 3.2 ForgexTheme 入口

```kotlin
@Composable
fun ForgexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = ForgexTypography
    val dimensions = ForgexDimensions.current

    CompositionLocalProvider(
        LocalForgexColorScheme provides colorScheme,
        LocalForgexTypography provides typography,
        LocalForgexDimensions provides dimensions,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}
```

### 3.3 暗色模式

- 跟随系统暗色模式（`isSystemInDarkTheme()`）。
- 暗色配色在 `DarkColorScheme` 中定义，不通过简单反色实现，确保对比度达标。
- 业务图标使用 tint 着色，禁止使用固定颜色的位图。

## 四、规范约定

1. **禁止硬编码样式**：颜色、字号、间距一律走 `ForgexTheme` Token。
2. **容器优先**：feature 页面优先用 `ForgexScaffold` / `ForgexListPage` 等容器，不要从零搭 Scaffold。
3. **设备自适应**：涉及列表+详情的页面必须根据 `DeviceType` 提供平板双栏布局。
4. **状态占位统一**：加载 / 错误 / 空态用统一占位组件，禁止在 feature 内自造样式各异的占位页。
5. **TopAppBar 统一**：返回按钮、标题样式由 `ForgexTopAppBar` 统一，feature 不自定义导航栏外观。
