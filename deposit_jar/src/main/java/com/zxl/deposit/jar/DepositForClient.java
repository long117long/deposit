package com.zxl.deposit.jar;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.zxl.deposit.DepositBinderProxy;
import com.zxl.deposit.IDeposit;
import com.zxl.deposit.ParamKeyworlds;
import com.zxl.deposit.ServiceName;

import java.util.HashMap;

/**
 * 接口客户端代理；
 * 主要提供让客户端使用的接口；
 *
 * @author long117long@126.com <br/>
 * @date 2019/12/24 <br/>
 */
public class DepositForClient {
    public static final String TAG = "DepositProxy";
    /**
     * 缓存binder的map
     */
    private static HashMap<String, IDeposit> depositMap = new HashMap<>();

    private final Context context;
    private final String pkgName;
    private final Class<?> clz;
    private final String serviceName;

    /**
     * @param context
     * @param pkgName     服务端所在的包名；如果为空，则调用的是context所对应的pkgName
     * @param clz         代理的接口类；
     * @param serviceName 接口所对应的服务名；如果为空，则对应的clz的类名
     */
    private DepositForClient(Context context, String pkgName, Class<?> clz, String serviceName) {
        this.context = context;
        if (TextUtils.isEmpty(pkgName)) {
            this.pkgName = context.getPackageName();
        } else {
            this.pkgName = pkgName;
        }

        this.clz = clz;
        this.serviceName = serviceName;
    }


    /**
     * 建造类
     */
    public static class Builder {
        private String pkgName;
        private String serviceName;
        private Context context;

        /**
         * 设置远程服务所在的apk的包名.<br>
         * <b>注意：如果远程服务和此类是在同一个apk中，此方法可以不用调用。</b>
         *
         * @param pkgName
         * @return
         */
        public Builder setPkgName(String pkgName) {
            this.pkgName = pkgName;
            return this;
        }

        /**
         * 设置在服务实现类使用注解@ServiceName时的名字。<br>
         * <b>如果服务实现类没有使用注解@ServiceName，此方法可以不用调用。</b>
         *
         * @param serviceName
         * @return
         */
        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * 设置Context。<br>
         * <b>此方法必须调用</b>
         *
         * @param context
         * @return
         */
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        /**
         * 创建接口类代理
         *
         * @param clz
         * @return
         */
        public <T> T create(Class<?> clz) {
            if (context == null || clz == null) {
                throw new NullPointerException("Param is null!");
            }
            DepositForClient proxy = new DepositForClient(context, pkgName, clz, serviceName);
            return proxy.genProxy();
        }

        public <T> T create(Class<?> clz, IDeposit binder) {
            if (context == null || clz == null) {
                throw new NullPointerException("Param is null!");
            }
            DepositBinderProxy proxy = new DepositBinderProxy(context, clz, binder);
            return (T) proxy.genProxy();
        }
    }

    /**
     * 产生代理
     *
     * @param <T>
     * @return
     */
    private <T> T genProxy() {
        depositBinder = getDepositBinder(clz, serviceName);
        if (depositBinder == null) {
            return null;
        }
        DepositBinderProxy proxy = new DepositBinderProxy(context, clz, depositBinder);
        return (T) proxy.genProxy();
    }


    private IDeposit depositBinder;

    /**
     * 获取远程服务的代理binder
     *
     * @param clz         远程服务对应的接口类
     * @param serviceName 远程服务名；
     * @return
     */
    private IDeposit getDepositBinder(Class clz, String serviceName) {
        if (TextUtils.isEmpty(serviceName)) {
            ServiceName annotation = (ServiceName) clz.getAnnotation(ServiceName.class);
            if (annotation != null) {
                serviceName = annotation.value();
            }
        }
        if (serviceName == null) {
            serviceName = "";
        }
        String reallyServiceName = ParamKeyworlds.getKey(clz, serviceName);
        IDeposit binder = getDepositBinderFromCache(reallyServiceName);
        if (binder != null) {
            return binder;
        }

        JarCallDepositManager manager = JarCallDepositManager.create(context, pkgName);
        Pair<Integer, IBinder> pair = manager.getServiceProxy(reallyServiceName);
        if (pair.first != 0) {
            return null;
        }
        binder = IDeposit.Stub.asInterface(pair.second);

        cacheDepositBinder(reallyServiceName, binder);
        linkToDeath(binder.asBinder(), reallyServiceName);
        return binder;
    }

    private void linkToDeath(final IBinder binder, final String key) {
        try {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Log.e(TAG, "linkToDeath binder = " + binder.toString());
                    depositMap.remove(key);
                    depositBinder = null;
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存中获取
     *
     * @param key
     * @return
     */
    private IDeposit getDepositBinderFromCache(String key) {
        return depositMap.get(key);
    }

    /**
     * 缓存Binder
     *
     * @param key
     * @param deposit
     */
    private void cacheDepositBinder(String key, IDeposit deposit) {
        depositMap.put(key, deposit);
    }
}
