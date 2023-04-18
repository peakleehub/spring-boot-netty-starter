package site.peaklee.framework.handler.impl;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

/**
 * @author PeakLee
 * @version 2023
 * @serial WebSocketFrameEncoderHandler
 * @since 2023/3/29
 */
@Slf4j
@ChannelHandler.Sharable
public final class WebSocketProtocFrameEncoderHandler extends MessageToMessageEncoder<MessageLiteOrBuilder> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
        ByteBuf result = null;
        if (msg instanceof MessageLite) {
            result = wrappedBuffer(((MessageLite) msg).toByteArray());
        }
        if (msg instanceof MessageLite.Builder) {
            result = wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray());
        }
        if (result!=null){
            WebSocketFrame frame = new BinaryWebSocketFrame(result);
            out.add(frame);
        }
    }
}
