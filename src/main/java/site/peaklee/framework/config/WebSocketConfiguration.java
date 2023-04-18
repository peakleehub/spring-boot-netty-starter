package site.peaklee.framework.config;

import lombok.Data;

/**
 * @author PeakLee
 * @version 2023
 * @serial WebScoketConfiguration
 * @since 2023/3/29
 */
@Data
public class WebSocketConfiguration {

    private String webSocketPath = "/";

    private Boolean enableSSL = false;

    private SocketSSL socketSSL = null;

    private Integer maxContentLength = 65535;

    private String subProtocols = null;

    private Boolean allowExtensions = true;

    private Integer maxFrameSize = 10240;

    private Boolean enableCompress = true;
}
