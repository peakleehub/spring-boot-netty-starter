package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.config.SocketAutoConfiguration;
import site.peaklee.framework.server.impl.ClientApplication;
import site.peaklee.framework.utils.IOCUtils;

/**
 * @author PeakLee
 * @version 2023
 * @serial ClientRetryHandler
 * @since 2023/4/11
 */
@Slf4j
@ChannelHandler.Sharable
public class ClientRetryHandler extends ChannelInboundHandlerAdapter  {

    private final ClientApplication clientApplication;

    private final SocketAutoConfiguration config;

    public ClientRetryHandler(ClientApplication clientApplication) {
        this.clientApplication = clientApplication;
        this.config = IOCUtils.getConfig()!=null?IOCUtils.getConfig():clientApplication.getContext().getBean(SocketAutoConfiguration.class);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (!clientApplication.getRetry().get()){
            ctx.fireChannelUnregistered();
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (config !=null && config.getClient().getAutoRetry()){
            log.warn("The server connection has been disconnected and will now begin reconnecting... ...");
            this.clientApplication.connect(ctx.channel());
        }else {
            ctx.fireChannelUnregistered();
        }
    }
}
