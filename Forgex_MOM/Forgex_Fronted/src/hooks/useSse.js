import { onUnmounted, ref } from 'vue';
export function useSse(options) {
    const source = ref(null);
    function connect() {
        if (source.value)
            return;
        const es = new EventSource(options.url, { withCredentials: true });
        source.value = es;
        es.addEventListener('connected', e => {
            options.onEvent?.('connected', e.data);
        });
        es.addEventListener('message', e => {
            try {
                const raw = e.data;
                const data = typeof raw === 'string' ? JSON.parse(raw) : raw;
                options.onEvent?.('message', data);
            }
            catch (err) {
                options.onError?.(err);
            }
        });
        es.onerror = e => {
            options.onError?.(e);
        };
    }
    function close() {
        if (!source.value)
            return;
        try {
            source.value.close();
        }
        catch (e) {
        }
        finally {
            source.value = null;
        }
    }
    onUnmounted(() => close());
    return { source, connect, close };
}
