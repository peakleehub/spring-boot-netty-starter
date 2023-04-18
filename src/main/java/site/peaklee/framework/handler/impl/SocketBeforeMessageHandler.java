package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.annotation.handler.OnBeforeMessage;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.core.spi.SocketBeforeReadMessage;
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
 * @serial SocketBeforeMessageHandler
 * @since 2023/4/10
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketBeforeMessageHandler extends ThreadLocalInboundHandlerAdapter implements AdapterSessionManager , AnnotationProxy, ListenerBroadcast, HandlerInitProxy {
    private Set<SocketBeforeReadMessage> beforeSocketReadMessageSet;
    private final Set<MethodProxy> beforeMessageMethodProxy;
    private final Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener;

    public SocketBeforeMessageHandler(Set<String> packages, AnnotationBeanManager annotationBeanManager,Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener){
        super(packages);
        this.beforeMessageMethodProxy = annotationBeanManager.getCache(OnBeforeMessage.class);
        this.handlerListener = handlerListener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Session withCreate = findWithCreate(ctx);
        if (null!=beforeSocketReadMessageSet && !beforeSocketReadMessageSet.isEmpty()){
            for (SocketBeforeReadMessage beforeSocketReadMessage : beforeSocketReadMessageSet) {
                log.debug("Execute before message method:{}",beforeSocketReadMessage.getClass());
                beforeSocketReadMessage.beforeReadMessage(withCreate, msg);
            }
        }
        broadcastNotification(HandlerEvent.BeforeMessage,HandlerCallback.builder()
                .session(withCreate)
                .msg(msg)
                .build());
        if (beforeMessageMethodProxy!=null && !beforeMessageMethodProxy.isEmpty()){
            for (MethodProxy proxy : beforeMessageMethodProxy) {
                invoke(proxy, withCreate,msg);
            }
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public Map<HandlerEvent, List<Consumer<HandlerCallback>>> getCallback() {
        return this.handlerListener;
    }

    @Override
    public void error(Method method, Exception e) {
        log.error("Cannot call method with name {} because: {}",method.getName(),e.getMessage());
    }

    @Override
    public void init() {
        SpiBeanManager spiBeanManager = getLocal();
        this.beforeSocketReadMessageSet = spiBeanManager.getCache(SocketBeforeReadMessage.class);
    }
}
