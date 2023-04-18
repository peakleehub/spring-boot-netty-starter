package site.peaklee.framework.utils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * @author PeakLee
 * @version 2023
 * @serial SSLUtil
 * @since 2023/3/29
 */
public class SSLUtil {
    private static volatile SSLContext sslContext = null;
    // type是PKCS12、path是pfx文件路径、password是pfx对应的密码
    public static SSLContext createSSLContext(String type ,String path ,String password) throws Exception {
        if(null == sslContext){
            synchronized (SSLUtil.class) {
                if(null == sslContext){
                    // 支持JKS、PKCS12（我们项目中用的是阿里云免费申请的证书，下载tomcat解压后的pfx文件，对应PKCS12）
                    KeyStore ks = KeyStore.getInstance(type);
                    // 证书存放地址
                    InputStream ksInputStream = new FileInputStream(path);
                    ks.load(ksInputStream, password.toCharArray());
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(ks, password.toCharArray());
                    sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(kmf.getKeyManagers(), null, null);
                }
            }
        }
        return sslContext;
    }
}
