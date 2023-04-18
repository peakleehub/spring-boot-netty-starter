package site.peaklee.framework.session.impl;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.config.SocketAutoConfiguration;
import site.peaklee.framework.enums.AppType;
import site.peaklee.framework.session.MessageCallback;
import site.peaklee.framework.utils.IOCUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author PeakLee
 * @version 2023
 * @serial Session
 * @since 2023/3/28
 */

@Data
@Slf4j
public class Session {

    private transient Channel channel;

    private Serializable id;

    private String ip = "localhost";

    private ChannelId sessionId;

    private Date activeTime;

    private Date inactiveTime;

    private Session(){}

    public static Session create(Serializable id){
        if (id!=null){
            Session session = new Session();
            session.id = id;
            return session;
        }
        return null;
    }

    public static Session create(Serializable id,Channel channel){
        if (channel!=null && id!=null){
            Session session = new Session();
            session.id = id;
            session.channel = channel;
            session.activeTime = new Date();
            return session;
        }
        return null;
    }

    public Boolean isActive(){
        return this.channel.isActive();
    }

    public void close(){
        try {
            ChannelFuture close = this.channel.closeFuture();
            close.addListener(cf->{
                if (cf.isSuccess()){
                    log.info("The client with current ID {} has been offline.",id);
                }
            });
        }catch (Exception e){
            log.error("An error occurred while closing the client with id {} because:{}",id,e.getMessage());
        }
    }

    public Session bindChannel(Channel channel){
        if (channel!=null){
            this.channel=channel;
            SocketAutoConfiguration config = IOCUtils.getConfig();
            if (config!=null && config.getApplicationType().equals(AppType.CLIENT)){
                this.ip = config.getClient().getHost();
            } else if (channel.remoteAddress()!=null){
                this.ip = channel.remoteAddress().toString();
            } else if (channel.localAddress()!=null){
                this.ip = channel.localAddress().toString();
            }
            this.sessionId = channel.id();
            this.activeTime = new Date();
        }
        return this;
    }

    public void send(Object msg, MessageCallback callback){
        try {
            if (Objects.isNull(msg) || msg.toString().isEmpty()){
                log.warn("send message failed,Message cannot be empty");
                callback.failed(this, null);
                return;
            }
            ChannelFuture channelFuture = this.channel.writeAndFlush(msg);
            channelFuture.addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    log.info("send message successfully to :{}",id);
                    if (callback!=null){
                        callback.success(this,msg);
                    }
                }else {
                    log.warn("send message failed,id:{}.",id);
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


    @Override
    public String toString() {
        return "Session{" +
                "channel=" + channel +
                ", id=" + id +
                ", ip='" + ip + '\'' +
                ", sessionId=" + sessionId +
                ", activeTime=" + activeTime +
                ", inactiveTime=" + inactiveTime +
                '}';
    }
}
