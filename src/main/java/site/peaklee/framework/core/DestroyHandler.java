package site.peaklee.framework.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import site.peaklee.framework.server.Server;
import site.peaklee.framework.utils.IOCUtils;

import javax.annotation.PreDestroy;

/**
 * @author PeakLee
 * @version 2023
 * @serial DestroyHandler
 * @since 2023/3/29
 */
@Slf4j
public final class DestroyHandler implements ApplicationContextAware, Ordered {

    private ApplicationContext applicationContext;


    @PreDestroy
    public void destroyHandler(){
        if (IOCUtils.hasBean("site.peaklee.framework.socketServer")) {
            Server bean = this.applicationContext.getBean("site.peaklee.framework.socketServer", Server.class);
            bean.destroy();
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
