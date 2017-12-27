package io.itit.shell.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.BatteryManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;
import com.tencent.smtt.sdk.WebView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.StringUtils;
import es.dmoral.toasty.Toasty;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.utils.AppUtils;
import io.itit.androidlibrary.utils.NetWorkUtil;
import io.itit.shell.ShellApp;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.domain.PostMessage;
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
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            Logger.e(e, "");
        }
    }

    public void evalJs(String callback, Map args) {
        if (!StringUtils.isEmpty(callback)) {
            Logger.d(JSON.toJSONString(args));
            webView.evaluateJavascript("shellInvokeCallback('" + callback + "'," + JSON
                    .toJSONString(args) + ")", null);
        }
    }

    public void evalJs(String callback) {
        if (!StringUtils.isEmpty(callback)) {
            webView.evaluateJavascript("shellInvokeCallback('" + callback + ")", null);
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
        //  evalJs(args.callback,"1");
    }

    public void setNavigationBarTitle(JsArgs.ArgsBean args) {
        shellFragment.textView.setText(args.title);
    }

    public void log(JsArgs.ArgsBean args) {
        Logger.d(args.message);
    }

    public void postMessage(JsArgs.ArgsBean args) {
        PostMessage pm = new PostMessage();
        pm.name = "pageMessage";
        pm.body = args;
        RxBus.get().post(Consts.BusAction.REC_MSG, JSON.toJSONString(pm));
    }

    public void pushPage(JsArgs.ArgsBean args) {
        ((MainFragment) shellFragment.getParentFragment()).startBrotherFragment(ShellFragment
                .newInstance(args.path, "", args.query, true));
    }

    public void popPage(JsArgs.ArgsBean args) {
        shellFragment.pop();
    }

    public void showLoading(JsArgs.ArgsBean args) {
        shellFragment.showLoading(true);
        evalJs(args.callback);
    }

    public void hideLoading(JsArgs.ArgsBean args) {
        shellFragment.showLoading(false);
        evalJs(args.callback);
    }

    public Map<String, Object> getNetworkType(JsArgs.ArgsBean args) {
        String type = NetWorkUtil.getNetworkTypeName(activity);
        Map<String, Object> res = new HashMap<>();
        res.put("networkType", type);
        return res;
    }

    public void setVariable(JsArgs.ArgsBean args) {
        ShellApp.variables.put(args.key, args.value);
        PostMessage pm = new PostMessage();
        pm.name = "variableChanged";
        pm.body = args;
        RxBus.get().post(Consts.BusAction.REC_MSG, JSON.toJSONString(pm));
    }

    public Map<String, Object> getVariable(JsArgs.ArgsBean args) {
        Object v = ShellApp.variables.get(args.key);
        Map<String, Object> res = new HashMap<>();
        res.put("value", v);
        return res;
    }

    public void removeVariable(JsArgs.ArgsBean args) {
        ShellApp.variables.remove(args.key);
    }


    public void setStorage(JsArgs.ArgsBean args) {
        PreferencesUtils.putString(activity, args.key, args.value + "");
        PostMessage pm = new PostMessage();
        pm.name = "variableChanged";
        pm.body = args;
        RxBus.get().post(Consts.BusAction.REC_MSG, JSON.toJSONString(pm));
    }

    public Map<String, Object> getStorage(JsArgs.ArgsBean args) {
        Object v = PreferencesUtils.getString(activity, args.key);
        Map<String, Object> res = new HashMap<>();
        res.put("value", v);
        return res;
    }

    public void removeStorage(JsArgs.ArgsBean args) {
        SharedPreferences settings = activity.getSharedPreferences(PreferencesUtils
                .PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(args.key);
        editor.apply();
    }

    public Map<String, Object> getSystemInfo(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("platform", "ANDROID");
        Point point = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(point);
        res.put("screenWidth", point.x);
        res.put("screenHeight", point.y);
        res.put("screenScale", 1);
        res.put("batteryLevel", ((BatteryManager) (activity.getSystemService(Context
                .BATTERY_SERVICE))).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));

        switch (activity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                .getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
                res.put("batteryState", "unknown");
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                res.put("batteryState", "charging");
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                res.put("batteryState", "unplugged");
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                res.put("batteryState", "full");
                break;
            default:
                res.put("batteryState", "unknown");
                break;
        }
        res.put("bundleRegion", Locale.getDefault().getLanguage());
        res.put("bundleVersion", AppUtils.getAppVersionName(activity));
        res.put("bundleBuild", activity.getPackageName());
        res.put("bundleAppId",  AppUtils.getVersionCode(activity));
        res.put("bundleDisplayName", AppUtils.getApplicationName(activity));


        return res;
    }

}
