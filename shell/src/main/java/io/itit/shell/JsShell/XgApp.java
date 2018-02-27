package io.itit.shell.JsShell;

import android.app.Activity;

import com.orhanobut.logger.Logger;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.smtt.sdk.WebView;

import java.util.HashMap;
import java.util.Map;

import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2018/1/30.
 */

public class XgApp extends WebJsFunc {
    public String token;

    public XgApp(Activity activity, WebView webView, ShellFragment shellFragment) {
        super(activity, webView, shellFragment);
    }

    public void setEnableDebug(JsArgs.ArgsBean args) {
        XGPushConfig.enableDebug(activity, args.enable);
    }

    public void startXG(JsArgs.ArgsBean args) {
        Logger.d("startXG:"+ Long.parseLong(args.appId));
        XGPushConfig.setAccessId(activity, Long.parseLong(args.appId));
        XGPushConfig.setAccessKey(activity,args.appKey);
//        XGPushManager.registerPush(activity, new XGIOperateCallback() {
//            @Override
//            public void onSuccess(Object data, int flag) {
//                token = (String) data;
//            }
//
//            @Override
//            public void onFail(Object data, int errCode, String msg) {
//
//            }
//        });
    }

    public void bind(JsArgs.ArgsBean args) {
        Logger.d("bindXG:"+ args.id);
        XGPushManager.registerPush(activity, args.id, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                token = (String) data;
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {

            }
        });
    }

    public void unbind(JsArgs.ArgsBean args) {
        XGPushManager.unregisterPush(activity, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                token = (String) data;
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {

            }
        });
    }

    public void stopXG(JsArgs.ArgsBean args) {
        XGPushManager.unregisterPush(activity, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                token = (String) data;
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {

            }
        });
    }


    public Map<String, Object> getDeviceToken(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        return res;
    }
}
