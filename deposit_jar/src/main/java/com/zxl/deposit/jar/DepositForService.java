package com.zxl.deposit.jar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.zxl.deposit.DepositBinderService;
import com.zxl.deposit.ParamKeyworlds;

import java.util.HashMap;
import java.util.Set;

/**
 * 接口服务端代理；
 * 主要提供让服务端使用的接口；
 *
 * @author long117long@126.com <br/>
 * @date 2019/12/24 <br/>
 */
public class DepositForService {
    public static final String TAG = "DepositForServiceTag";
    private static DepositForService instance;

    private Context context;
    /**
     * key: 代理服务key<br>
     * value: {@link DepositBinderService}的实例
     * <p>
     * 这里的缓存，是为了提供服务所在的进程死了之后，再启动起来的时候再次向其注册
     */
    private HashMap<String, DepositBinderService> binderMap = new HashMap<>(8);

    private DepositForService(Context context) {
        this.context = context;
        registerBroadcast();
    }

    public static DepositForService getInstance(Context context) {
        if (instance == null) {
            synchronized (DepositForService.class) {
                if (instance == null) {
                    instance = new DepositForService(context);
                }
            }
        }
        return instance;
    }

    /**
     * 注册
     *
     * @param object         服务类的实例
     * @param interfaceClass 服务接口类
     * @return
     */
    public int register(Object object, Class interfaceClass) {
        DepositBinderService depositBinder = create(object, interfaceClass);
        if (depositBinder == null) {
            return -1;
        }
        String reallyServiceName = depositBinder.getServiceName();
        registerService(reallyServiceName, depositBinder);
        binderMap.put(reallyServiceName, depositBinder);
        return 0;
    }

    /**
     * 创建Binder服务
     *
     * @param object         服务类的实例
     * @param interfaceClass 服务接口类
     * @return
     */
    public DepositBinderService create(Object object, Class interfaceClass) {
        if (object == null || interfaceClass == null) {
            return null;
        }

        boolean isInterface = interfaceClass.isInterface();
        if (!isInterface) {
            return null;
        }

        DepositBinderService depositBinder = new DepositBinderService(context, object, interfaceClass);
        return depositBinder;
    }

    //暂未实现反注册
    //public int unregister(){}

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            registerServiceFromMap();
        }
    };

    /**
     * 注册监听。当服务启动发出广播后，将已之前注册的服务再次向服务进行注册；
     */
    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ParamKeyworlds.KEY_String_startAction);
        context.registerReceiver(receiver, intentFilter);
    }

    /**
     * 从classMap中获取key，向注册
     *
     * @return
     */
    private void registerServiceFromMap() {
        Set<String> keySet = binderMap.keySet();
        for (String key : keySet) {
            registerService(key, binderMap.get(key));
        }
    }


    private int registerService(String serviceName, DepositBinderService binder) {
        JarCallDepositManager manager = getJarCallDepositManager();
        int ret = manager.registerService(serviceName, binder);
        return ret;
    }

    private JarCallDepositManager manager;

    private JarCallDepositManager getJarCallDepositManager() {
        if (manager == null) {
            //这里先只支持 服务只能向自己所在的应用注册服务
            manager = JarCallDepositManager.create(context, context.getPackageName());
        }
        return manager;
    }

}
