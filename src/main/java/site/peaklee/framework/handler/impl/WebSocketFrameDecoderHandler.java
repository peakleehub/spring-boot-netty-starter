package site.peaklee.framework.handler.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author PeakLee
 * @version 2023
 * @serial WebSocketFrameDecoderHandler
 * @since 2023/3/29
 */
@Slf4j
@ChannelHandler.Sharable
public final class WebSocketFrameDecoderHandler extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ch, WebSocketFrame frame, List<Object> out) throws Exception {
        log.debug("Client message received....");
        if (frame instanceof TextWebSocketFrame) {
            // 文本消息
            TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
            out.add(textFrame.text());
        } else if (frame instanceof BinaryWebSocketFrame) {
            // 二进制消息
            ByteBuf buf = frame.content();
            out.add(buf);
            // 自旋累加
            buf.retain();
        } else if (frame instanceof PongWebSocketFrame) {
            // PING存活检测消息
            log.debug("Client Pong message received.");
        } else if (frame instanceof CloseWebSocketFrame) {
            // 关闭指令消息
            log.debug("Client close.:{}", frame);
            ch.close();
        }
    }
}
