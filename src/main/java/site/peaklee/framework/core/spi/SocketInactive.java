package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketActive
 * @since 2023/3/30
 */
public interface SocketInactive extends AdapterSessionManager {

    /**
     * 客户端下线时的回调
     * @param context 连接上下文
     */
    void inactive(Session context);
}
