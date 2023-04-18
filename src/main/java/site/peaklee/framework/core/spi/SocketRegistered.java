package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;
import site.peaklee.framework.cache.AdapterSessionManager;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketRegistered
 * @since 2023/3/30
 */
public interface SocketRegistered extends AdapterSessionManager {

    /**
     * 连接注册时的回调
     * @param context 连接上下文
     */
    void registered(Session context);
}
