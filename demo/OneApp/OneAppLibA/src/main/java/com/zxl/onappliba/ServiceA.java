package com.zxl.onappliba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.zxl.deposit.jar.DepositForService;
import com.zxl.oneappbase.IAFun;

/**
 * @author long117long@126.com <br/>
 * @date 2021/4/8 <br/>
 */
public class ServiceA extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    FunUseServiceName aFun = new FunUseServiceName();
    FunDefault defaultFun = new FunDefault();
    @Override
    public void onCreate() {
        super.onCreate();
        DepositForService.getInstance(getApplicationContext()).register(aFun, IAFun.class);
        DepositForService.getInstance(getApplicationContext()).register(defaultFun, IAFun.class);
    }
}
