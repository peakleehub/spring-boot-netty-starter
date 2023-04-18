package site.peaklee.framework.context;

import site.peaklee.framework.cache.SessionManager;
import site.peaklee.framework.cache.impl.DefaultSessionManager;
import site.peaklee.framework.server.Server;
import site.peaklee.framework.session.impl.ApplicationSession;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketConfigurableContext
 * @since 2023/3/27
 */
public interface SocketConfigurableContext extends Server {

    ConfigurableApplicationContext getContext();

    ApplicationSession getApplicationSession();

    String[] getArgs();

    default SessionManager getSessionManager(){
        return DefaultSessionManager.getInstance();
    }
}
