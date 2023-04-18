package site.peaklee.framework.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.peaklee.framework.core.DestroyHandler;
import site.peaklee.framework.server.impl.AnnotationBeanManager;
import site.peaklee.framework.utils.IOCUtils;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketConfiguration
 * @since 2023/4/3
 */
@Configuration
@EnableConfigurationProperties({SocketAutoConfiguration.class})
public class SocketConfiguration {
    @Bean
    public DestroyHandler destroyHandler(){
        return new DestroyHandler();
    }

    @Bean
    public AnnotationBeanManager annotationBeanManager(){
        return new AnnotationBeanManager();
    }

    @Bean
    public IOCUtils iocUtils(){
        return new IOCUtils();
    }
}
