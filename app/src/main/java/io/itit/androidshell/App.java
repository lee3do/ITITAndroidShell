package io.itit.androidshell;

import io.itit.shell.ShellApp;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class App extends ShellApp {

    @Override
    public void onCreate() {
        super.onCreate();
        assertVersion = 10;
        startPage = R.drawable.guide_page3;
        setBugly("fa0a542826");

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
