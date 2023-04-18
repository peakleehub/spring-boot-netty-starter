package site.peaklee.framework.enums;

/**
 * @author PeakLee
 * @version 2023
 * @serial SSLType
 * @since 2023/3/27
 */
public enum SSLType {
    PKCS12,
    JKS;

    public String getCode(){
        if (this == PKCS12) {
            return "PKCS12";
        }else {
            return "JKS";
        }
    }
}
