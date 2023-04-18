package site.peaklee.framework.config;

import lombok.Data;

/**
 * @author PeakLee
 * @version 2023
 * @serial ClientConfiguration
 * @since 2023/4/11
 */
@Data
public class ClientConfiguration {

    private String host = "localhost";

    private Integer threadCount = 10;

    private Integer timeOut = 15000;

    private Boolean autoRetry = true;

    private Integer retryCount = -1;
}
