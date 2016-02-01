package org.noear.ddcat;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;


import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.InitResultCallback;

import org.noear.ddcat.dao.Setting;
import org.noear.ddcat.dao.engine.DdApi;
import org.noear.ddcat.dao.engine.DdFactory;
import org.noear.ddcat.dao.engine.DdLogListener;
import org.noear.ddcat.dao.engine.DdNode;
import org.noear.ddcat.dao.engine.DdNodeSet;
import org.noear.ddcat.dao.engine.DdSource;
import org.noear.ddcat.dao.ImgLoader;
import org.noear.ddcat.utils.LogWriter;
import org.noear.sited.ISdFactory;
import org.noear.sited.SdApi;
import org.noear.sited.SdNode;
import org.noear.sited.SdNodeSet;
import org.noear.sited.SdSource;

import java.util.Iterator;
import java.util.List;

/**
 * Created by yuety on 14-8-6.
 */
public class App extends Application {
    private static App mCurrent;

    public void onCreate() {
        super.onCreate();
        mCurrent = this;

        DdApi.tryInit(new DdFactory(), new DdLogListener());
    }

    public static App getCurrent(){
        return mCurrent;
    }
}
