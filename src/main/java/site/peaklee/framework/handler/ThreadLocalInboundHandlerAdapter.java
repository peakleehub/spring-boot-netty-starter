package site.peaklee.framework.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import site.peaklee.framework.server.impl.SpiBeanManager;
import site.peaklee.framework.utils.IOCUtils;

import java.util.Set;

/**
 * @author PeakLee
 * @version 2023
 * @serial ThreadLocalInboundHandlerAdapter
 * @since 2023/4/18
 */
public abstract class ThreadLocalInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
    private final Set<String> packages;
    private final ThreadLocal<SpiBeanManager> threadLocal = new ThreadLocal<>();

    public ThreadLocalInboundHandlerAdapter(Set<String> packages) {
        this.packages = packages;
    }

    protected SpiBeanManager getLocal(){
        SpiBeanManager spiBeanManager = threadLocal.get();
        if (spiBeanManager==null){
            spiBeanManager = SpiBeanManager.initInstance(this.packages, IOCUtils.getContext());
            threadLocal.set(spiBeanManager);
        }
        return spiBeanManager;
    }
}
