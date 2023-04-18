package site.peaklee.framework.core.spi;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;
import site.peaklee.framework.cache.AdapterSessionManager;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketRemoveSession
 * @since 2023/3/31
 */
public interface SocketRegisteredSession extends AdapterSessionManager {

    void registerBefore(Session session);

    void registered(Session session);
}
