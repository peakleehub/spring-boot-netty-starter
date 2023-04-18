package site.peaklee.framework.handler;

import site.peaklee.framework.enums.HandlerEvent;
import site.peaklee.framework.pojo.HandlerCallback;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author PeakLee
 * @version 2023
 * @serial ListenerBroadcast
 * @since 2023/4/10
 */
public interface ListenerBroadcast {

    Map<HandlerEvent, List<Consumer<HandlerCallback>>> getCallback();

    default void broadcastNotification(HandlerEvent handlerEvent,HandlerCallback pojo){
        if (getCallback()!=null && !getCallback().isEmpty()){
            List<Consumer<HandlerCallback>> consumers = getCallback().get(handlerEvent);
            if (consumers!=null && !consumers.isEmpty()){
                for (Consumer<HandlerCallback> consumer : consumers) {
                    consumer.accept(pojo);
                }
            }
        }
    }
}
