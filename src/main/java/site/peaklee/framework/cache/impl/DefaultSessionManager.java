package site.peaklee.framework.cache.impl;

import site.peaklee.framework.cache.SessionManager;
import site.peaklee.framework.session.impl.GroupSession;
import site.peaklee.framework.session.MessageCallback;
import site.peaklee.framework.session.MessageGroupCallback;
import site.peaklee.framework.session.impl.Session;
import lombok.extern.slf4j.Slf4j;
import site.peaklee.framework.utils.IOCUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PeakLee
 * @version 2023
 * @serial DefaultChannelManager
 * @since 2023/3/27
 */
@Slf4j
public final class DefaultSessionManager implements SessionManager {

    protected static class DefaultSessionManager$Instance{
        protected static final DefaultSessionManager INSTANCE=new DefaultSessionManager();
    }

    private DefaultSessionManager(){}

    public static SessionManager getInstance(){
        if (IOCUtils.hasBean(SessionManager.class)) {
            return IOCUtils.getBean(SessionManager.class);
        }
        return DefaultSessionManager$Instance.INSTANCE;
    }

    //当前在线客户端的组id
    public static final String GLOBAL_ID;

    static {
        GLOBAL_ID = UUID.randomUUID().toString().replace("-", "");
    }

    //当前在线客户端组
    private static final GroupSession online = GroupSession.create(GLOBAL_ID);

    //当前在线客户端
    private static final Map<Serializable,Session> onlineClient=new ConcurrentHashMap<>();

    //当前在线客户端以ChannelId为key
    private static final Map<Serializable,Session> onlineChannelIdClient=new ConcurrentHashMap<>();

    //当前在线组
    private static final Map<Serializable,GroupSession> onlineGroupClient=new ConcurrentHashMap<>();


    @Override
    public int getClientSize(){
        return onlineClient.size();
    }

    @Override
    public int getGroupClientSize(){
        return onlineGroupClient.size();
    }

    @Override
    public boolean registrySession(Serializable id, Session session) {
        if (id!=null && session!=null){
            online.add(session);
            onlineChannelIdClient.put(session.getSessionId(), session);
            onlineClient.put(id, session);
            return true;
        }
        return false;
    }

    @Override
    public boolean registrySession(Session session) {
        if (session!=null && !onlineClient.containsKey(session.getId())){
            online.add(session);
            onlineChannelIdClient.put(session.getSessionId(), session);
            onlineClient.put(session.getId(), session);
            return true;
        }
        return false;
    }

    @Override
    public Session findSession(Serializable id) {
        if (id!=null && onlineClient.containsKey(id)){
            return onlineClient.get(id);
        }
        return null;
    }

    @Override
    public Session findSessionChannel(Serializable id) {
        if (id!=null && onlineChannelIdClient.containsKey(id)){
            return onlineChannelIdClient.get(id);
        }
        return null;
    }

    @Override
    public Session findSessionInGroup(Serializable groupId, Serializable id) {
        if (groupId!=null && id!=null && onlineGroupClient.containsKey(groupId)){
            if (onlineGroupClient.get(groupId).hasSession(id))
            return onlineGroupClient.get(groupId).findSession(id);
        }
        return null;
    }

    @Override
    public GroupSession findGroupSession(Serializable id) {
        if (id!=null && onlineGroupClient.containsKey(id)){
            return onlineGroupClient.get(id);
        }
        return null;
    }

    @Override
    public boolean addSession(Serializable groupId, Session session) {
        if (groupId!=null && session!=null){
            GroupSession groupSession = onlineGroupClient.get(groupId);
            if (groupSession!=null){
                groupSession.add(session);
                return true;
            }
        }
        return false;
    }

    @Override
    public Session removeSession(Serializable groupId, Serializable id) {
        if (groupId!=null && id!=null){
            GroupSession groupSession = onlineGroupClient.get(groupId);
            if (groupSession!=null){
                return groupSession.remove(id);
            }
        }
        return null;
    }

    @Override
    public GroupSession createGroup(Serializable id) {
        if (id!=null && !onlineGroupClient.containsKey(id)){
            GroupSession groupSession = GroupSession.create(id);
            onlineGroupClient.put(id,groupSession);
            return groupSession;
        }
        return onlineGroupClient.get(id);
    }

    @Override
    public GroupSession removeGroup(Serializable groupId) {
        return onlineGroupClient.remove(groupId);
    }

    @Override
    public Session deregisterSession(Serializable id) {
        if (id!=null){
            Session remove = onlineClient.remove(id);
            online.remove(id);
            onlineChannelIdClient.remove(remove.getSessionId());
            groupClientClean(id);
            remove.setInactiveTime(new Date());
            return remove;
        }
        return null;
    }

    @Override
    public void sendSession(Serializable sendId, Object msg, MessageCallback callback) {
        if (sendId!=null && onlineClient.containsKey(sendId)){
            Session session = onlineClient.get(sendId);
            session.send(msg, callback);
        }
    }

    @Override
    public void sendSession(Serializable sendId, Object msg) {
        this.sendSession(sendId,msg,null);
    }

    @Override
    public void sendGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback, Session... exclude) {
        if (groupId!=null){
            GroupSession groupSession = onlineGroupClient.get(groupId);
            groupSession.sendBroadcast(msg, callback, exclude);
        }
    }

    @Override
    public void sendGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback) {
        this.sendGroupBroadcast(groupId, msg,callback,new Session[0]);
    }

    @Override
    public void sendGroupBroadcast(Serializable groupId, Object msg, Session... exclude) {
        this.sendGroupBroadcast(groupId, msg,null,exclude);
    }

    @Override
    public void sendGroupBroadcast(Serializable groupId, Object msg) {
        this.sendGroupBroadcast(groupId, msg,null,new Session[0]);
    }

    @Override
    public void sendSomeGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback, Serializable... some) {
        if (groupId!=null) {
            GroupSession groupSession = onlineGroupClient.get(groupId);
            groupSession.sendSome(msg, callback, some);
        }
    }

    @Override
    public void sendSomeGroupBroadcast(Serializable groupId, Object msg, Serializable... some) {
        this.sendSomeGroupBroadcast(groupId, msg, null, some);
    }

    @Override
    public void sendBroadcast(Object msg, MessageGroupCallback callback, Session... exclude) {
        online.sendBroadcast(msg, callback, exclude);
    }

    @Override
    public void sendBroadcast(Object msg, MessageGroupCallback callback) {
        this.sendBroadcast(msg,callback,new Session[0]);
    }

    @Override
    public void sendBroadcast(Object msg, Session... exclude) {
        this.sendBroadcast(msg,null,exclude);
    }

    @Override
    public void sendBroadcast(Object msg) {
        this.sendBroadcast(msg, null,new Session[0]);
    }

    @Override
    public void sendSomeBroadcast(Object msg, MessageGroupCallback callback, Serializable... some) {
        online.sendSome(msg,callback,some);
    }

    @Override
    public void sendSomeBroadcast(Object msg, Serializable... some) {
        this.sendSomeBroadcast(msg,null,some);
    }


    private synchronized void groupClientClean(Serializable id){
        for (Map.Entry<Serializable, GroupSession> entry : onlineGroupClient.entrySet()) {
            entry.getValue().remove(id);
        }
    }
}
