package site.peaklee.framework.pojo;

import site.peaklee.framework.cache.AdapterSessionManager;
import site.peaklee.framework.session.impl.Session;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Builder;
import lombok.Data;

/**
 * @author PeakLee
 * @version 2023
 * @serial HandlerCallback
 * @since 2023/3/31
 */
@Data
@Builder
public class HandlerCallback implements AdapterSessionManager {
    private Session session;
    private Object msg;
    private Throwable cause;
    private IdleStateEvent idleState;
}
