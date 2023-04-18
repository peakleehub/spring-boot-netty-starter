package site.peaklee.framework.factory;

import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.config.SocketAutoConfiguration;
import site.peaklee.framework.context.SocketConfigurableContext;
import site.peaklee.framework.context.impl.SocketConfigurableContextImpl;
import site.peaklee.framework.context.impl.WebSocketConfigurableContextImpl;
import site.peaklee.framework.core.HandlerBean;
import site.peaklee.framework.enums.AppType;
import site.peaklee.framework.enums.SocketType;
import site.peaklee.framework.server.impl.ClientApplication;

import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketConfigurableContextFactory
 * @since 2023/3/27
 */
public class SocketConfigurableContextFactory {

    public static SocketConfigurableContext createContext(ConfigurableApplicationContext springContext,
                                                          Set<HandlerBean> handlers, Set<String> packages, String[] command){
        SocketAutoConfiguration bean = springContext.getBean(SocketAutoConfiguration.class);
        if (bean.getApplicationType().equals(AppType.SERVER)){
            if (bean.getServer().getSocketType() == null){
                throw new NullPointerException("SocketType not null.");
            }
            if (bean.getServer().getSocketType().equals(SocketType.Socket)){
                return new SocketConfigurableContextImpl(springContext,handlers,packages,command);
            }else {
                return new WebSocketConfigurableContextImpl(springContext,handlers,packages,command);
            }
        }else {
            return new ClientApplication(springContext,handlers,packages,command);
        }
    }
}
