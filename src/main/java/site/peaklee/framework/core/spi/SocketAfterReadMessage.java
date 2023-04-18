package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketAfterReadMessage
 * @since 2023/4/10
 */
public interface SocketAfterReadMessage extends AdapterSessionManager {

    /**
     * 读取消息后的回调
     * @param ctx 上下文
     * @param msg 消息
     */
    void afterReadMessage(Session ctx, Object msg) throws Exception;
}
