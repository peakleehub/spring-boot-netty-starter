package site.peaklee.framework.cache;

import site.peaklee.framework.session.impl.GroupSession;
import site.peaklee.framework.session.impl.Session;

import java.io.Serializable;

/**
 * @author PeakLee
 * @version 2023
 * @serial SessionFind
 * @since 2023/3/31
 */
public interface SessionFind {

    /**
     * 在当前在线的客户端中查找某个session
     * @param id sessionId
     * @return null或Session
     */
    Session findSession(Serializable id);

    /**
     * 在当前在线的客户端中查找某个session
     * @param id sessionChannelId
     * @return null或Session
     */
    Session findSessionChannel(Serializable id);

    /**
     * 在当前在线的组中查询某个客户端session
     * @param groupId 组id
     * @param id id
     * @return null或Session
     */
    Session findSessionInGroup(Serializable groupId,Serializable id);

    /**
     * 在当前在线的组中查找某个GroupSession
     * @param id GroupSessionId
     * @return null或GroupSession
     */
    GroupSession findGroupSession(Serializable id);

}
