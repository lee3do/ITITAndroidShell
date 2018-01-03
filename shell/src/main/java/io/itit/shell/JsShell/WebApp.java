package io.itit.shell.JsShell;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.BatteryManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.ToastUtils;
import es.dmoral.toasty.Toasty;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.utils.AppUtils;
import io.itit.androidlibrary.utils.NetWorkUtil;
import io.itit.shell.ShellApp;
import io.itit.shell.Utils.Locations;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.domain.PostMessage;
import io.itit.shell.ui.MainFragment;
import io.itit.shell.ui.PresentPageActivity;
import io.itit.shell.ui.ShellFragment;
import io.itit.shell.ui.ShowImageActivity;
import me.leolin.shortcutbadger.ShortcutBadger;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Lee_3do on 2017/12/25.
 */

public class WebApp extends WebJsFunc {

    public WebApp(Activity activity, WebView webView, ShellFragment shellFragment) {
        super(activity, webView, shellFragment);
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
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(activity, perms)) {
            ToastUtils.show(activity, "定位中");
            Locations.location.init(activity, this, args);
        } else {
            ToastUtils.show(activity, "没有定位权限，无法定位");
        }
    }

    public void previewImage(JsArgs.ArgsBean args) {
        Intent intent = new Intent(activity, ShowImageActivity.class);
        ArrayList<String> images = new ArrayList<>();
        intent.putExtra("URL", args.urls);
        intent.putExtra("POS", args.index);
        activity.startActivity(intent);
    }

    public Map<String, Object> canOpenURLSchema(JsArgs.ArgsBean args) {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(args.url));
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isValid = !activities.isEmpty();
        Map<String, Object> res = new HashMap<>();
        res.put("result", isValid);
        return res;
    }

    public void openURLSchema(JsArgs.ArgsBean args) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(args.url);
        intent.setData(content_url);
        activity.startActivity(intent);
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
        if (shellFragment.getParentFragment() instanceof MainFragment) {
            Logger.d("1");
            ((MainFragment) shellFragment.getParentFragment()).start(ShellFragment.newInstance
                    (args.path, "", args.query, true));
        } else {
            Logger.d("2");
            shellFragment.start(ShellFragment.newInstance(args.path, "", args.query, true));
        }
    }

    public void enablePullToRefresh(JsArgs.ArgsBean args) {
        shellFragment.enableRefresh(true);
    }

    public void stopPullToRefresh(JsArgs.ArgsBean args) {
        shellFragment.stopPullToRefresh();
    }

    public void presentPage(JsArgs.ArgsBean args) {
        Intent intent = new Intent(activity, PresentPageActivity.class);
        intent.putExtra("ext", JSON.toJSONString(args));
        activity.startActivity(intent);
    }

    public void dismissPage(JsArgs.ArgsBean args) {
        if (activity instanceof PresentPageActivity) {
            activity.finish();
        }
    }

    public void popToRootPage(JsArgs.ArgsBean args) {
        activity.popTo(MainFragment.class, false);
    }

    public void popPage(JsArgs.ArgsBean args) {
        shellFragment.pop();
    }

    public void loadPage(JsArgs.ArgsBean args) {
        webView.loadUrl(ShellApp.getFileFolderUrl(activity) + args.path);
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

    public Map<String, Object> getFileURL(JsArgs.ArgsBean args) {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        Map<String, Object> res = new HashMap<>();
        if (file.exists()) {
            res.put("url", "file:" + file.getPath());
        } else {
            res.put("url", "file:" + ShellApp.getFileFolderUrl(activity) + args.path);
        }
        return res;
    }

    public Map<String, Object> getFilePath(JsArgs.ArgsBean args) {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        Map<String, Object> res = new HashMap<>();
        if (file.exists()) {
            res.put("url", file.getAbsolutePath());
        } else {
            res.put("url", ShellApp.getFileFolderUrl(activity) + args.path);
        }
        return res;
    }

    public Map<String, Object> readFile(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        StringBuilder sb = FileUtils.readFile((String) getFilePath(args).get("url"), "UTF-8");
        res.put("result", sb.toString());
        return res;
    }

    public void deleteFile(JsArgs.ArgsBean args) throws IOException {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        if (file.exists()) {
            file.delete();
        }
    }

    public void createDirectory(JsArgs.ArgsBean args) throws IOException {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        file.mkdirs();
    }

    public Map<String, Object> isFileExists(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        res.put("result", file.exists());
        return res;
    }

    public void writeFile(JsArgs.ArgsBean args) throws IOException {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtils.writeFile(file.getAbsolutePath(), args.content, true);
    }

    public Map<String, Object> listFiles(JsArgs.ArgsBean args) {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        Map<String, Object> res = new HashMap<>();
        res.put("result", file.list());
        return res;
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

    public void setApplicationBadge(JsArgs.ArgsBean args) {
        int num = args.badge;
        if (num == 0) {
            ShortcutBadger.removeCount(activity);
        } else {
            ShortcutBadger.applyCount(activity, num);
        }
    }

    public void removeStorage(JsArgs.ArgsBean args) {
        SharedPreferences settings = activity.getSharedPreferences(PreferencesUtils
                .PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(args.key);
        editor.apply();
    }

    public void setPasteboard(JsArgs.ArgsBean args) {
        ClipboardManager clipboardManager = (ClipboardManager)activity.getSystemService(activity.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("", args.string); //文本型数据 clipData 的构造方法。
        clipboardManager.setPrimaryClip(clipData);
    }

    public Map<String, Object> getPasteboard(JsArgs.ArgsBean args) {
        ClipboardManager cm = (ClipboardManager)activity.getSystemService(activity.CLIPBOARD_SERVICE);
        ClipData cd2 = cm.getPrimaryClip();
        String str2 = cd2.getItemAt(0).getText().toString();
        Map<String, Object> res = new HashMap<>();
        res.put("string", str2);
        return res;
    }

    public void setTabBarBadge(JsArgs.ArgsBean args) {
        RxBus.get().post(Consts.BusAction.UpdateUnRead, args);
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
        res.put("bundleAppId", AppUtils.getVersionCode(activity));
        res.put("bundleDisplayName", AppUtils.getApplicationName(activity));
        return res;
    }

}
