package site.peaklee.framework.core.spi;

import site.peaklee.framework.server.Server;
import site.peaklee.framework.session.impl.ApplicationSession;

/**
 * @author PeakLee
 * @version 2023
 * @serial ApplicationDestroy
 * @since 2023/4/3
 */
public interface ApplicationDestroy {

    void beforeDestroy(ApplicationSession serverSession);

    void destroy(Server server);
}
