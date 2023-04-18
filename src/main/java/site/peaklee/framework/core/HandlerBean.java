package site.peaklee.framework.core;

import site.peaklee.framework.enums.HandlerType;
import lombok.Builder;
import lombok.Data;
import site.peaklee.framework.enums.HandlerType;

/**
 * @author PeakLee
 * @version 2023
 * @serial HandlerBean
 * @since 2023/3/28
 */
@Data
@Builder
public class HandlerBean {
    private Class<?> handlerClass;
    private String name;
    private Integer order;
    private HandlerType type;
    private Class<?> innerClass;
}
