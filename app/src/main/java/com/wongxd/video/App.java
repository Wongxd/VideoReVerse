package com.wongxd.video;

import android.app.Application;

/**
 * Created by wongxd on 2017/9/7.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        CrashHandler.getInstance().init(this,2000L);
    }
}
