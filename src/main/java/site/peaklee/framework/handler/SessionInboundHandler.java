package site.peaklee.framework.handler;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;
import io.netty.handler.timeout.IdleStateEvent;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;

/**
 * @author PeakLee
 * @version 2023
 * @serial SessionInboundHandler
 * @since 2023/3/31
 */
public interface SessionInboundHandler<T> extends AdapterSessionManager {

    default void onRegistered(Session session){};

    default void onUnregistered(Session session){};

    default void onActive(Session session){};

    default void onInactive(Session session){};

    void onMessage(Session session,T msg);

    default void onComplete(Session session){};

    default void onIdle(Session session, IdleStateEvent stateEvent){};

    default void onError(Session session,Throwable cause){};

}
