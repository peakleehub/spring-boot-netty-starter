package site.peaklee.framework.session;

import site.peaklee.framework.session.impl.GroupSession;
import site.peaklee.framework.session.impl.Session;
import site.peaklee.framework.session.impl.GroupSession;
import site.peaklee.framework.session.impl.Session;

import java.util.List;

/**
 * @author PeakLee
 * @version 2023
 * @serial MessageGroupCallback
 * @since 2023/3/28
 */
public interface MessageGroupCallback {
    void success(GroupSession session, Object msg);
    void failed(GroupSession session, List<Session> ids, Object msg);
}
