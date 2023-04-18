package site.peaklee.framework.pojo;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author PeakLee
 * @version 2023
 * @serial MethodPojo
 * @since 2023/4/10
 */
@Data
@Builder
public class MethodProxy {
    private Method method;
    private Object target;
}
