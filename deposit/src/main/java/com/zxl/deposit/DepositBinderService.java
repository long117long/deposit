package com.zxl.deposit;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.zxl.deposit.utils.BundleUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Binder服务;
 * 接口实现类 变成Binder服务（Binder），主要是通过此类做的包装;
 *
 * @author long117long@126.com <br/>
 * @date 2020/5/25 <br/>
 */
public class DepositBinderService extends IDeposit.Stub {
    protected Context context;
    protected Class interfaceClass;
    protected Object obj;
    protected HashMap<String, Method> methodMap = null;

    /**
     * @param context        上下文；不能为空
     * @param obj            实现interfaceClass的服务实现类实例；不能为空;
     * @param interfaceClass obj所要提供的服务接口类;
     *                       <b>需要注意的是，此类中的方法不能有重名（AIDL中定义的方法也不能有重名的）</b>
     */
    public DepositBinderService(Context context, Object obj, Class interfaceClass) {
        if (context == null || interfaceClass == null || obj == null) {
            throw new NullPointerException("Param is null!");
        }
        this.interfaceClass = interfaceClass;
        this.context = context.getApplicationContext();
        if (this.context == null) {
            this.context = context;
        }
        this.obj = obj;
    }

    /**
     * 获取服务的名字
     *
     * @return
     */
    public String getServiceName() {
        String serviceName = "";
        ServiceName annotation = obj.getClass().getAnnotation(ServiceName.class);
        if (annotation != null) {
            serviceName = annotation.value();
            if (TextUtils.isEmpty(serviceName)) {
                serviceName = interfaceClass.getName();
            }
        }
        String reallyServiceName = ParamKeyworlds.getKey(interfaceClass, serviceName);
        return reallyServiceName;
    }

    /**
     * 由此方法，转调obj中的具体实现；
     *
     * @param bundle 从此binder远程代理类传递过来的参数
     * @return
     */
    @Override
    public Bundle call(Bundle bundle) {
        bundle.setClassLoader(this.getClass().getClassLoader());
        if (!bundle.containsKey(ParamKeyworlds.KEY_String_methodName)) {
            return null;
        }
        String methodName = bundle.getString(ParamKeyworlds.KEY_String_methodName);
        Method method = getMethod(methodName);
        if (method == null) {
            return null;
        }
        // 获取参数名列表；在代理端，是按照方法声明参数的顺序获取的
        ArrayList<String> paramList = bundle.getStringArrayList(ParamKeyworlds.KEY_StringArrayList_paramList);
        Object[] objs = null;
        // 如果有参数，则从bundle中取出来根据paramList中的顺序取出来
        if (paramList != null && paramList.size() != 0) {
            objs = new Object[paramList.size()];
            int i = 0;
            for (String key : paramList) {
                objs[i++] = bundle.get(key);
            }
        }

        Object[] paramObjs = convertArgs(method, objs);
        try {
            Object methodResult;
            if (paramObjs == null) {
                methodResult = method.invoke(getObj());
            } else {
                methodResult = method.invoke(getObj(), paramObjs);
            }
            Bundle result = new Bundle();
            result.putInt(ParamKeyworlds.KEY_int_ret, 0);
            if (methodResult == null) {
                result.putString(ParamKeyworlds.KEY_result_null, null);
            } else {
                BundleUtils.putIntoBundle(result, ParamKeyworlds.KEY_result, methodResult);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据方法名获取方法
     *
     * @param methodName
     * @return
     */
    private Method getMethod(String methodName) {
        HashMap<String, Method> methods = getMethods();
        return methods.get(methodName);
    }

    /**
     * 获取{@link #interfaceClass}类中所有的方法；
     *
     * @return
     */
    private HashMap<String, Method> getMethods() {
        if (methodMap != null) {
            return methodMap;
        }
        methodMap = new HashMap<>();
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            methodMap.put(method.getName(), method);
        }
        return methodMap;
    }

    protected Object getObj() {
        return obj;
    }

    /**
     * 参数转换
     *
     * @param method
     * @param args
     * @return
     */
    protected Object[] convertArgs(Method method, Object[] args) {
        if (args == null) {
            return null;
        }
        Object[] result = new Object[args.length];
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            result[i] = args[i];
            for (int j = 0; j < parameterAnnotations[i].length; j++) {
                Annotation annotation = parameterAnnotations[i][j];
                if (annotation == null) {
                    continue;
                }

                String name = annotation.toString();
                if (name.indexOf(ParamDeposit.class.getName()) > 0) {
                    if (args[j] == null) {
                        continue;
                    }
                    Class<?> parameterType = method.getParameterTypes()[i];
                    if (!parameterType.isInterface()) {
                        throw new IllegalArgumentException(method.getName() + " " + parameterType.getName() + "  is not interface!");
                    }
                    Object proxy = create(context, args[i], parameterType);
                    result[i] = proxy;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 从client端 传过来的 参数中有@ParamDeposit，已在client端转成了BinderProxy，
     * 而BinderProxy 转换成 参数对应的实例，
     * 这个map就是用来存储这两者的对应关系，
     * 防止每次调用接口，参数实例没有变化，而却new一个DepositClientBinder的问题。
     */
    static HashMap<String, Object> argsObjProxyMap = new HashMap<>();

    static Object create(Context context, Object object, Class<?> parameterType) {
        Object proxy = argsObjProxyMap.get(object.toString());
        if (proxy == null) {
            IDeposit deposit = IDeposit.Stub.asInterface((IBinder) object);
            DepositBinderProxy depositParamProxy = new DepositBinderProxy(context, parameterType, deposit);
            proxy = depositParamProxy.genProxy();
            argsObjProxyMap.put(object.toString(), proxy);
            deathToLink(object);
        }
        return proxy;
    }

    private static void deathToLink(final Object object) {
        if (object instanceof IBinder) {
            try {
                ((IBinder) object).linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        argsObjProxyMap.remove(object.toString());
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}
