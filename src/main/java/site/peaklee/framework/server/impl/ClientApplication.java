package site.peaklee.framework.server.impl;

import com.google.protobuf.MessageLite;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.core.HandlerBean;
import site.peaklee.framework.core.SpringSocketApplication;
import site.peaklee.framework.handler.impl.ClientRetryHandler;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial ClientApplication
 * @since 2023/4/11
 */
@Slf4j
public final class ClientApplication extends SocketApplication {

    private final EventLoopGroup workGroup;

    private final Bootstrap bootstrap;

    private final AtomicInteger retryCount = new AtomicInteger(0);

    private final AtomicBoolean retry = new AtomicBoolean(false);

    public AtomicBoolean getRetry() {
        return retry;
    }

    public ClientApplication(ConfigurableApplicationContext context, Set<HandlerBean> handlers, Set<String> packages, String[] command) {
        super(context, handlers, packages, command);
        this.workGroup = new NioEventLoopGroup(configuration.getClient().getThreadCount(),new DefaultThreadFactory(String.format(THREAD_NAME_TEP,configuration.getPort())));
        this.bootstrap = new Bootstrap();
        this.initBootstrap();
        this.initHandler();
        this.startApplication();
    }

    @Override
    protected synchronized void start(Consumer<ChannelFuture> callback) {
        ChannelFuture bind = this.bootstrap.connect(this.configuration.getClient().getHost(),this.configuration.getPort());
        bindStartListener(bind, callback);
    }

    @Override
    protected void startSuccessful(String format) {
        retry.set(false);
        retryCount.set(0);
        log.info("Started socket client in {} seconds (Successfully connected to server [{}: {}])",format,this.configuration.getClient().getHost(),this.configuration.getPort());
        SpringSocketApplication.global_watch.start("RunningApplication");
    }

    public synchronized void connect(Channel channel){
        IS_START.set(false);
        retry.set(true);
        Integer maxRetry = configuration.getClient().getRetryCount();
        int i = retryCount.getAndAdd(1);
        if (i>maxRetry && maxRetry != -1){
            log.error("Failed connection to server [{}: {}]",this.configuration.getClient().getHost(),this.configuration.getPort());
            System.exit(0);
        }
        if (configuration.getClient().getAutoRetry()){
            log.error("Attempting to reconnect to server for {} time.: [{}: {}]",i,this.configuration.getClient().getHost(),this.configuration.getPort());
            channel.eventLoop().schedule(this::startApplication
                    ,this.configuration.getClient().getTimeOut(), TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    protected void startFailed(Channel channel) {
        boolean b = retry.get();
        if (!b){
            log.error("Failed connection to server [{}: {}]",this.configuration.getClient().getHost(),this.configuration.getPort());
            System.exit(0);
        }
    }

    @Override
    protected void startError(Channel channel,Throwable e) {
        log.error("Connection server error : {}",e.getMessage());
        System.exit(0);
    }

    @Override
    protected void initBootstrap() {
        this.bootstrap.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,this.configuration.getClient().getTimeOut());
    }

    @Override
    protected void initHandler() {
        this.bootstrap.handler(new ChannelInitializer<SocketChannel>(){
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();
                initializerChannel(pipeline);
            }
        });
    }

    @Override
    protected void loadProtocCoder(ChannelPipeline pipeline, MessageLite instance) {
        pipeline.addLast("site.peaklee.framework.protobufEncoder", new ProtobufEncoder());
        pipeline.addLast("site.peaklee.framework.protobufDecoder", new ProtobufDecoder(instance));
    }

    @Override
    protected void closeBootstrap() {
        this.workGroup.shutdownGracefully();
    }

    @Override
    protected void loadFirstHandler(ChannelPipeline pipeline) {
        pipeline.addLast("site.peaklee.framework.ClientRetryHandler", new ClientRetryHandler(this));
    }

    @Override
    protected void loadLastHandler(ChannelPipeline pipeline) {}
}
