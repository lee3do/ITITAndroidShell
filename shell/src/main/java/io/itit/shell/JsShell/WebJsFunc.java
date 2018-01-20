package io.itit.shell.JsShell;

import android.app.Activity;
import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.tencent.smtt.sdk.WebView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import cn.trinea.android.common.util.StringUtils;
import io.itit.androidlibrary.ui.BaseActivity;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2017/12/28.
 */

public class WebJsFunc {
    public BaseActivity activity;
    public WebView webView;
    public ShellFragment shellFragment;

    public WebJsFunc(Activity activity, WebView webView, ShellFragment shellFragment) {
        this.activity = (BaseActivity) activity;
        this.webView = webView;
        this.shellFragment = shellFragment;
    }

    @JavascriptInterface
    public void postMessage(String value) {
        Logger.d("invoke:"+value);
        JsArgs arg = JSON.parseObject(value, JsArgs.class);
        try {
            Method m = this.getClass().getMethod(arg.func, JsArgs.ArgsBean.class);
            activity.runOnUiThread(() -> {
                try {
                    Object res = m.invoke(this, arg.args);//yes
                    if (res != null && res instanceof Map) {
                        evalJs(arg.args.callback, (Map) res);
                    } else {
                        evalJs(arg.args.callback);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.e(e, "");
                }
            });
        } catch (NoSuchMethodException e) {
            Logger.e(e, "");
        }
    }

    public void evalJs(String callback, Map args) {
        Logger.d("evaljs:"+JSON.toJSONString(args));
        if (!StringUtils.isEmpty(callback)) {
            webView.evaluateJavascript("shellInvokeCallback('" + callback + "'," + JSON
                    .toJSONString(args) + ")", null);
        }
    }

    public void evalJs(String callback) {
        if (!StringUtils.isEmpty(callback)) {
            webView.evaluateJavascript("shellInvokeCallback('" + callback + ")", null);
        }
    }
}