package site.peaklee.framework.utils;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author PeakLee
 * @version 2023
 * @serial ScanUtil
 * @since 2023/3/28
 */
@Slf4j
public class ScanUtil {

    private static class ScanUtil$Instance{
        protected static ScanUtil INSTANCE=new ScanUtil();
    }

    public static ScanUtil getInstance(){
        return ScanUtil$Instance.INSTANCE;
    }


    private static final Map<String, Set<Class<?>>> cache=new HashMap<>();

    public Set<Class<?>> getClasses(String packageName){
        if (cache.containsKey(packageName)){
            return cache.get(packageName);
        }
        Set<Class<?>> classes = new HashSet<>();
        try {
            // 是否循环迭代
            boolean recursive = true;
            // 获取包的名字 并进行替换
            String packageDirName = packageName.replace('.', '/');
            // 定义一个枚举的集合 并进行循环来处理这个目录下的things
            Enumeration<URL> dirs;
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    addClass(classes, filePath, packageName);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                // 如果是一个.class文件 而且不是目录
                                if (name.endsWith(".class") && !entry.isDirectory()) {
                                    // 去掉后面的".class" 获取真正的类名
                                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                                    try {
                                        // 添加到classes
                                        classes.add(Class.forName(packageName + '.' + className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    log.info("protocol :{}",protocol);
                }
            }
            cache.put(packageName, classes);
        }catch (Exception e){
            log.error("scanner package not found:{}", e.getMessage());
        }
        return classes;
    }

    public <A extends Annotation> Set<Class<?>> getAnnotationClasses(String packageName, Class<A> annotationClass) {

        //找用了annotationClass注解的类
        Set<Class<?>> controllers = new HashSet<>();
        Set<Class<?>> clsList = getClasses(packageName);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                if (cls.getAnnotation(annotationClass) != null) {
                    controllers.add(cls);
                }
            }
        }
        return controllers;
    }

    public Set<Class<?>> getInterfaceClasses(String packageName, Class<?> interfaceClass) {
        Set<Class<?>> controllers = new HashSet<>();
        Set<Class<?>> clsList = getClasses(packageName);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                if (interfaceClass.isAssignableFrom(cls)) {
                    controllers.add(cls);
                }
            }
        }
        return controllers;
    }

    public <T> Set<Class<T>> getInterfaceTypeClasses(String packageName, Class<T> interfaceClass) {
        Set<Class<T>> controllers = new HashSet<>();
        Set<Class<?>> clsList = getClasses(packageName);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                if (interfaceClass.isAssignableFrom(cls)) {
                    controllers.add((Class<T>) cls);
                }
            }
        }
        return controllers;
    }


    public <A extends Annotation,T> Set<Class<T>> getAnnotationClassesLimitType(String packageName, Class<A> annotationClass,Class<T> resultClass) {
        Set<Class<T>> controllers = new HashSet<>();
        Set<Class<?>> clsList = getClasses(packageName);
        if (clsList != null && clsList.size() > 0) {
            for (Class<?> cls : clsList) {
                if (cls.getAnnotation(annotationClass) != null) {
                    if (resultClass.isAssignableFrom(cls)){
                        controllers.add((Class<T>) cls);
                    }
                }
            }
        }
        return controllers;
    }
    public Set<Class<?>> getAnnotationClassesLimitsType(String packageName, List<Class<? extends Annotation>> annotationClass) {
        Set<Class<?>> controllers = new HashSet<>();
        Set<Class<?>> clsList = getClasses(packageName);
        if (clsList != null && clsList.size() > 0) {
            Set<Class<?>> collect = clsList.stream().filter(clazz -> {
                if (annotationClass != null) {
                    return annotationClass.stream().anyMatch(annot-> clazz.getAnnotation(annot)!=null);
                }
                return false;
            }).collect(Collectors.toSet());
            controllers.addAll(collect);
        }
        return controllers;
    }


    private void addClass(Set<Class<?>> classes, String filePath, String packageName) throws Exception {
        File[] files = new File(filePath).listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
        assert files != null;
        for (File file : files) {
            String fileName = file.getName();
            if (file.isFile()) {
                String classsName = fileName.substring(0, fileName.lastIndexOf("."));
                if (!packageName.isEmpty()) {
                    classsName = packageName + "." + classsName;
                }
                doAddClass(classes, classsName);
            }else if (file.isDirectory()){
                addClass(classes, file.getPath(), packageName+"."+file.getName());
            }

        }
    }

    private void doAddClass(Set<Class<?>> classes, final String classsName) throws Exception {
        ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return super.loadClass(name);
            }
        };
        classes.add(classLoader.loadClass(classsName));
    }

    public void getAllPackages(String packageName){

    }

}
