# Forgex MOM 主题系统

## 概述

本主题系统基于 Ant Design 6 的 Token 体系设计，提供了完整的浅色/暗色主题支持，并支持系统主题自动跟随。

## 目录结构

```
src/theme/
├── types.ts           # TypeScript 类型定义
├── tokens.ts          # 浅色/暗色主题 Token 配置
├── antdTheme.ts       # Ant Design 主题配置生成器
├── cssVariables.ts    # CSS 变量生成器
├── presets.ts         # 预设主题色盘
├── index.ts           # 统一导出
└── README.md          # 本文档

src/composables/
└── useSystemTheme.ts  # 系统主题检测 Composable
```

## 核心概念

### 1. Theme Token

Theme Token 是主题系统的核心，定义了所有颜色、字体、圆角、阴影等设计元素。

```typescript
interface ThemeTokens {
  // 品牌色
  colorPrimary: string
  colorPrimaryHover: string
  colorPrimaryActive: string
  
  // 文本色
  colorText: string
  colorTextSecondary: string
  colorTextTertiary: string
  
  // 背景色
  colorBgBase: string
  colorBgContainer: string
  colorBgElevated: string
  
  // ... 更多 Token
}
```

### 2. 主题模式

支持三种主题模式：

- `light`: 浅色模式
- `dark`: 暗色模式
- `system`: 跟随系统主题

### 3. CSS 变量

所有 Token 会被转换为 CSS 变量，供样式文件使用：

```css
.my-component {
  background: var(--fx-bg-container);
  color: var(--fx-text-primary);
  border-color: var(--fx-border-color);
}
```

## 使用指南

### 在组件中使用主题

```vue
<script setup lang="ts">
import { useSystemTheme, resolveThemeMode } from '@/composables/useSystemTheme'
import { useAntdTheme } from '@/theme/antdTheme'
import { lightTokens, darkTokens } from '@/theme/tokens'

// 获取系统主题
const { systemTheme } = useSystemTheme()

// 解析实际主题模式
const resolvedMode = computed(() => 
  resolveThemeMode(layoutConfig.value.themeMode, systemTheme.value)
)

// 生成 Ant Design 主题配置
const { computedTheme } = useAntdTheme(layoutConfig, resolvedMode)
</script>

<template>
  <a-config-provider :theme="computedTheme">
    <!-- 你的组件内容 -->
  </a-config-provider>
</template>
```

### 在样式中使用 CSS 变量

```less
.my-component {
  // 背景色
  background: var(--fx-bg-container);
  
  // 文本色
  color: var(--fx-text-primary);
  
  // 边框
  border: 1px solid var(--fx-border-color);
  border-radius: var(--fx-radius);
  
  // 阴影
  box-shadow: var(--fx-shadow);
  
  // Hover 效果
  &:hover {
    background: var(--fx-fill);
    color: var(--fx-primary-hover);
  }
}
```

### 可用的 CSS 变量

#### 背景色
- `--fx-bg-base`: 页面背景
- `--fx-bg-container`: 容器背景（卡片、表格）
- `--fx-bg-elevated`: 浮层背景（弹窗、下拉）
- `--fx-bg-layout`: 布局背景

#### 文本色
- `--fx-text-primary`: 主文本
- `--fx-text-secondary`: 次要文本
- `--fx-text-tertiary`: 三级文本
- `--fx-text-disabled`: 禁用文本

#### 边框色
- `--fx-border-color`: 主边框
- `--fx-border-secondary`: 次要边框
- `--fx-split-color`: 分割线

#### 填充色
- `--fx-fill`: 主填充（hover 背景）
- `--fx-fill-secondary`: 次要填充
- `--fx-fill-alter`: 表头背景

#### 主题色
- `--fx-primary`: 主题色
- `--fx-primary-hover`: 主题色 hover
- `--fx-primary-active`: 主题色 active

#### 功能色
- `--fx-success`: 成功色
- `--fx-warning`: 警告色
- `--fx-error`: 错误色
- `--fx-info`: 信息色

#### 字体
- `--fx-font-size`: 基础字号
- `--fx-font-size-lg`: 大号字号
- `--fx-font-size-sm`: 小号字号
- `--fx-font-family`: 字体族

#### 圆角
- `--fx-radius`: 基础圆角
- `--fx-radius-lg`: 大圆角
- `--fx-radius-sm`: 小圆角

#### 阴影
- `--fx-shadow`: 基础阴影
- `--fx-shadow-secondary`: 次要阴影

## 自定义主题

### 修改预设主题色

编辑 `src/theme/presets.ts`：

```typescript
export const THEME_PRESETS: ThemePreset[] = [
  {
    name: '我的主题',
    value: '#your-color',
    description: '描述'
  },
  // ... 更多预设
]
```

### 修改 Token 值

编辑 `src/theme/tokens.ts`：

```typescript
export const lightTokens: ThemeTokens = {
  colorPrimary: '#your-color',
  // ... 修改其他 Token
}
```

### 添加新的 CSS 变量

编辑 `src/theme/cssVariables.ts`：

```typescript
export function generateCSSVariables(tokens, layoutConfig) {
  return {
    // ... 现有变量
    '--fx-my-custom-var': 'value',
  }
}
```

## 最佳实践

### 1. 优先使用 CSS 变量

❌ 不推荐：
```css
.my-component {
  background: #ffffff;
  color: #1f2937;
}
```

✅ 推荐：
```css
.my-component {
  background: var(--fx-bg-container);
  color: var(--fx-text-primary);
}
```

### 2. 使用语义化变量名

❌ 不推荐：
```css
.my-component {
  background: var(--fx-bg-base);  /* 页面背景用于组件 */
}
```

✅ 推荐：
```css
.my-component {
  background: var(--fx-bg-container);  /* 容器背景用于组件 */
}
```

### 3. 添加过渡动画

```css
.my-component {
  transition: background-color 0.3s ease, color 0.3s ease;
}
```

### 4. 确保对比度

- 浅色模式：文本与背景对比度 ≥ 4.5:1（WCAG AA）
- 暗色模式：文本与背景对比度 ≥ 7:1（WCAG AAA）

## 调试技巧

### 1. 使用浏览器开发者工具

在 Chrome DevTools 中：
1. 检查元素
2. 查看 Computed 标签
3. 搜索 `--fx-` 查看所有 CSS 变量值

### 2. 实时修改 CSS 变量

在控制台中：
```javascript
document.documentElement.style.setProperty('--fx-primary', '#ff0000')
```

### 3. 查看当前主题 Token

在控制台中：
```javascript
// 查看浅色主题
import { lightTokens } from '@/theme/tokens'
console.log(lightTokens)

// 查看暗色主题
import { darkTokens } from '@/theme/tokens'
console.log(darkTokens)
```

## 常见问题

### Q: 为什么我的组件没有应用主题？

A: 确保：
1. 组件被 `<a-config-provider>` 包裹
2. 使用了正确的 CSS 变量
3. 没有硬编码颜色值覆盖

### Q: 如何支持更多主题模式？

A: 在 `tokens.ts` 中添加新的 Token 配置，然后在 `antdTheme.ts` 中添加对应的逻辑。

### Q: 主题切换时出现闪烁怎么办？

A: 确保添加了过渡动画：
```css
transition: background-color 0.3s ease, color 0.3s ease;
```

## 参考资源

- [Ant Design 主题定制](https://ant.design/docs/react/customize-theme-cn)
- [Vue Vben Admin](https://github.com/vbenjs/vue-vben-admin)
- [WCAG 对比度标准](https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html)
