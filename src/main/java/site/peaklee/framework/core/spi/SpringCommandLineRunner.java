package site.peaklee.framework.core.spi;

import org.springframework.core.Ordered;
import site.peaklee.framework.context.SocketConfigurableContext;
import org.springframework.boot.CommandLineRunner;

/**
 * @author PeakLee
 * @version 2023
 * @serial SpringCommandLineRunner
 * @since 2023/4/2
 */
public interface SpringCommandLineRunner extends CommandLineRunner, Ordered {

    @Override
    default int getOrder(){
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    default void run(String... args) {}

    void run(SocketConfigurableContext context) throws Exception;
}
