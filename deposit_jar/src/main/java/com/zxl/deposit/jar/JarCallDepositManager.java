package com.zxl.deposit.jar;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Pair;

import com.zxl.deposit.IDeposit;
import com.zxl.deposit.ParamKeyworlds;


/**
 * @author long117long@126.com <br/>
 * @date 2020/5/23 <br/>
 */
class JarCallDepositManager {
    private static final String TAG = "DepositManagerTag";
    private Context context;
    private IDeposit deposit;
    private String pkgName;

    private JarCallDepositManager(Context context, String pkgName) {
        this.context = context;
        this.pkgName = pkgName;
    }

    /**
     * 创建代理;
     * 如果用此方法，表示使用的服务 是在 context.getPackageName 对应的应用中
     *
     * @param context
     * @return
     */
    public static JarCallDepositManager create(Context context) {
        return JarCallDepositManager.create(context, context.getPackageName());
    }

    /**
     * 创建代理
     *
     * @param context 不能为空
     * @param pkgName 要使用的服务所在的应用的包名。如果为空，则用 context.getPackageName 对应的包名。
     * @return
     */
    public static JarCallDepositManager create(Context context, String pkgName) {
        if (context == null) {
            throw new NullPointerException("Context is null!");
        }
        if (TextUtils.isEmpty(pkgName)) {
            pkgName = context.getPackageName();
        }
        return new JarCallDepositManager(context, pkgName);
    }

    /**
     * 注册服务
     *
     * @param serviceName
     * @param binder
     * @return
     */
    public int registerService(String serviceName, IDeposit binder) {
        Pair<Integer, IDeposit> pair = getProviderBinder();
        if (pair.first != 0) {
            return pair.first;
        }

        IDeposit deposit = pair.second;
        Bundle bundle = new Bundle();
        bundle.putString(ParamKeyworlds.KEY_String_methodName, ParamKeyworlds.KEY_METHOD_registerService);
        bundle.putString(ParamKeyworlds.KEY_String_serviceName, serviceName);
        bundle.putBinder(ParamKeyworlds.KEY_Binder_ProcessBinder, binder.asBinder());
        Bundle result = null;
        try {
            result = deposit.call(bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (result != null) {
            int ret = result.getInt(ParamKeyworlds.KEY_int_ret);
            return ret;
        }
        return -1;
    }

    /**
     * 停止提供服务
     *
     * @param serviceName
     * @return
     */
    public int unregisterService(String serviceName) {
        Pair<Integer, IDeposit> pair = getProviderBinder();
        if (pair.first != 0) {
            return pair.first;
        }

        IDeposit deposit = pair.second;
        Bundle bundle = new Bundle();
        bundle.putString(ParamKeyworlds.KEY_String_methodName, ParamKeyworlds.KEY_METHOD_unregisterService);
        bundle.putString(ParamKeyworlds.KEY_String_serviceName, serviceName);
        Bundle result = null;
        try {
            result = deposit.call(bundle);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (result != null) {
            int ret = result.getInt(ParamKeyworlds.KEY_int_ret);
            return ret;
        }
        return -1;
    }

    /**
     * 获取服务代理
     *
     * @param serviceName
     * @return
     */
    public Pair<Integer, IBinder> getServiceProxy(String serviceName) {
        Pair<Integer, IDeposit> pair = getProviderBinder();
        if (pair.first != 0) {
            return Pair.create(pair.first, null);
        }

        IDeposit deposit = pair.second;
        Bundle bundle = new Bundle();
        bundle.putString(ParamKeyworlds.KEY_String_methodName, ParamKeyworlds.KEY_METHOD_getServiceProxy);
        bundle.putString(ParamKeyworlds.KEY_String_serviceName, serviceName);
        try {
            Bundle result = deposit.call(bundle);
            if (result == null) {
                return Pair.create(-1, null);
            }

            int ret = result.getInt(ParamKeyworlds.KEY_int_ret);
            if (ret != 0) {
                return Pair.create(ret, null);
            }
            IBinder binder = result.getBinder(ParamKeyworlds.KEY_Binder_Binder);
            return Pair.create(0, binder);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Pair.create(-1, null);
    }

    private Pair<Integer, IDeposit> getProviderBinder() {
        if (deposit != null) {
            return Pair.create(0, deposit);
        }
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = getUri();
        if (uri == null) {
            return Pair.create(-1, null);
        }
        Bundle result = contentResolver.call(uri, ParamKeyworlds.KEY_METHOD_getDepositProviderBinder, "", null);
        if (result == null) {
            return Pair.create(-1, null);
        }
        int ret = result.getInt(ParamKeyworlds.KEY_int_ret);
        if (ret != 0) {
            return Pair.create(ret, null);
        }
        IBinder binder = result.getBinder(ParamKeyworlds.KEY_Binder_Binder);
        if (binder == null) {
            return Pair.create(-1, null);
        }

        linkToDeath(binder);
        deposit = IDeposit.Stub.asInterface(binder);
        return Pair.create(0, deposit);
    }

    private Uri uri = null;

    private Uri getUri() {
        if (uri != null) {
            return uri;
        }
        uri = Uri.parse("content://" + pkgName + ".BinderDepositProvider");
        return uri;
    }

    private void linkToDeath(IBinder binder) {
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
