package site.peaklee.framework.config;

import site.peaklee.framework.enums.SSLType;
import lombok.Data;
import site.peaklee.framework.enums.SSLType;

/**
 * @author PeakLee
 * @version 2023
 * @serial SocketSSL
 * @since 2023/3/27
 */
@Data
public class SocketSSL {

    private String certPath;

    private String password;

    private SSLType sslType = SSLType.PKCS12;
}
