package top.yqingyu.rpc.consumer;

import top.yqingyu.common.utils.StringUtil;
import top.yqingyu.rpc.Constants;
import top.yqingyu.rpc.annontation.QyRpcProducerProperties;
import top.yqingyu.rpc.util.RpcUtil;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyMetaDataCache {

    final ConcurrentHashMap<Method, QyRpcProducerProperties> methodPropertiesCache = new ConcurrentHashMap<>();
    final ConcurrentHashMap<Method, String> methodNameCache = new ConcurrentHashMap<>();
    final ConcurrentHashMap<Method, Object> specialMethodNameCache = new ConcurrentHashMap<>();
    final Class<?> proxyClass;

    public ProxyMetaDataCache(Class<?> proxyClass) {
        this.proxyClass = proxyClass;
    }

    public String getMethodName(Method method, Object[] param) {

        String methodStrName = methodNameCache.get(method);
        if (StringUtil.isEmpty(methodStrName)) {
            String className = RpcUtil.getClassName(proxyClass);
            String name = method.getName();

            StringBuilder sb = new StringBuilder(className).append(Constants.method).append(name);
            if (param != null) for (Class<?> o : method.getParameterTypes()) {
                sb.append("#").append(o.getName());
            }
            methodStrName = sb.toString();
            methodNameCache.put(method, methodStrName);
        }
        return methodStrName;
    }

    public void putSpecialMethodNameCache(Method method, Object trn) {
        if (trn != null)
            specialMethodNameCache.put(method, trn);
    }
    public Object getSpecialMethodName(Method method) {
        return specialMethodNameCache.get(method);
    }
    public QyRpcProducerProperties getMethodProperties(Method method) {
        QyRpcProducerProperties annotation = methodPropertiesCache.get(method);
        if (annotation == null) {
            annotation = method.getAnnotation(QyRpcProducerProperties.class);
        }
        if (annotation == null) {
            annotation = proxyClass.getAnnotation(QyRpcProducerProperties.class);
        }
        return annotation;
    }
}
