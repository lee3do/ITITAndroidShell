package io.itit.androidshell;

import io.itit.shell.ShellApp;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class App extends ShellApp {

    @Override
    public void onCreate() {
        super.onCreate();
        startPage = R.drawable.start;
        if (BuildConfig.name.equals("lzhb")) {
            setBugly("fa0a542826");
        }


        if (BuildConfig.name.equals("wyhk")) {
            setBugly("fbaaf6bcba");
        }

//        XGPushConfig.enableDebug(this,true);
//        XGPushConfig.setAccessId(this,2100277113);
//        XGPushConfig.setAccessKey(this,"A25BX2L5RS8I");
//        XGPushManager.registerPush(this, new XGIOperateCallback() {
//            @Override
//            public void onSuccess(Object data, int flag) {
//                Logger.d("success"+data);
//            }
//            @Override
//            public void onFail(Object data, int errCode, String msg) {
//                Logger.d("false"+data);
//            }
//        });
    }
}
