package com.zxl.deposit.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Pair;

import com.zxl.deposit.IDeposit;
import com.zxl.deposit.ParamKeyworlds;

/**
 * @author long117long@126.com <br/>
 * @date 2020/1/6 <br/>
 */
public class ProviderBinder extends IDeposit.Stub {

    private static ProviderBinder instance;
    private Context context;

    private ProviderBinder(Context context) {
        this.context = context;
    }

    public static ProviderBinder getInstance(Context context) {
        if (instance == null) {
            synchronized (ProviderBinder.class) {
                if (instance == null) {
                    instance = new ProviderBinder(context);
                }
            }
        }
        return instance;
    }

    /**
     * 被进程调用
     *
     * @param bundle
     * @return
     */
    @Override
    public Bundle call(Bundle bundle) {
        String method = bundle.getString(ParamKeyworlds.KEY_String_methodName);
        if (ParamKeyworlds.KEY_METHOD_registerService.equals(method)) {
            return registerService(bundle);
        } else if (ParamKeyworlds.KEY_METHOD_getServiceProxy.equals(method)) {
            return getDepositBinder(bundle);
        } else if (ParamKeyworlds.KEY_METHOD_unregisterService.equals(method)) {
            unregisterService(bundle);
        }
        return null;
    }

    private Bundle registerService(Bundle bundle) {
        String key = bundle.getString(ParamKeyworlds.KEY_String_serviceName);
        IBinder deposit = bundle.getBinder(ParamKeyworlds.KEY_Binder_ProcessBinder);
        int ret = ProxyBinderManger.getInstance(context).registerByProxyBinder(key, deposit);
        Bundle result = new Bundle();
        result.putInt(ParamKeyworlds.KEY_int_ret, ret);
        return result;
    }

    private Bundle unregisterService(Bundle bundle) {
        String key = bundle.getString(ParamKeyworlds.KEY_String_serviceName);
        int ret = ProxyBinderManger.getInstance(context).unregisterProxyService(key);
        Bundle result = new Bundle();
        result.putInt(ParamKeyworlds.KEY_int_ret, ret);
        return result;
    }

    /**
     * 被{@link BinderDepositProvider}调用
     *
     * @return
     */
    public Bundle getDepositBinder(Bundle bundle) {
        String serviceName = bundle.getString(ParamKeyworlds.KEY_String_serviceName);
        Pair<Integer, IBinder> pair = ProxyBinderManger.getInstance(context).getDepositBinder(serviceName);
        Bundle result = new Bundle();
        result.putInt(ParamKeyworlds.KEY_int_ret, pair.first);
        if (pair.first == 0) {
            result.putBinder(ParamKeyworlds.KEY_Binder_Binder, pair.second);
        }
        return result;
    }
}
