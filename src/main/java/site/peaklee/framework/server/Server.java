package site.peaklee.framework.server;

import site.peaklee.framework.enums.HandlerEvent;
import site.peaklee.framework.pojo.HandlerCallback;
import site.peaklee.framework.session.impl.ApplicationSession;
import io.netty.channel.ChannelFuture;

import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial Server
 * @since 2023/3/28
 */
public interface Server {

    void addHandlerListener(HandlerEvent handlerEvent, Consumer<HandlerCallback> callback);

    void successful(Consumer<ChannelFuture> callback);

    void closed(Consumer<Server> callback);

    void closeBefore(Consumer<ApplicationSession> callback);

    void destroy();
}
