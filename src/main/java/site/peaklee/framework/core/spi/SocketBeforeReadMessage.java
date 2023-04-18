package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial PreSocketReadMessage
 * @since 2023/3/30
 */
public interface SocketBeforeReadMessage extends AdapterSessionManager {

    /**
     * 读取消息前的回调
     * @param ctx 上下文
     * @param msg 消息
     */
    void beforeReadMessage(Session ctx, Object msg);
}
