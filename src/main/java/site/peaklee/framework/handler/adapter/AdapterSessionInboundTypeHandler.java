package site.peaklee.framework.handler.adapter;

import site.peaklee.framework.handler.SessionInboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author PeakLee
 * @version 2023
 * @serial AdapterSessionInboundHandler
 * @since 2023/3/30
 */
public abstract class AdapterSessionInboundTypeHandler<T> extends SimpleChannelInboundHandler<T> implements SessionInboundHandler<T> {


    @Override
    public final boolean acceptInboundMessage(Object msg) throws Exception {
        return super.acceptInboundMessage(msg);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, T t) throws Exception {
        onMessage(findWithCreate(ctx),t);
        ctx.fireChannelRead(t);
    }

    @Override
    public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        onRegistered(findWithCreate(ctx));
        ctx.fireChannelRegistered();
    }

    @Override
    public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        onUnregistered(findWithCreate(ctx));
        ctx.fireChannelUnregistered();
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception {
        onActive(findWithCreate(ctx));
        ctx.fireChannelActive();
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        onInactive(findWithCreate(ctx));
        ctx.fireChannelInactive();
    }

    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        onComplete(findWithCreate(ctx));
        ctx.fireChannelReadComplete();
    }

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            onIdle(findWithCreate(ctx), (IdleStateEvent) evt);
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        onError(findWithCreate(ctx), cause);
        ctx.fireExceptionCaught(cause);
    }
}
