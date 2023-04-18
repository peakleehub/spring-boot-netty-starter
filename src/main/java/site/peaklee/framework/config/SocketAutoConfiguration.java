package site.peaklee.framework.config;

import io.netty.handler.logging.LogLevel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import site.peaklee.framework.enums.AppType;
import site.peaklee.framework.enums.ProtoType;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketAutoConfiguration
 * @since 2023/3/27
 */

@Data
@ConfigurationProperties(prefix = "socket")
public class SocketAutoConfiguration {

    private AppType applicationType = AppType.SERVER;

    private Integer port=7080;

    private Boolean enableLog = false;

    private LogLevel logLevel = LogLevel.INFO;

    private ProtoType supportProtobuf = ProtoType.STRING;

    private ServerConfiguration server;

    private ClientConfiguration client;

    private Boolean enableMonitor = Boolean.FALSE;

    public ServerConfiguration getServer() {
        if (this.server==null) {
            this.server = new ServerConfiguration();
        }
        return this.server;
    }

    public ClientConfiguration getClient() {
        if (this.client==null) {
            this.client = new ClientConfiguration();
        }
        return this.client;
    }
}
