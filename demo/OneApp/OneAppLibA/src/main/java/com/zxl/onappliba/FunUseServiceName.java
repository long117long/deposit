package com.zxl.onappliba;

import android.util.Log;

import com.zxl.oneappbase.IAFun;
import com.zxl.deposit.ServiceName;

/**
 * @author long117long@126.com <br/>
 * @date 2021/4/8 <br/>
 */
@ServiceName("AF")
public class FunUseServiceName implements IAFun {
    private static final String TAG = "FunUseServiceNameTag";

    @Override
    public int start(String name) {
        if (name == null) {
            name = "";
        }
        Log.e(TAG, "start: " + name);
        return name.length() * 2;
    }
}
