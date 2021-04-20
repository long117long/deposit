package com.zxl.oneapplibb;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zxl.deposit.jar.DepositForClient;
import com.zxl.oneappbase.IAFun;

/**
 * @author long117long@126.com <br/>
 * @date 2021/4/12 <br/>
 */
public class UseFunActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_fun);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDefaultClick();
            }
        });

        findViewById(R.id.btn_service_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnServiceNameClick();
            }
        });
    }

    private void btnDefaultClick() {
        IAFun defaultFun = new DepositForClient.Builder()
                .setContext(getApplicationContext())
                .create(IAFun.class);
        int start = defaultFun.start("start");
        Log.e("test", "start = " + start);
    }

    private void btnServiceNameClick() {
        IAFun iaFun = new DepositForClient.Builder()
                .setContext(getApplicationContext())
                .setServiceName("AF")
                .create(IAFun.class);
        int start = iaFun.start("start");
        Log.e("test", "start = " + start);
    }

}
