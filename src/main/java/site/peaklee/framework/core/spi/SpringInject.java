package site.peaklee.framework.core.spi;

import org.springframework.context.ApplicationContext;

/**
 * @author PeakLee
 * @version 2023
 * @serial SpringInject
 * @since 2023/3/30
 */
public interface SpringInject {
    /**
     * 实现此类将自动加入到spring容器管理中
     * @param context spring容器上下文
     */
    void setContext(ApplicationContext context);
}
