package site.peaklee.framework.cache;

/**
 * @author PeakLee
 * @version 2023
 * @serial SessionStatistics
 * @since 2023/4/17
 */
public interface SessionStatistics {

    /**
     * 获取当前在线用户数量
     * @return 数量
     */
    int getClientSize();

    /**
     * 获取当前在线用户组的数量
     * @return 数量
     */
    int getGroupClientSize();
}
