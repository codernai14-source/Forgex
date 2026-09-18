# SSE 推送使用方式

> 分类：后端 / 配置与审计
> 版本：**V1.0.0**

## 源码与接入点

- 后端连接：`Forgex_Sys/.../SysMessageSseController.java` 的 `GET /message/stream`。
- 后端发送：注入 `SseEmitterService`，调用 `sendToUser` 或 `sendToTenant`。
- 前端通用封装：`Forgex_Fronted/src/hooks/useSse.ts`，使用浏览器 `EventSource`，自动携带 Cookie（`withCredentials: true`）。

## 基础接入

```ts
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useSse } from '@/hooks/useSse'

const events = ref<Array<{ name: string; data: unknown }>>([])
const connection = useSse({
  // 后端当前真实路径；不要照抄旧的 /sse/connect
  url: '/message/stream',
  autoReconnect: true,
  reconnectDelay: 3000,
  onEvent: (name, data) => events.value.push({ name, data }),
  onError: error => console.error('SSE error', error),
})

onMounted(() => connection.connect())
onBeforeUnmount(() => connection.close())
```

收到的事件至少包括 `connected` 和 `heartbeat`；业务事件由后端发送时使用对应事件名。`useSse` 会解析 JSON，解析失败只回调 `onError`，不会把坏数据交给页面。

## 后端发送示例

```java
@RequiredArgsConstructor
@Service
public class PermissionNoticeService {
    private final SseEmitterService sse;

    public void notifyChanged(Long tenantId, Long userId, String version) {
        sse.sendToUser(tenantId, userId, "permission-changed",
                Map.of("tenantId", tenantId, "version", version));
    }
}
```

向租户广播时使用 `sse.sendToTenant(tenantId, "message", payload)`。不要把密码、令牌或完整异常堆栈放进推送数据；连接本身依赖当前登录 Cookie 和租户上下文。

## 高阶场景

**权限变更刷新**：页面监听 `permission-changed` 后重新请求 `/sys/menu/routes`，成功后清理已撤销的标签页；同时保留“刷新权限”按钮作为断线兜底。

**断线重连**：`useSse` 在错误后按 `reconnectDelay` 重连，调用 `close()` 会设置用户主动关闭标志，不再重连。页面销毁时必须关闭连接，避免重复连接。

**消息历史**：需要跨页面共享时可使用 `useSseStore` 的 `subscribe`、`getRecentMessages`，但该 Store 使用另一套事件模型，接入前应统一事件名和连接 URL，避免同时创建两条连接。

## 推荐用法与错误排查

- 推荐一个登录会话只创建一个连接，由 Store 或应用级服务分发事件；组件只订阅，不重复 `connect()`。
- `401/立即断开`：检查 Cookie、租户上下文和登录状态；未登录时后端会返回立即关闭的 emitter。
- `404`：确认使用 `/message/stream`。当前仓库的 `messageNotification.ts` 仍写着 `/sse/connect`，需先修正。
- 连接建立但无事件：检查代理是否关闭缓冲、响应是否为 `text/event-stream`，并查看后端 `SseEmitterService` 日志。
- JSON 解析错误：确保 `data` 是合法 JSON；纯文本事件需要页面自行处理或统一后端序列化。
- 多实例只收到部分消息：当前连接池是进程内内存，需增加 Redis/MQ 转发或启用粘性会话。
