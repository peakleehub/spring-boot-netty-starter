package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.core.spi.*;
import site.peaklee.framework.handler.HandlerInitProxy;
import site.peaklee.framework.handler.ThreadLocalInboundHandlerAdapter;
import site.peaklee.framework.server.impl.SpiBeanManager;
import site.peaklee.framework.session.impl.Session;

import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketSessionHandler
 * @since 2023/3/30
 */
@Slf4j
@ChannelHandler.Sharable
public final class SocketInterceptHandler extends ThreadLocalInboundHandlerAdapter implements AdapterSessionManager, HandlerInitProxy {

    private  Set<SocketRegistered> registeredSet=null;

    private  Set<SocketUnregistered> unregisteredSet=null;

    private  Set<SocketActive> activeSet=null;

    private  Set<SocketInactive> inactiveSet=null;

    private  Set<SocketIdle> socketIdleSet=null;

    private  Set<SocketExceptionCapture> socketExceptionCaptureSet=null;

    private  Set<SocketCompleteMessage> completeSocketMessageSet=null;

    public SocketInterceptHandler(Set<String> packages) {
        super(packages);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        if (registeredSet!=null && !registeredSet.isEmpty()){
            Session withCreate = findWithCreate(ctx);
            for (SocketRegistered socketRegistered : registeredSet) {
                log.debug("Execute registration method:{}",socketRegistered.getClass());
                socketRegistered.registered(withCreate);
            }
        }
        ctx.fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        if (unregisteredSet!=null && !unregisteredSet.isEmpty()){
            Session withCreate = findWithCreate(ctx);
            for (SocketUnregistered socketUnregistered : unregisteredSet) {
                log.debug("Execute socketUnregistered method:{}",socketUnregistered.getClass());
                socketUnregistered.Unregistered(withCreate);
            }
        }
        ctx.fireChannelUnregistered();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (activeSet!=null && !activeSet.isEmpty()){
            Session withCreate = findWithCreate(ctx);
            for (SocketActive active : activeSet) {
                log.debug("Execute active method:{}",active.getClass());
                active.active(withCreate);
            }
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (inactiveSet!=null && !inactiveSet.isEmpty()){
            Session withCreate = findWithCreate(ctx);
            for (SocketInactive inactive : inactiveSet) {
                log.debug("Execute active method:{}",inactive.getClass());
                inactive.inactive(withCreate);
            }
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        if (completeSocketMessageSet!=null && !completeSocketMessageSet.isEmpty()){
            Session withCreate = findWithCreate(ctx);
            for (SocketCompleteMessage completeSocketMessage : completeSocketMessageSet) {
                log.debug("Execute complete message method:{}",completeSocketMessage.getClass());
                completeSocketMessage.completeReadMessage(withCreate);
            }
        }
        ctx.fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent){
            if (socketIdleSet!=null && !socketIdleSet.isEmpty()){
                Session withCreate = findWithCreate(ctx);
                for (SocketIdle socketIdle : socketIdleSet) {
                    log.debug("Execute dile method:{}",socketIdle.getClass());
                    socketIdle.idle(withCreate, (IdleStateEvent) evt);
                }
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (socketExceptionCaptureSet!=null && !socketExceptionCaptureSet.isEmpty()){
            Session withCreate = findWithCreate(ctx);
            for (SocketExceptionCapture capture : socketExceptionCaptureSet) {
                log.debug("Execute capture method:{}",capture.getClass());
                capture.capture(withCreate, cause);
            }
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void init() {
        SpiBeanManager instance = getLocal();
        this.registeredSet = instance.getCache(SocketRegistered.class);
        this.unregisteredSet = instance.getCache(SocketUnregistered.class);
        this.activeSet = instance.getCache(SocketActive.class);
        this.inactiveSet = instance.getCache(SocketInactive.class);
        this.socketIdleSet = instance.getCache(SocketIdle.class);
        this.socketExceptionCaptureSet = instance.getCache(SocketExceptionCapture.class);
        this.completeSocketMessageSet = instance.getCache(SocketCompleteMessage.class);
    }
}
