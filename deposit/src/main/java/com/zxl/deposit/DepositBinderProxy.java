package com.zxl.deposit;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.zxl.deposit.utils.BundleUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

/**
 * Binder代理;
 * 接口类 变成Binder代理(proxy)，主要是通过此类做的包装;
 *
 * @author long117long@126.com <br/>
 * @date 2020/5/23 <br/>
 */
public class DepositBinderProxy<T> implements InvocationHandler {

    protected Context context;
    protected Class clz;
    private IDeposit deposit;
    protected T proxy;

    public DepositBinderProxy(Context context, Class<T> clz, IDeposit deposit) {
        this.context = context;
        this.clz = clz;
        this.deposit = deposit;
        linkToDeath(deposit.asBinder());
    }

    /**
     * 产生代理
     *
     * @return
     */
    public T genProxy() {
        if (proxy != null) {
            return proxy;
        }
        if (context == null || clz == null) {
            throw new NullPointerException("Param is null!");
        }
        if (!clz.isInterface()) {
            return null;
        }
        ClassLoader classLoader = clz.getClassLoader();
        proxy = (T) Proxy.newProxyInstance(classLoader, new Class[]{clz}, this);
        return proxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws RemoteException, InvocationTargetException, IllegalAccessException {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }

        Object[] objArgs = convertArgs(method, args);

        Bundle bundle = new Bundle();
        bundle.putString(ParamKeyworlds.KEY_String_methodName, method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        ArrayList<String> paramList = new ArrayList<>();
        int i = 0;
        for (Class clz : parameterTypes) {
            String key = clz.getName() + "#" + i;
            paramList.add(key);
            BundleUtils.putIntoBundle(bundle, key, objArgs[i]);
            i++;
        }
        bundle.putStringArrayList(ParamKeyworlds.KEY_StringArrayList_paramList, paramList);
        IDeposit deposit = getDeposit();
        if (deposit == null) {
            return null;
        }
        Bundle result = deposit.call(bundle);
        if (result == null) {
            return null;
        }
        result.setClassLoader(method.getReturnType().getClassLoader());
        if (result.getInt(ParamKeyworlds.KEY_int_ret) == 0) {
            if (result.containsKey(ParamKeyworlds.KEY_result_null)) {
                return null;
            }
            return result.get(ParamKeyworlds.KEY_result);
        } else {
            return null;
        }
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
                    DepositBinderService depositBinder = new DepositBinderService(context, args[i], method.getParameterTypes()[i]);
                    result[i] = depositBinder;
                    break;
                }
            }
        }
        return result;
    }

    protected IDeposit getDeposit() {
        return deposit;
    }

    private void linkToDeath(final IBinder binder) {
        try {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    deposit = null;
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
