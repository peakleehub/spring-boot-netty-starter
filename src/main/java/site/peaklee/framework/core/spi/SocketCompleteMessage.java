package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial CompleteSocketMessage
 * @since 2023/3/30
 */
public interface SocketCompleteMessage extends AdapterSessionManager {
    /**
     * 读取消息完成的回调
     * @param ctx 上下文
     */
    void completeReadMessage(Session ctx);
}
