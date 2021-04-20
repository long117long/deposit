package com.zxl.onappliba;

import android.util.Log;

import com.zxl.oneappbase.IAFun;

/**
 * @author long117long@126.com<br/>
 * @date 2021/4/13 <br/>
 */
public class FunDefault implements IAFun {
    private static final String TAG = "FunDefaultTag";
    @Override
    public int start(String name) {
        if (name == null) {
            name = "";
        }
        Log.e(TAG, "start: " + name);
        return name.length();
    }
}
