package site.peaklee.framework.utils;

import site.peaklee.framework.pojo.MethodProxy;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PeakLee
 * @version 2022
 * @serial ClassUtils
 * @since 2022/6/26
 */
public class ClsUtils {
    private static final Map<String, Class<?>> BASIC_CLASS=new ConcurrentHashMap<>(10);

    private static final Map<String,Set<Field>> FILED_CACHE = new HashMap<>();

    private static final Map<String,Set<Method>> METHODS_CACHE = new HashMap<>();


    static {
        BASIC_CLASS.put(Boolean.class.getName(), Boolean.class);
        BASIC_CLASS.put(Byte.class.getName(), Byte.class);
        BASIC_CLASS.put(Character.class.getName(), Character.class);
        BASIC_CLASS.put(Double.class.getName(), Double.class);
        BASIC_CLASS.put(Float.class.getName(), Float.class);
        BASIC_CLASS.put(Integer.class.getName(), Integer.class);
        BASIC_CLASS.put(Long.class.getName(), Long.class);
        BASIC_CLASS.put(Short.class.getName(), Short.class);
        BASIC_CLASS.put(String.class.getName(), String.class);
        BASIC_CLASS.put(Void.class.getName(), Void.class);

        BASIC_CLASS.put(boolean.class.getName(), Boolean.class);
        BASIC_CLASS.put(byte.class.getName(), Byte.class);
        BASIC_CLASS.put(char.class.getName(), Character.class);
        BASIC_CLASS.put(double.class.getName(), Double.class);
        BASIC_CLASS.put(float.class.getName(), Float.class);
        BASIC_CLASS.put(int.class.getName(), Integer.class);
        BASIC_CLASS.put(long.class.getName(), Long.class);
        BASIC_CLASS.put(short.class.getName(), Short.class);
        BASIC_CLASS.put(void.class.getName(), Void.class);

    }

    public static Boolean isPrimitive(Class<?> clazz){
        if (clazz == null){
            return true;
        }
        return clazz.isPrimitive() || BASIC_CLASS.containsKey(clazz.getName());
    }

    public static Set<Field> getAllFields(Class<?> clazz){
        if (clazz!=null && FILED_CACHE.containsKey(clazz.getName())){
            return FILED_CACHE.get(clazz.getName());
        }
        Set<Field> result=new HashSet<>();
        if (Objects.isNull(clazz) || clazz.isInterface() || isPrimitive(clazz) || clazz.isAnnotation()){
            return result;
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        if (declaredFields.length>0){
            for (Field declaredField : declaredFields) {
                if (Modifier.isStatic(declaredField.getModifiers()) ||
                        Modifier.isFinal(declaredField.getModifiers()) ||
                Modifier.isAbstract(declaredField.getModifiers()) ||
                Modifier.isVolatile(declaredField.getModifiers()) ||
                Modifier.isSynchronized(declaredField.getModifiers()) ||
                Modifier.isTransient(declaredField.getModifiers())){
                    continue;
                }
                result.add(declaredField);
            }
        }
        if (clazz.isEnum()){
            return result;
        }
        result.addAll(getAllFields(clazz.getSuperclass()));
        FILED_CACHE.put(clazz.getName(), result);
        return result;
    }
    public static Set<Method> getMethods( Class<?> clazz){
        if (clazz!=null && METHODS_CACHE.containsKey(clazz.getName())){
            return METHODS_CACHE.get(clazz.getName());
        }
        Set<Method> result=new HashSet<>();
        if (Objects.isNull(clazz) || isPrimitive(clazz) || clazz.isAnnotation()){
            return result;
        }
        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length>0){
            for (Method method : methods) {
                if (Modifier.isNative(method.getModifiers())||
                Modifier.isStatic(method.getModifiers())){
                    continue;
                }
                result.add(method);
            }
        }
        METHODS_CACHE.put(clazz.getName(),result);
        return result;
    }

