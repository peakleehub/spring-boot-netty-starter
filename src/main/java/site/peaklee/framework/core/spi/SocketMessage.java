package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketMessage
 * @since 2023/4/10
 */
public interface SocketMessage extends AdapterSessionManager {

    /**
     * 读取消息的回调
     * @param ctx 上下文
     * @param msg 消息
     */
    void readMessage(Session ctx, Object msg);
}
