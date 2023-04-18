package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.enums.HandlerEvent;
import site.peaklee.framework.handler.ListenerBroadcast;
import site.peaklee.framework.pojo.HandlerCallback;
import site.peaklee.framework.session.impl.Session;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketListenerHandler
 * @since 2023/3/31
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketListenerHandler extends ChannelInboundHandlerAdapter implements AdapterSessionManager, ListenerBroadcast {

    private final Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener;

    public SocketListenerHandler(Map<HandlerEvent, List<Consumer<HandlerCallback>>> handlerListener) {
        this.handlerListener = handlerListener;
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        broadcastNotification(HandlerEvent.Registered,HandlerCallback.builder()
                .session(withCreate)
                .build());
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        broadcastNotification(HandlerEvent.UnRegistered,HandlerCallback.builder()
                .session(withCreate)
                .build());
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        broadcastNotification(HandlerEvent.Active,HandlerCallback.builder()
                .session(withCreate)
                .build());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        broadcastNotification(HandlerEvent.Inactive,HandlerCallback.builder()
                .session(withCreate)
                .build());
        ctx.fireChannelInactive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        Session withCreate = findWithCreate(ctx);
        broadcastNotification(HandlerEvent.Complete,HandlerCallback.builder()
                .session(withCreate)
                .build());
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent){
            Session withCreate = findWithCreate(ctx);
            broadcastNotification(HandlerEvent.Idle,HandlerCallback.builder()
                    .session(withCreate)
                    .idleState((IdleStateEvent) evt)
                    .build());
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Session withCreate = findWithCreate(ctx);
        broadcastNotification(HandlerEvent.Error,HandlerCallback.builder()
                .session(withCreate)
                .cause(cause)
                .build());
        ctx.fireExceptionCaught(cause);
    }


    @Override
    public Map<HandlerEvent, List<Consumer<HandlerCallback>>> getCallback() {
        return this.handlerListener;
    }

}
