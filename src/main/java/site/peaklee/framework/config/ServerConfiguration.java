package site.peaklee.framework.config;

import lombok.Data;
import site.peaklee.framework.enums.SocketType;

import java.util.concurrent.TimeUnit;

/**
 * @author PeakLee
 * @version 2023
 * @serial ServerConfiguration
 * @since 2023/4/11
 */
@Data
public class ServerConfiguration {

    private SocketType socketType= SocketType.Socket;

    private Boolean enableIdle = true;

    private Integer idleTime = 60;

    private Integer readerIdleTime = 60;

    private Integer writerIdleTime =60;

    private TimeUnit idleUnit = TimeUnit.MINUTES;

    private Integer bossThreads = 1;

    private Integer workThreads = 10;

    private Integer socketBacklog = 128;

    private Boolean socketReuseaddr = false;

    private Boolean socketKeepalive = false;

    private Boolean tcpNoDelay = true;

    private Boolean allowHalfClosure = false;

    private Integer socketLinger = -1;

    private Boolean autoRegisterSession = true;

    private WebSocketConfiguration websocket = null;

    public WebSocketConfiguration getWebsocket() {
        if (websocket==null){
            this.websocket = new WebSocketConfiguration();
        }
        return websocket;
    }
}