    public static Set<Method> matchMethodsAnnotation(Set<Method> methods,Class<? extends Annotation> annotation){
        Set<Method> result =new HashSet<>();
        if (Objects.isNull(methods) || methods.isEmpty()|| Objects.isNull(annotation)){
            return result;
        }
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)){
                result.add(method);
            }
        }
        return result;
    }

    public static Set<MethodProxy> matchMethodProxyAnnotation(Object target,Set<Method> methods, Class<? extends Annotation> annotation){
        Set<MethodProxy> result =new HashSet<>();
        if (Objects.isNull(methods) || methods.isEmpty()|| Objects.isNull(annotation)){
            return result;
        }
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)){
                result.add(MethodProxy.builder().method(method).target(target).build());
            }
        }
        return result;
    }

    public static Set<MethodProxy> matchMethodProxyAnnotation(Object target,Class<?> clazz,Class<? extends Annotation> annotation){
        return matchMethodProxyAnnotation(target,getMethods(clazz), annotation);
    }


    public static Set<Method> matchMethodsAnnotation(Class<?> clazz,Class<? extends Annotation> annotation){
        return matchMethodsAnnotation(getMethods(clazz), annotation);
    }


    public static Set<Field> matchFieldsAnnotation(Class<?> clazz, Class<? extends Annotation> annotation){
        return matchFieldsAnnotation(getAllFields(clazz),annotation);
    }



    public static Set<Field> matchFieldsAnnotation(Set<Field> fields, Class<? extends Annotation> annotation){
        Set<Field> result=new HashSet<>();
        if (Objects.isNull(fields) || fields.isEmpty() || Objects.isNull(annotation)){
            return result;
        }
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotation)){
                result.add(field);
            }
        }
        return result;
    }


    public static Object string2Primitive(Field target, String val){
        target.setAccessible(true);
        if (Collection.class.isAssignableFrom(target.getType())&& val.contains(",")){
            String[] split = val.split(",");
            try {
                Type genericType = target.getGenericType();
                Class<?> type;
                if (genericType instanceof ParameterizedType){
                    Type rawType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                    type = Class.forName(rawType.getTypeName());
                }else if (genericType instanceof TypeVariable){
                    String s = ((TypeVariable<?>) genericType).getGenericDeclaration().toString();
                    type = Class.forName(s);
                }else {
                    type = Class.forName(genericType.getTypeName());
                }
                Collection<Object> o= instanceCollection(target.getType(),split.length);
                for (String s : split) {
                    o.add(convertPrimitive(type, s));
                }
                return o;
            }catch (Exception e){
                System.out.println(e.getMessage());
                return val;
            }
        }
        if (target.getType().isArray() && val.contains(",")){
            String[] split = val.split(",");
            Object[] instance = (Object[]) Array.newInstance(target.getType().getComponentType(), split.length);
            for (int i = 0; i < split.length; i++) {
                Object item = convertPrimitive(target.getType().getComponentType(), split[i]);
                if (item!=null && item.getClass().equals(target.getType().getComponentType())){
                    instance[i]= item;
                }
            }
            return instance;
        }
        return convertPrimitive(target.getType(), val);
    }

    private static Collection<Object> instanceCollection(Class<?> clazz,int size) throws InstantiationException, IllegalAccessException {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())){
            if (Set.class.isAssignableFrom(clazz)){
                return new HashSet<>();
            }else if (List.class.isAssignableFrom(clazz)){
                return new ArrayList<>();
            }else if (Deque.class.isAssignableFrom(clazz)){
                return new ArrayDeque<>();
            }else if (Queue.class.isAssignableFrom(clazz)){
                return new ArrayBlockingQueue<>(size);
            }else if (Stack.class.isAssignableFrom(clazz)){
                return new Stack<>();
            }else {
                return new ArrayList<>();
            }
        }else {
            return (Collection<Object>) clazz.newInstance();
        }
    }

    private static Object convertPrimitive(Class<?> target,String val){
        if (target.isEnum()){
            try {
                MethodType methodType = MethodType.methodType(target, String.class);
                MethodHandle valueOf = MethodHandles.lookup().findStatic(target, "valueOf", methodType);
                return valueOf.invoke(val);
            }catch (Throwable e){
                System.out.println(e.getMessage());
                return val;
            }
        }
        if (Integer.class.equals(target) || int.class.equals(target)) {
            return Integer.parseInt(val);
        }else if (Double.class.equals(target) || double.class.equals(target)){
            return Double.parseDouble(val);
        }else if (Float.class.equals(target) || float.class.equals(target)){
            return Float.parseFloat(val);
        }else if (Long.class.equals(target) || long.class.equals(target)){
            return Long.parseLong(val);
        }else if (Short.class.equals(target) || short.class.equals(target)){
            return Short.parseShort(val);
        }else if (Byte.class.equals(target) || byte.class.equals(target)){
            return Byte.parseByte(val);
        }else {
            return val;
        }
    }
}
