package site.peaklee.framework.server.impl;

import com.google.protobuf.MessageLite;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.core.HandlerBean;
import site.peaklee.framework.core.SpringSocketApplication;
import site.peaklee.framework.enums.SocketType;

import java.util.Set;
import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketApplication
 * @since 2023/3/27
 */
@Slf4j
public abstract class ServerApplication extends SocketApplication {

    private final EventLoopGroup boosGroup;

    private final EventLoopGroup workGroup;

    private final ServerBootstrap bootstrap;

    protected ServerApplication(ConfigurableApplicationContext context, Set<HandlerBean> handlers, Set<String> packages, String[] command) {
        super(context,handlers,packages,command);
        this.boosGroup = new NioEventLoopGroup(configuration.getServer().getBossThreads(), new DefaultThreadFactory(String.format(THREAD_NAME_TEP,configuration.getPort())));
        this.workGroup = new NioEventLoopGroup(configuration.getServer().getWorkThreads(),new DefaultThreadFactory(String.format(THREAD_NAME_TEP,configuration.getPort())));
        this.bootstrap = new ServerBootstrap();
        this.initBootstrap();
        this.initHandler();
        this.startApplication();
    }



    protected void initBootstrap(){
        this.bootstrap.group(this.boosGroup, this.workGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, this.configuration.getServer().getSocketBacklog())
                .option(ChannelOption.SO_REUSEADDR, this.configuration.getServer().getSocketReuseaddr())
                .childOption(ChannelOption.SO_LINGER, this.configuration.getServer().getSocketLinger())
                .childOption(ChannelOption.SO_KEEPALIVE, this.configuration.getServer().getSocketKeepalive())
                .childOption(ChannelOption.TCP_NODELAY, this.configuration.getServer().getTcpNoDelay())
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, this.configuration.getServer().getAllowHalfClosure());
    }


    protected void initHandler(){
        this.bootstrap.childHandler(new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                initializerChannel(pipeline);
            }
        });
    }

    protected synchronized void start(Consumer<ChannelFuture> callback){
        ChannelFuture bind = this.bootstrap.bind(this.configuration.getPort());
        bindStartListener(bind, callback);
    }

    @Override
    protected void startSuccessful(String format) {
        log.info("Started socket server in {} seconds (Socket Running Listener port for {})",format,this.configuration.getPort());
        SpringSocketApplication.global_watch.start("RunningApplication");
    }

    @Override
    protected void startFailed(Channel channelFuture) {
        log.error("Socket server fail bind to {}", this.configuration.getPort());
        System.exit(0);
    }

    @Override
    protected void startError(Channel channel,Throwable e) {
        log.error("Socket server fail {}", e.getMessage());
        System.exit(0);
    }

    @Override
    protected void loadProtocCoder(ChannelPipeline pipeline, MessageLite instance) {
        pipeline.addLast("site.peaklee.framework.protobufDecoder", new ProtobufDecoder(instance));
        if (configuration.getServer().getSocketType() == SocketType.Socket){
            pipeline.addLast("site.peaklee.framework.protobufEncoder", new ProtobufEncoder());
        }
    }

    @Override
    protected void closeBootstrap() {
        this.boosGroup.shutdownGracefully();
        this.workGroup.shutdownGracefully();
    }

    @Override
    protected void loadFirstHandler(ChannelPipeline pipeline) {}

    @Override
    protected void loadLastHandler(ChannelPipeline pipeline) {}
}
