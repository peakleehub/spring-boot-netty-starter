package site.peaklee.framework.session.impl;

import site.peaklee.framework.session.MessageServerCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial ServerSession
 * @since 2023/3/28
 */
@Slf4j
public class ApplicationSession {
    private transient final Channel channel;

    private transient final ChannelFuture channelFuture;

    private ApplicationSession(Channel channel, ChannelFuture channelFuture) {
        this.channel = channel;
        this.channelFuture = channelFuture;
    }

    public void addListener(Consumer<Future<?>> callback){
        this.channelFuture.addListener(callback::accept);
    }


    public static ApplicationSession create(Channel channel, ChannelFuture channelFuture){
        return new ApplicationSession(channel, channelFuture);
    }

    public void send(Object msg, MessageServerCallback callback){
        try {
            if (Objects.isNull(msg) || msg.toString().isEmpty()){
                log.warn("send message failed,Message cannot be empty");
                callback.failed(this, null);
                return;
            }
            ChannelFuture channelFuture = this.channel.writeAndFlush(msg);
            channelFuture.addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    log.debug("send message successfully");
                    if (callback!=null){
                        callback.success(this,msg);
                    }
                }else {
                    log.debug("send message failed");
                    if (callback!=null){
                        callback.failed(this,msg);
                    }
                }
            });
        }catch (Exception e){
            log.error("send message failed:{}", e.getMessage());
        }
    }



    public void send(Object msg){
        this.send(msg, null);
    }


    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public Boolean isActive(){
        return this.channel.isActive();
    }

    public void close(){
        if (channel!=null){
            this.channel.close();
        }
        if (channel!=null){
            this.channel.closeFuture();
        }
    }
}
