package site.peaklee.framework.cache;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import site.peaklee.framework.cache.impl.DefaultSessionManager;
import site.peaklee.framework.session.MessageCallback;
import site.peaklee.framework.session.MessageGroupCallback;
import site.peaklee.framework.session.impl.GroupSession;
import site.peaklee.framework.session.impl.Session;

import java.io.Serializable;

/**
 * @author PeakLee
 * @version 2023
 * @serial AdapterSessionManager
 * @since 2023/3/30
 */
public interface AdapterSessionManager extends SessionManager{

    SessionManager SESSION_MANAGER= DefaultSessionManager.getInstance();

    @Override
    default boolean registrySession(Serializable id, Session session){
        return SESSION_MANAGER.registrySession(id, session);
    }

    @Override
    default boolean registrySession(Session session){
        return SESSION_MANAGER.registrySession(session);
    }

    @Override
    default boolean addSession(Serializable groupId, Session session){
        return SESSION_MANAGER.addSession(groupId, session);
    }

    @Override
    default Session removeSession(Serializable groupId, Serializable id){
        return SESSION_MANAGER.removeSession(groupId,id);
    }

    @Override
    default GroupSession createGroup(Serializable id){
        return SESSION_MANAGER.createGroup(id);
    }

    @Override
    default GroupSession removeGroup(Serializable groupId){
        return SESSION_MANAGER.removeGroup(groupId);
    }

    @Override
    default Session deregisterSession(Serializable id){
        return SESSION_MANAGER.deregisterSession(id);
    }

    @Override
    default void sendSession(Serializable sendId, Object msg, MessageCallback callback){
        SESSION_MANAGER.sendSession(sendId, msg,callback);
    }

    @Override
    default void sendSession(Serializable sendId, Object msg){
        SESSION_MANAGER.sendSession(sendId, msg);
    }

    @Override
    default void sendGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback, Session... exclude){
        SESSION_MANAGER.sendGroupBroadcast(groupId,msg,callback,exclude);
    }

    @Override
    default void sendGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback){
        SESSION_MANAGER.sendGroupBroadcast(groupId,msg,callback);
    }

    @Override
    default void sendGroupBroadcast(Serializable groupId, Object msg, Session... exclude){
        SESSION_MANAGER.sendGroupBroadcast(groupId,msg,exclude);
    }

    @Override
    default void sendGroupBroadcast(Serializable groupId, Object msg){
        SESSION_MANAGER.sendGroupBroadcast(groupId,msg);
    }

    @Override
    default void sendSomeGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback, Serializable... some){
        SESSION_MANAGER.sendSomeGroupBroadcast(groupId,msg,callback,some);
    }

    @Override
    default void sendSomeGroupBroadcast(Serializable groupId, Object msg, Serializable... some){
        SESSION_MANAGER.sendSomeGroupBroadcast(groupId,msg,some);
    }

    @Override
    default void sendBroadcast(Object msg, MessageGroupCallback callback, Session... exclude){
        SESSION_MANAGER.sendBroadcast(msg,callback,exclude);
    }

    @Override
    default void sendBroadcast(Object msg, MessageGroupCallback callback){
        SESSION_MANAGER.sendBroadcast(msg,callback);
    }

    @Override
    default void sendBroadcast(Object msg, Session... exclude){
        SESSION_MANAGER.sendBroadcast(msg,exclude);
    }

    @Override
    default void sendBroadcast(Object msg){
        SESSION_MANAGER.sendBroadcast(msg);
    }

    @Override
    default void sendSomeBroadcast(Object msg, MessageGroupCallback callback, Serializable... some){
        SESSION_MANAGER.sendSomeBroadcast(msg,callback,some);
    }

    @Override
    default void sendSomeBroadcast(Object msg, Serializable... some){
        SESSION_MANAGER.sendSomeBroadcast(msg,some);
    }

    default Session createSession(Serializable id, ChannelHandlerContext ctx){
        return Session.create(id).bindChannel(ctx.channel());
    }

    @Override
    default Session findSession(Serializable id){
        return SESSION_MANAGER.findSession(id);
    }

    @Override
    default Session findSessionInGroup(Serializable groupId, Serializable id){
        return SESSION_MANAGER.findSessionInGroup(groupId, id);
    }

    @Override
    default Session findSessionChannel(Serializable id){
        return SESSION_MANAGER.findSessionChannel(id);
    }

    @Override
    default GroupSession findGroupSession(Serializable id){
        return SESSION_MANAGER.findGroupSession(id);
    }

    @Override
    default int getClientSize(){
        return SESSION_MANAGER.getClientSize();
    };

    @Override
    default int getGroupClientSize(){
        return SESSION_MANAGER.getGroupClientSize();
    };

    default Session createSession(Serializable id, Channel ctx){
        return Session.create(id).bindChannel(ctx);
    }

    default Session findWithCreate(ChannelHandlerContext ctx){
        ChannelId id = ctx.channel().id();
        Session session = SESSION_MANAGER.findSessionChannel(id);
        if (session==null){
            session = createSession(id.asShortText(), ctx);
        }
        return session;
    }
}
