package site.peaklee.framework.handler.impl;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author PeakLee
 * @version 2023
 * @serial WebSocketFrameEncoderHandler
 * @since 2023/3/29
 */
@Slf4j
@ChannelHandler.Sharable
public final class WebSocketTextFrameEncoderHandler extends MessageToMessageEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, String msg, List<Object> out) throws Exception {
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(msg);
        out.add(textWebSocketFrame);
    }
}
