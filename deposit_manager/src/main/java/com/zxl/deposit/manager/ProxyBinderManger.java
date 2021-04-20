package com.zxl.deposit.manager;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Pair;

import com.zxl.deposit.DepositBinderService;

import java.util.HashMap;
import java.util.Map;

/**
 * 此类的主要功能是 代理Binder的管理类。
 * 具有以下几个接口功能：
 * 1.设置 代理Binder；
 * 2.获取 代理Binder；
 * 此类为管理类，因此需要为单例；
 *
 * @author long117long@126.com <br/>
 * @date 2020/1/6 <br/>
 */
public class ProxyBinderManger {

    private static ProxyBinderManger instance;
    private Context context;

    private ProxyBinderManger(Context context) {
        this.context = context;
    }


    /**
     * 存放代理的map。
     * key：代理binder的名称；
     * value：代理binder
     */
    private HashMap<String, IBinder> depositProcessBinderMap = new HashMap<>();

    public static ProxyBinderManger getInstance(Context context) {
        if (instance == null) {
            synchronized (ProxyBinderManger.class) {
                if (instance == null) {
                    instance = new ProxyBinderManger(context);
                }
            }
        }
        return instance;
    }


    /**
     * 处理注册；
     * 注册过来的是直接可用的代理Binder的名称，以及对应的代理Binder;
     * 注册代理binder，并缓存起来；
     *
     * @param proxyBinderName
     * @param deposit
     * @return
     */
    public int registerByProxyBinder(String proxyBinderName, IBinder deposit) {
        boolean equals = (Binder.getCallingUid() == Process.myUid());
        if (!equals) {
            return -1;
        }

        linkToDeath(deposit);
        depositProcessBinderMap.put(proxyBinderName, deposit);
        return 0;
    }

    /**
     * 处理注册；
     * 注册过来的是直接可用的代理Binder的名称，以及对应的代理Binder;
     * 注册代理binder，并缓存起来；
     *
     * @return
     */
    public Pair<Integer, String> registerByProxyBinder(Object obj, Class interfaceClass) {
        if (obj == null) {
            throw new NullPointerException("obj is null!");
        }
        boolean equals = (Binder.getCallingUid() == Process.myUid());
        if (!equals) {
            return Pair.create(-1, null);
        }
        DepositBinderService deposit = new DepositBinderService(context, obj, interfaceClass);
        linkToDeath(deposit);
        String serviceName = deposit.getServiceName();
        depositProcessBinderMap.put(serviceName, deposit);
        return Pair.create(0, serviceName);
    }


    /**
     * 反注册
     *
     * @param proxyBinderName
     * @return
     */
    public int unregisterProxyService(String proxyBinderName) {
        depositProcessBinderMap.remove(proxyBinderName);
        return 0;
    }

    /**
     * 根据接口类和服务名获取代理binder
     *
     * @return
     */
    public Pair<Integer, IBinder> getDepositBinder(String proxyBinderName) {
        if (TextUtils.isEmpty(proxyBinderName)) {
            return Pair.create(-1, null);
        }

        //这一步拿到的binder并不是interfaceClassName和serviceName对应的binder，
        //而是能够创建 interfaceClassName和serviceName 服务的 ProcessDepositBinder
        IBinder iBinder = depositProcessBinderMap.get(proxyBinderName);
        if (iBinder == null) {
            return Pair.create(-2, null);
        }

        return Pair.create(0, iBinder);
    }


    private void linkToDeath(final IBinder binder) {
        try {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    for (Map.Entry<String, IBinder> entry : depositProcessBinderMap.entrySet()) {
                        if (entry.getValue() == binder) {
                            depositProcessBinderMap.remove(entry.getKey());
                        }
                    }
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
