package site.peaklee.framework.session.impl;

import site.peaklee.framework.session.MessageCallback;
import site.peaklee.framework.session.MessageGroupCallback;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PeakLee
 * @version 2023
 * @serial GroupSession
 * @since 2023/3/28
 */
@Data
@Slf4j
public class GroupSession {

    private final Map<Serializable, Session> groupSession = new ConcurrentHashMap<>();
    private final Map<Serializable, Session> channelIdGroupSession = new ConcurrentHashMap<>();

    private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Serializable groupId;

    private GroupSession() {
    }

    public static GroupSession create(Serializable groupId) {
        GroupSession groupSession = new GroupSession();
        groupSession.setGroupId(groupId);
        return groupSession;
    }

    public GroupSession add(Session session) {
        this.groupSession.put(session.getId(), session);
        this.channelGroup.add(session.getChannel());
        this.channelIdGroupSession.put(session.getSessionId(), session);
        return this;
    }

    public Session remove(Serializable id) {
        Session remove = this.groupSession.remove(id);
        if (remove != null) {
            this.channelGroup.remove(remove.getChannel());
            this.channelIdGroupSession.remove(remove.getSessionId());
        }
        return remove;
    }

    public Session findSession(Serializable id){
        return this.groupSession.remove(id);
    }

    public Boolean hasSession(Serializable id){
        return this.groupSession.containsKey(id);
    }

    public void clear() {
        this.groupSession.clear();
        this.channelGroup.clear();
        this.channelIdGroupSession.clear();
    }

    public Integer size(){
        return this.channelGroup.size();
    }

    public void sendBroadcast(Object msg, MessageGroupCallback callback, Session... exclude) {
        try {
            if (Objects.isNull(msg) || msg.toString().isEmpty()){
                log.warn("send message failed,Message cannot be empty");
                callback.failed(this, null,null);
                return;
            }
            ChannelGroupFuture channelFutures = this.channelGroup.writeAndFlush(msg, cm -> {
                if (exclude != null && exclude.length > 0) {
                    for (Session session : exclude) {
                        return session.getSessionId() != cm.id();
                    }
                }
                return true;
            });
            Iterator<ChannelFuture> iterator = channelFutures.iterator();
            List<Session> result = new ArrayList<>();
            while (iterator.hasNext()) {
                ChannelFuture next = iterator.next();
                next.addListener(cf -> {
                    if (!cf.isSuccess()) {
                        Session session = this.channelIdGroupSession.get(next.channel().id());
                        if (session != null) {
                            result.add(session);
                        }
                    }
                });
            }
            processingResults(callback,result,msg);
        } catch (Exception e) {
            log.error("send broadcast message failed:{}", e.getMessage());
        }


    }

    public void sendBroadcast(Object msg,Session... exclude){
        this.sendBroadcast(msg, null, exclude);
    }

    public void sendBroadcast(Object msg){
        this.sendBroadcast(msg, null,new Session[0]);
    }

    public void sendSome(Object msg,MessageGroupCallback callback, Serializable... sendSome){
        try {
            if (Objects.isNull(msg) || msg.toString().isEmpty()){
                log.warn("send message failed,Message cannot be empty");
                callback.failed(this, null,null);
                return;
            }
            if (sendSome!=null && sendSome.length>0){
                List<Session> result=new ArrayList<>();
                for (Serializable id : sendSome) {
                    Session session = this.groupSession.get(id);
                    if (session!=null){
                        session.send(msg, new MessageCallback() {
                            @Override
                            public void success(Session self,Object msg) {

                            }
                            @Override
                            public void failed(Session self,Object msg) {
                                result.add(session);
                            }
                        });
                    }
                }
                processingResults(callback,result,msg);
            }
        }catch (Exception e){
            log.error("send broadcast message failed:{}", e.getMessage());
        }
    }

    public void sendSome(Object msg,Serializable... sendSome){
        this.sendSome(msg, null,sendSome);
    }

    private void processingResults(MessageGroupCallback callback,List<Session> sessions,Object msg){
        if (sessions.isEmpty()) {
            log.info("send broadcast message successfully to group :{}",groupId);
            if (callback != null) {
                callback.success(this,msg);
            }
        } else {
            log.warn("send broadcast message failed,please handle it through the callback function.");
            if (callback != null) {
                callback.failed(this,sessions, msg);
            }
        }
    }
}
