package site.peaklee.framework.session;

import site.peaklee.framework.session.impl.ApplicationSession;

/**
 * @author PeakLee
 * @version 2023
 * @serial MessageCallback
 * @since 2023/3/28
 */
public interface MessageServerCallback {
    void success(ApplicationSession session, Object msg);
    void failed(ApplicationSession session, Object msg);
}
