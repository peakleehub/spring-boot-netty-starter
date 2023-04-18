package site.peaklee.framework.context.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.core.HandlerBean;
import site.peaklee.framework.server.impl.ServerApplication;

import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketConfigurableContextImpl
 * @since 2023/3/27
 */
@Slf4j
public final class SocketConfigurableContextImpl extends ServerApplication {

    public SocketConfigurableContextImpl(final ConfigurableApplicationContext context, Set<HandlerBean> handlers, Set<String> packages, String[] command) {
        super(context, handlers,packages,command);
    }

}
