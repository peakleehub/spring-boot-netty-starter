package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;
import site.peaklee.framework.cache.AdapterSessionManager;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketExceptionCapture
 * @since 2023/3/30
 */
public interface SocketExceptionCapture  extends AdapterSessionManager {

    /**
     * 异常捕获的回调
     * @param context 连接上下文
     * @param e 捕获到的异常
     */
    void capture(Session context, Throwable e);
}
