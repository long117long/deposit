package com.zxl.deposit.manager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.zxl.deposit.ParamKeyworlds;

/**
 * @author long117long@126.com <br/>
 * @date 2019/12/24 <br/>
 */
public class BinderDepositProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        sendStartBroadcast();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (ParamKeyworlds.KEY_METHOD_getDepositProviderBinder.equals(method)) {
            return getDepositProviderBinder(arg, extras);
        }
        return super.call(method, arg, extras);
    }


    /**
     * @param arg
     * @param bundle
     */
    private Bundle getDepositProviderBinder(String arg, Bundle bundle) {
        Bundle result = new Bundle();
//        if (!getCallingPackage().equals(getContext().getPackageName())) {
//            result.putInt(ParamKeyworlds.KEY_int_ret, -1);
//            return result;
//        }

        result.putInt(ParamKeyworlds.KEY_int_ret, 0);
        result.putBinder(ParamKeyworlds.KEY_Binder_Binder, ProviderBinder.getInstance(getContext()));
        return result;
    }

    /**
     * 启动的时候发一个广播，并将ProviderBinder发送过去。
     * 目的是为了当主进程死了之后，而其他进程没有死，当主进程又活了，告诉子进程并同时将ProviderBinder发送过去，
     * 让子进程再次注册一下
     */
    private void sendStartBroadcast() {
        Intent intent = new Intent();
        intent.setPackage(getContext().getPackageName());
        intent.setAction(ParamKeyworlds.KEY_String_startAction);
        Bundle bundle = new Bundle();
        bundle.putBinder(ParamKeyworlds.KEY_Binder_Binder, ProviderBinder.getInstance(getContext()).asBinder());
        intent.putExtra(ParamKeyworlds.KEY_Bundle_extra, bundle);
        getContext().sendBroadcast(intent);
    }
}
