package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;
import io.netty.handler.timeout.IdleStateEvent;
import site.peaklee.framework.cache.AdapterSessionManager;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketIdle
 * @since 2023/3/30
 */
public interface SocketIdle extends AdapterSessionManager {

    /**
     * 心跳触发时的回调
     * @param context 连接上下文
     * @param idleStateEvent 心跳状态
     */
    void idle(Session context, IdleStateEvent idleStateEvent);
}
