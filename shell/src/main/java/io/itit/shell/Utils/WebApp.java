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
import java.util.HashMap;
import java.util.Map;

import cn.trinea.android.common.util.StringUtils;
import es.dmoral.toasty.Toasty;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.utils.NetWorkUtil;
import io.itit.shell.JsArgs;
import io.itit.shell.ui.MainFragment;
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

    public void evalJs(String callback,Map args){
        webView.evaluateJavascript("shellInvokeCallback('" + callback + "',"+JSON.toJSONString(args)+")", null);
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
      //  evalJs(args.callback,"1");
    }

    public void setNavigationBarTitle(JsArgs.ArgsBean args) {
        shellFragment.textView.setText(args.title);
    }

    public void log(JsArgs.ArgsBean args) {
        Logger.d(args.message);
    }

    public void postMessage(JsArgs.ArgsBean args) {
        RxBus.get().post(Consts.BusAction.REC_MSG, JSON.toJSONString(args.args));
    }

    public void pushPage(JsArgs.ArgsBean args) {
        ((MainFragment) shellFragment.getParentFragment()).startBrotherFragment(ShellFragment
                .newInstance(args.path, "",args.query, true));
    }

    public void popPage(JsArgs.ArgsBean args) {
        shellFragment.pop();
    }

    public void showLoading(JsArgs.ArgsBean args) {
        shellFragment.showLoading(true);
    }

    public void hideLoading(JsArgs.ArgsBean args) {
        shellFragment.showLoading(false);
    }

    public void getNetworkType(JsArgs.ArgsBean args) {
        String type = NetWorkUtil.getNetworkTypeName(activity);
        Map<String,String> res = new HashMap<>();
        res.put("networkType",type);
        evalJs(args.callback,res);
    }


}
