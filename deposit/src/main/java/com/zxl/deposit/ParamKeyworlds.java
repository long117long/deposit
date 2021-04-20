package com.zxl.deposit;

import java.lang.annotation.Annotation;

/**
 * @author long117long@126.com <br/>
 * @date 2019/12/24 <br/>
 */
public final class ParamKeyworlds {
    public static final String KEY_METHOD_getServiceProxy = "getServiceProxy";
    public static final String KEY_METHOD_registerService = "registerService";
    public static final String KEY_METHOD_unregisterService = "unregisterService";
    public static final String KEY_METHOD_getDepositProviderBinder = "getDepositProviderBinder";
    public static final String KEY_METHOD_remove_remote_proxy = "#_remove_remote_proxy_#";

    public static final String KEY_String_methodName = "methodName";
    public static final String KEY_StringArrayList_paramList = "paramList";
    public static final String KEY_int_ret = "ret";
    public static final String KEY_result = "result";
    public static final String KEY_result_null = "result_null";
    public static final String KEY_String_serviceName = "serviceName";
    public static final String KEY_String_serviceInterfaceClz = "serviceInterfaceClz";
    public static final String KEY_Binder_Binder = "Binder";
    public static final String KEY_Binder_ProcessBinder = "ProcessBinder";
    public static final String KEY_Bundle_extra = "extra";
    public static final String KEY_String_startAction = "com.zxl.binderdeposit.BinderDepositProvider.start";


    /**
     * 产生key
     *
     * @param clz
     * @param serviceName
     * @return
     */
    public static String getKey(Class clz, String serviceName) {
        Annotation annotation = clz.getAnnotation(ServiceClassName.class);
        String clzName;
        if (annotation != null) {
            clzName = ((ServiceClassName) annotation).value();
        } else {
            clzName = clz.getName();
        }
        return getKey(clzName, serviceName);
    }

    /**
     * 产生key
     *
     * @param className
     * @param serviceName
     * @return
     */
    static String getKey(String className, String serviceName) {
        return className + "#" + serviceName;
    }
}
