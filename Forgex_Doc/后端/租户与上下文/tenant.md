# 多租户

> Alias page for stable navigation in `Forgex_Doc`

本页为导航别名页，完整内容请阅读 [多租户（详细版）](./多租户.md)。

核心覆盖：

- `tenant_id` 行级数据隔离
- `TenantContext` / `UserContext` 请求级上下文
- 忽略租户规则（TABLE / SERVICE / MAPPER）
- 公共配置 `tenant_id = 0` 回退
- 自动填充租户与审计字段
- 异步任务上下文透传
