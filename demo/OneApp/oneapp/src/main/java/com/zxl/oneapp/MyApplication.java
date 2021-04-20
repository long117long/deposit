package com.zxl.oneapp;

import android.app.Application;
import android.content.Intent;

/**
 * @author long117long@126.com <br/>
 * @date 2021/4/8 <br/>
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(), "com.zxl.onappliba.ServiceA");
        startService(intent);
    }
}
