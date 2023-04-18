package site.peaklee.framework.server.impl;

import io.netty.channel.ChannelHandler;
import site.peaklee.framework.handler.HandlerInitProxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PeakLee
 * @version 2023
 * @serial DefaultHandlerCache
 * @since 2023/4/18
 */
final class DefaultHandlerCacheHelper {
    protected static class DefaultHandlerCacheHelper$Instance{
        protected static final DefaultHandlerCacheHelper INSTANCE = new DefaultHandlerCacheHelper();
    }

    private DefaultHandlerCacheHelper(){};

    private final Map<String, ChannelHandler> handlerCache = new ConcurrentHashMap<>();

    public static DefaultHandlerCacheHelper getInstance(){
        return DefaultHandlerCacheHelper$Instance.INSTANCE;
    }

    public void cache(String name,ChannelHandler object){
        handlerCache.put(name, object);
    }

    public ChannelHandler getCache(String name,ChannelHandler defaultVal){
        ChannelHandler result = defaultVal;
        if (handlerCache.containsKey(name)){
            result = handlerCache.get(name);
        }else {
            cache(name, defaultVal);
        }
        if (HandlerInitProxy.class.isAssignableFrom(result.getClass())){
            ((HandlerInitProxy) result).init();
        }
        return result;
    }

    public void clear(){
        this.handlerCache.clear();
    }
}
