package site.peaklee.framework.context.impl;

import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import site.peaklee.framework.config.SocketSSL;
import site.peaklee.framework.config.WebSocketConfiguration;
import site.peaklee.framework.core.HandlerBean;
import site.peaklee.framework.enums.ProtoType;
import site.peaklee.framework.handler.impl.WebSocketFrameDecoderHandler;
import site.peaklee.framework.handler.impl.WebSocketProtocFrameEncoderHandler;
import site.peaklee.framework.handler.impl.WebSocketTextFrameEncoderHandler;
import site.peaklee.framework.server.impl.ServerApplication;
import site.peaklee.framework.utils.SSLUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial WebSocketConfigurableContextImpl
 * @since 2023/3/27
 */
@Slf4j
public final class WebSocketConfigurableContextImpl extends ServerApplication {


    @Override
    protected void loadCoderHandler(ChannelPipeline pipeline) {
        WebSocketConfiguration websocket = this.configuration.getServer().getWebsocket();
        if (websocket.getEnableSSL() && websocket.getSocketSSL()==null){
            log.warn("Please configure the relevant properties of SSL while enabling it, otherwise it will not take effect.");
        }
        if (websocket.getEnableSSL() &&websocket.getSocketSSL()!=null){
            try {
                SocketSSL socketSSL = websocket.getSocketSSL();
                SSLContext sslContext = SSLUtil.createSSLContext(socketSSL.getSslType().getCode(), socketSSL.getCertPath(), socketSSL.getPassword());
                SSLEngine sslEngine = sslContext.createSSLEngine();
                sslEngine.setNeedClientAuth(false);
                sslEngine.setUseClientMode(false);
                pipeline.addLast("site.peaklee.framework.sslHandler", new SslHandler(sslEngine));
            }catch (Exception e){
                log.warn("Failed to load the ssl certificate because:{}",e.getMessage());
            }
        }
        pipeline.addLast("site.peaklee.framework.httpCodec", new HttpServerCodec());
        pipeline.addLast("site.peaklee.framework.chunked", new ChunkedWriteHandler());
        pipeline.addLast("site.peaklee.framework.aggregator", new HttpObjectAggregator(websocket.getMaxContentLength()));
        if (websocket.getEnableCompress()){
            pipeline.addLast("site.peaklee.framework.compress",new WebSocketServerCompressionHandler());
        }
        pipeline.addLast("site.peaklee.framework.webSocketServer",new WebSocketServerProtocolHandler(websocket.getWebSocketPath(),websocket.getSubProtocols(),websocket.getAllowExtensions(),websocket.getMaxFrameSize()));

        pipeline.addLast("site.peaklee.framework.frameDecoder", new WebSocketFrameDecoderHandler());

        ProtoType supportProtobuf = this.configuration.getSupportProtobuf();
        switch (supportProtobuf){
            case STRING:
                pipeline.addLast("site.peaklee.framework.frameEncoder", new WebSocketTextFrameEncoderHandler());
                break;
            case PROTOBUF:
                pipeline.addLast("site.peaklee.framework.frameEncoder", new WebSocketProtocFrameEncoderHandler());
                assemblingProtobuf(pipeline);
                break;
            default:
                assembling(pipeline, decoder);
                assembling(pipeline, encoder);
                break;
        }
    }

    public WebSocketConfigurableContextImpl(ConfigurableApplicationContext context, Set<HandlerBean> handlers, Set<String> packages, String[] command) {
        super(context, handlers,packages,command);
    }

}
