package top.yqingyu.rpc.util;

import top.yqingyu.common.cglib.core.DuplicatesPredicate;

public class RpcUtil {
    /**
     * 倘若该类实现的接口实现了某些接口，类名会选取与该对象全限定名最接近的接口的全限定名
     */
    public static String getClassName(Class<?> aClass) {
        String className = aClass.getName();
        if (aClass.isInterface()) {
            return className;
        }

        Class<?>[] interfaces = aClass.getInterfaces();
        Class<?> interface$ = null;
        String[] classNameS = className.split("[.]");
        int sameMax = 0;

        for (Class<?> anInterface : interfaces) {
            String name = anInterface.getName();
            String[] split = name.split("[.]");
            int min = Math.min(classNameS.length, split.length);
            for (int i = 0; i < min; i++) {
                if (!split[i].equals(classNameS[i])) {
                    break;
                }
                if (i > sameMax) {
                    sameMax = i;
                    interface$ = anInterface;
                }
            }
        }
        if (interface$ != null) {
            className = interface$.getName();
        }
        return className;
    }

    public static ClassLoader getClassLoader(Class<?> c) {
        ClassLoader cl = c.getClassLoader();
        if (cl == null) {
            cl = DuplicatesPredicate.class.getClassLoader();
        }
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        return cl;
    }
}
