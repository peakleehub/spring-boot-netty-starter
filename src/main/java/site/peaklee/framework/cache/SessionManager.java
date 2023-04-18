package site.peaklee.framework.cache;

import site.peaklee.framework.session.impl.GroupSession;
import site.peaklee.framework.session.MessageCallback;
import site.peaklee.framework.session.MessageGroupCallback;
import site.peaklee.framework.session.impl.Session;

import java.io.Serializable;

/**
 * @author PeakLee
 * @version 2023
 * @serial ChannelManager
 * @since 2023/3/27
 */
public interface SessionManager extends SessionFind,SessionStatistics {

    /**
     * 客户端连接之后注册客户端
     * @param id 客户端id
     * @param session 客户端session
     * @return 是否成功
     */
    boolean registrySession(Serializable id, Session session);

    /**
     * 客户端连接之后注册客户端
     * @param session 客户端session
     * @return 是否成功
     */
    boolean registrySession(Session session);

    /**
     * 取消注册
     * @param id 客户端id
     * @return 是否成功
     */
    Session deregisterSession(Serializable id);

    /**
     * 添加session到组
     * @param groupId 组id
     * @param session 客户端session
     * @return 是否成功
     */
    boolean addSession(Serializable groupId, Session session);

    /**
     * 在组中删除客户端session
     * @param groupId 组id
     * @param id 客户端id
     * @return 被删除的session对象
     */
    Session removeSession(Serializable groupId,Serializable id);


    /**
     * 创建一个客户端群组
     * @param id 群组id
     * @return 创建的组
     */
    GroupSession createGroup(Serializable id);

    /**
     * 删除组
     * @param groupId 组id
     * @return 删除组对象
     */
    GroupSession removeGroup(Serializable groupId);

    /**
     * 发送消息
     * @param sendId 发送id
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     */
    void sendSession(Serializable sendId, Object msg, MessageCallback callback);

    /**
     * 发送消息
     * @param sendId 发送id
     * @param msg 消息
     */
    void sendSession(Serializable sendId, Object msg);

    /**
     * 区域广播
     * @param groupId 广播组id
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     * @param exclude 在此组群中排除不需要发送的对象
     */
    void sendGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback,Session... exclude);

    /**
     * 区域广播
     * @param groupId 广播组id
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     */
    void sendGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback);


    /**
     * 区域广播
     * @param groupId 广播组id
     * @param msg 消息
     * @param exclude 在此组群中排除不需要发送的对象
     */
    void sendGroupBroadcast(Serializable groupId, Object msg,Session... exclude);

    /**
     * 区域广播
     * @param groupId 广播组id
     * @param msg 消息
     */
    void sendGroupBroadcast(Serializable groupId, Object msg);


    /**
     * 在特定群组中仅对一部分人区域广播
     * @param groupId 广播组id
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     * @param some 在此组群中需要发送的对象
     */
    void sendSomeGroupBroadcast(Serializable groupId, Object msg, MessageGroupCallback callback,Serializable... some);

    /**
     * 在特定群组中仅对一部分人区域广播
     * @param groupId 广播组id
     * @param msg 消息
     * @param some 在此组群中需要发送的对象
     */
    void sendSomeGroupBroadcast(Serializable groupId, Object msg,Serializable... some);


    /**
     * 全体广播
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     * @param exclude 在此组群中排除不需要发送的对象
     */
    void sendBroadcast(Object msg, MessageGroupCallback callback,Session... exclude);

    /**
     * 全体广播
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     */
    void sendBroadcast(Object msg, MessageGroupCallback callback);


    /**
     * 全体广播
     * @param msg 消息
     * @param exclude 在此组群中排除不需要发送的对象
     */
    void sendBroadcast(Object msg,Session... exclude);

    /**
     * 全体广播
     * @param msg 消息
     */
    void sendBroadcast(Object msg);


    /**
     * 仅对一部分人广播
     * @param msg 消息
     * @param callback 消息发送后的成功回调
     * @param some 需要发送的对象
     */
    void sendSomeBroadcast(Object msg, MessageGroupCallback callback,Serializable... some);

    /**
     * 仅对一部分人广播
     * @param msg 消息
     * @param some 需要发送的对象
     */
    void sendSomeBroadcast(Object msg,Serializable... some);

}
