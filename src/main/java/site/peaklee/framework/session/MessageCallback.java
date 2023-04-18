package site.peaklee.framework.session;

import site.peaklee.framework.session.impl.Session;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial MessageCallback
 * @since 2023/3/28
 */
public interface MessageCallback {
    void success(Session session, Object msg);
    void failed(Session session,Object msg);
}
