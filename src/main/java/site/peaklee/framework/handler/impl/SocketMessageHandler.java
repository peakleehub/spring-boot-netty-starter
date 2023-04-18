package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.annotation.handler.OnMessage;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.core.spi.SocketMessage;
import site.peaklee.framework.enums.HandlerEvent;
import site.peaklee.framework.handler.AnnotationProxy;
import site.peaklee.framework.handler.HandlerInitProxy;
import site.peaklee.framework.handler.ListenerBroadcast;
import site.peaklee.framework.handler.ThreadLocalInboundHandlerAdapter;
import site.peaklee.framework.pojo.HandlerCallback;
import site.peaklee.framework.pojo.MethodProxy;
import site.peaklee.framework.server.impl.AnnotationBeanManager;
import site.peaklee.framework.server.impl.SpiBeanManager;
import site.peaklee.framework.session.impl.Session;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketMessageHandler
 * @since 2023/4/10
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketMessageHandler extends ThreadLocalInboundHandlerAdapter implements AdapterSessionManager, AnnotationProxy, ListenerBroadcast, HandlerInitProxy {
    private Set<SocketMessage> socketMessages;
    private final Set<MethodProxy> messageMethodProxy;
    private final Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener;

    public SocketMessageHandler(Set<String> packages, AnnotationBeanManager annotationBeanManager, Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener){
        super(packages);
        this.messageMethodProxy = annotationBeanManager.getCache(OnMessage.class);
        this.handlerListener = handlerListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Session withCreate = findWithCreate(ctx);
        if (null!=socketMessages && !socketMessages.isEmpty()){
            for (SocketMessage message : socketMessages) {
                log.debug("Execute message method:{}",message.getClass());
                message.readMessage(withCreate, msg);
            }
        }
        broadcastNotification(HandlerEvent.Message,HandlerCallback.builder()
                .session(withCreate)
                .msg(msg)
                .build());
        if (messageMethodProxy!=null && !messageMethodProxy.isEmpty()){
            for (MethodProxy proxy : messageMethodProxy) {
                invoke(proxy, withCreate,msg);
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void error(Method method, Exception e) {
        log.error("Cannot call method with name {} because: {}",method.getName(),e.getMessage());
    }

    @Override
    public Map<HandlerEvent, List<Consumer<HandlerCallback>>> getCallback() {
        return this.handlerListener;
    }

    @Override
    public void init() {
        SpiBeanManager spiBeanManager = getLocal();
        this.socketMessages = spiBeanManager.getCache(SocketMessage.class);
    }
}
