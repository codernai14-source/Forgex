# Forgex Mobile Android Skeleton

本目录是按《手机端、PAD 端开发方案》初始化的移动端工程骨架，位于 `Forgex_MOM` 下，与前后端平级。

## 当前已搭建

- 多模块结构：`app`、`core/*`、`feature/*`
- 技术基线：Kotlin + Compose + Hilt + Retrofit + DataStore
- 环境维度：`dev`、`test`、`prod`
- 设备策略：运行时识别 `MOBILE` / `TABLET`
- 登录链路：`/auth/login -> /auth/choose-tenant -> /sys/menu/routes` 已接入（基于网关 `/api` 前缀）

## 模块职责

- `app`：应用入口、导航编排、全局 DI 配置
- `core/common`：通用模型与结果包装
- `core/network`：网络层与拦截器骨架
- `core/datastore`：本地会话持久化（Token/Tenant）
- `core/ui`：设备识别与通用 UI 容器
- `feature/*`：业务模块页面骨架

## 下一步建议

1. 对接验证码流程（`/auth/captcha/image` 或滑块）并完成加密传输（SM2）策略。
2. 将首页菜单从占位按钮切换为后端 `componentKey` 动态驱动。
3. 增加 Gradle Wrapper（本地若已安装 Gradle，可执行 `gradle wrapper` 生成）。
