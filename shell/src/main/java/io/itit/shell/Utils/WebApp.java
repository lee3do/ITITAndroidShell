package io.itit.shell.Utils;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;
import com.tencent.smtt.sdk.WebView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.trinea.android.common.util.StringUtils;
import es.dmoral.toasty.Toasty;
import io.itit.androidlibrary.Consts;
import io.itit.shell.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2017/12/25.
 */

public class WebApp {
    public Activity activity;
    public WebView webView;
    public ShellFragment shellFragment;

    public WebApp(Activity activity, WebView webView, ShellFragment shellFragment) {
        this.activity = activity;
        this.webView = webView;
        this.shellFragment = shellFragment;
    }

    @JavascriptInterface
    public void postMessage(String value) {
        Logger.d(value);
        JsArgs arg = JSON.parseObject(value, JsArgs.class);
        try {
            Class clazz = Class.forName("io.itit.shell.Utils.WebApp");
            Method m = clazz.getMethod(arg.func, JsArgs.ArgsBean.class);
            activity.runOnUiThread(() -> {
                try {
                    m.invoke(this, arg.args);//yes
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.e(e, "");
                }
            });
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            Logger.e(e, "");
        }
    }

    public void showToast(JsArgs.ArgsBean args) {
        if (StringUtils.isEmpty(args.type)) {
            args.type = "normal";
        }

        switch (args.type) {
            case "info":
                Toasty.info(activity, args.message, Toast.LENGTH_SHORT, false).show();
                break;
            case "error":
                Toasty.error(activity, args.message, Toast.LENGTH_SHORT, false).show();
                break;
            case "success":
                Toasty.success(activity, args.message, Toast.LENGTH_SHORT, false).show();
                break;
            default:
                Toasty.normal(activity, args.message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void getLocation(JsArgs.ArgsBean args) {
        webView.evaluateJavascript("shellInvokeCallback('" + args.callback + "',1)", null);
    }

    public void setNavigationBarTitle(JsArgs.ArgsBean args) {
        shellFragment.textView.setText(args.title);
    }

    public void log(JsArgs.ArgsBean args) {
        Logger.d(args.message);
    }

    public void postMessage(JsArgs.ArgsBean args) {
        Logger.d(args.args);
        RxBus.get().post(Consts.BusAction.REC_MSG, JSON.toJSONString(args.args));
    }
}
