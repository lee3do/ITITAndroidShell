package io.itit.shell.JsShell;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.alibaba.fastjson.JSON;
import com.hwangjr.rxbus.RxBus;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ListUtils;
import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.ToastUtils;
import es.dmoral.toasty.Toasty;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.ui.ScanQrActivity;
import io.itit.androidlibrary.utils.AppUtils;
import io.itit.androidlibrary.utils.CommonUtil;
import io.itit.androidlibrary.utils.NetWorkUtil;
import io.itit.androidlibrary.utils.VoiceRecorder;
import io.itit.shell.ShellApp;
import io.itit.shell.Utils.Locations;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.domain.PostMessage;
import io.itit.shell.ui.MainActivity;
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
            EasyPermissions.requestPermissions(shellFragment, "请授予定位权限。",
                    10086, perms);
        }
    }


    public void setNavigationBarSegment(JsArgs.ArgsBean args) {
      shellFragment.setNavigationBarSegment(args);
    }

    public void selectNavigationBarSegment(JsArgs.ArgsBean args) {
        shellFragment.mViewPager.setCurrentItem(args.index);
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


    public void setNavigationBarVisible(JsArgs.ArgsBean args) {
        shellFragment.toolbar.setVisibility(args.visible == null ? View.VISIBLE : (args.visible ?
                View.VISIBLE : View.GONE));
    }


    public void setNavigationBarTitle(JsArgs.ArgsBean args) {
        shellFragment.mTab.setVisibility(View.GONE);
        if (StringUtils.isEmpty(args.image)) {
            shellFragment.textView.setText(args.title);
            shellFragment.centerImage.setVisibility(View.GONE);
            shellFragment.textView.setVisibility(View.VISIBLE);
        } else {
            shellFragment.centerImage.setVisibility(View.VISIBLE);
            shellFragment.textView.setVisibility(View.GONE);
            displayImage(args.image, shellFragment.centerImage);
        }
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

    public void setNavigationBarItems(JsArgs.ArgsBean args) {
        if (!ListUtils.isEmpty(args.images)) {
            for (String image : args.images) {
                ImageView imageView = new ImageView(activity);
                Toolbar.LayoutParams lp = new Toolbar.LayoutParams(CommonUtil.dipToPixel(33),
                        CommonUtil.dipToPixel(27));
                if (args.position.equals("left")) {
                    lp.gravity = Gravity.LEFT;
                } else if (args.position.equals("right")) {
                    lp.gravity = Gravity.RIGHT;
                }
                imageView.setLayoutParams(lp);
                imageView.setTag(image);
                displayImage(image, imageView);
                imageView.setOnClickListener(v -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("value", imageView.getTag());
                    webView.evaluateJavascript("pageNavigationItemClicked(" + JSON.toJSONString
                            (res) + ")", null);
                });
                shellFragment.toolbar.addView(imageView);
            }
        }
        if (!ListUtils.isEmpty(args.titles)) {
            for (String title : args.titles) {
                TextView textView = new TextView(activity);
                textView.setText(title);
                Toolbar.LayoutParams lp = new Toolbar.LayoutParams(CommonUtil.dipToPixel(50),
                        CommonUtil.dipToPixel(30));
                if (args.position.equals("left")) {
                    lp.gravity = Gravity.LEFT;
                } else if (args.position.equals("right")) {
                    lp.gravity = Gravity.RIGHT;
                }
                textView.setTextColor(Color.parseColor(ShellApp.appConfig.navigationBarColor));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(lp);
                textView.setOnClickListener(v -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("value", textView.getText());
                    webView.evaluateJavascript("pageNavigationItemClicked(" + JSON.toJSONString
                            (res) + ")", null);
                });
                shellFragment.toolbar.addView(textView);
            }
        }
    }


    public void pushPage(JsArgs.ArgsBean args) {
        if (shellFragment.getParentFragment() instanceof MainFragment) {
            Logger.d("1");
            ((MainFragment) shellFragment.getParentFragment()).start(ShellFragment.newInstance
                    (args, true));
        } else {
            Logger.d("2");
            shellFragment.start(ShellFragment.newInstance(args, true));
        }
    }


    public void showWebView(JsArgs.ArgsBean args) {
        args.navigate = true;
        ((MainFragment) shellFragment.getParentFragment()).start(ShellFragment.newInstance(args,
                true));
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

    public void showImagePicker(JsArgs.ArgsBean args) {
        ImagePicker.getInstance().setSelectLimit(args.limit);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        shellFragment.startActivityForResult(intent, 10086);
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

    public void downloadFile(JsArgs.ArgsBean args) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(args.url));
        request.setDestinationInExternalPublicDir("/download/", args.path);
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context
                .DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }


    public void startAudioRecord(JsArgs.ArgsBean args) {
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(activity, perms)) {
            VoiceRecorder.getInstance().startRecording();
        } else {
            EasyPermissions.requestPermissions(shellFragment, "请授予录音权限。",
                    10086, perms);
        }

    }

    public Map<String, Object> getAudioRecordStatus(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("isRecording", VoiceRecorder.getInstance().isRecording());
        return res;
    }

    public Map<String, Object> stopAudioRecord(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("path", VoiceRecorder.getInstance().stopRecoding());
        return res;
    }

    public void saveImageToAlbum(JsArgs.ArgsBean args) {
        Picasso.with(activity).load(args.path).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "pic",
                        "description");
                ToastUtils.show(activity, "图片保存成功");
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    public void showModal(JsArgs.ArgsBean args) {
        new MaterialDialog.Builder(activity).theme(Theme.LIGHT).title(args.title).content(args
                .message).negativeText("关闭").onNegative((dialog, which) -> dialog.dismiss()).show();
    }

    public void scanQRCode(JsArgs.ArgsBean args) {
        String[] perms = {Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(activity, perms)) {
            Intent intent = new Intent(activity, ScanQrActivity.class);
            activity.startActivity(intent);
        } else {
            EasyPermissions.requestPermissions(shellFragment, "请授予照相权限。",
                    10086, perms);
        }

    }

    public void openLocation(JsArgs.ArgsBean args) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse
                    ("androidamap://viewMap?sourceApplication=appname&poiname=" + args.title +
                            "&lat=" + args.latitude + "&lon=" + args.longitude + "&dev=1"));
            intent.setPackage("com.autonavi.minimap");
            activity.startActivity(intent);
        } catch (Exception e) {
            try {
                Uri uri = Uri.parse("geo:" + args.latitude + "," + args.longitude + "(" + args
                        .title + ")");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(it);
            } catch (Exception ex) {
                new MaterialDialog.Builder(activity).theme(Theme.LIGHT).title("错误").content
                        ("未安装地图应用,无法导航").negativeText("关闭").onNegative((dialog, which) -> dialog
                        .dismiss()).show();
            }

        }
    }

    public void showActionSheet(JsArgs.ArgsBean args) {
        new MaterialDialog.Builder(activity).theme(Theme.LIGHT).title(args.title).items(args
                .options).itemsCallback((dialog, view, which, text) -> {
            Map<String, Object> res = new HashMap<>();
            res.put("option", text);
            evalJs(args.callback, res);
        }).show();
    }

    public void showPickerView(JsArgs.ArgsBean args) {
        new MaterialDialog.Builder(activity).theme(Theme.LIGHT).title(args.title).items(args
                .items).itemsCallback((dialog, view, which, text) -> {
            Map<String, Object> res = new HashMap<>();
            res.put("item", text);
            evalJs(args.callback, res);
        }).show();
    }

    public void showDatePickerView(JsArgs.ArgsBean args) {
        Date date = new Date(args.date);
        if (args.mode.equals("date")) {
            DatePickerDialog dialog = new DatePickerDialog(activity, (view, year, month,
                                                                      dayOfMonth) -> {
                Map<String, Object> res = new HashMap<>();
                res.put("result", new Date(year - 1900, month, dayOfMonth).getTime());
                evalJs(args.callback, res);
            }, date.getYear() + 1900, date.getMonth(), date.getDay());
            dialog.show();
        }

        if (args.mode.equals("time")) {
            TimePickerDialog dialog = new TimePickerDialog(activity, (view, hourOfDay, minute) -> {
                Map<String, Object> res = new HashMap<>();
                res.put("hour", hourOfDay);
                res.put("minute", minute);
                evalJs(args.callback, res);
            }, date.getHours(), date.getMinutes(), false);
            dialog.show();
        }

        if (args.mode.equals("dateAndTime")) {
            DatePickerDialog dialog = new DatePickerDialog(activity, (view, year, month,
                                                                      dayOfMonth) -> {
                TimePickerDialog dialog2 = new TimePickerDialog(activity, (view2, hourOfDay,
                                                                           minute) -> {
                    Map<String, Object> res = new HashMap<>();
                    Logger.d(year + " " + month + " " + dayOfMonth + " " + hourOfDay + " " +
                            minute);
                    res.put("result", new Date(year - 1900, month, dayOfMonth, hourOfDay, minute)
                            .getTime());
                    evalJs(args.callback, res);
                }, date.getHours(), date.getMinutes(), false);
                dialog2.show();
            }, date.getYear() + 1900, date.getMonth(), date.getDay());
            dialog.show();
        }
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



    public Map<String, Object> moveFile(JsArgs.ArgsBean args) throws IOException {
        boolean b = FileUtils.copyFile(args.source,new File(ShellApp.getFileFolderPath(activity), args.dest).getAbsolutePath());
        Map<String, Object> res = new HashMap<>();
        res.put("result", b);
        return res;
    }

    public void writeFile(JsArgs.ArgsBean args) throws IOException {
        File file = new File(ShellApp.getFileFolderPath(activity), args.path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtils.writeFile(file.getAbsolutePath(), args.content, true);
    }

    public void vibrate(JsArgs.ArgsBean args) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
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

    public Map<String, Object> getStorageInfo(JsArgs.ArgsBean args) {
        SharedPreferences settings = activity.getSharedPreferences(PreferencesUtils
                .PREFERENCE_NAME, Context.MODE_PRIVATE);
        Map<String, Object> res = new HashMap<>();
        res.put("value", settings.getAll());
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
        Logger.d(JSON.toJSONString(settings.getAll()));
        editor.apply();
    }

    public void setPasteboard(JsArgs.ArgsBean args) {
        ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(activity
                .CLIPBOARD_SERVICE);
        ClipData clipData = clipboardManager.getPrimaryClip();
        clipData.addItem(new ClipData.Item(args.string));
        Logger.d(args.string);
        clipboardManager.setText(args.string);
    }

    public Map<String, Object> getPasteboard(JsArgs.ArgsBean args) {
        ClipboardManager cm = (ClipboardManager) activity.getSystemService(activity
                .CLIPBOARD_SERVICE);
        String text = cm.getText().toString();// 注意 item.getText 可能为空

        Map<String, Object> res = new HashMap<>();
        res.put("string", text);
        return res;
    }

    public void setTabBarSelectedIndex(JsArgs.ArgsBean args) {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).mFragment.bottomBar.setCurrentItem(args.index);
        }
    }


    public Map<String, Object> getTabBarSelectedIndex(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        if (activity instanceof MainActivity) {
            res.put("index", ((MainActivity) activity).mFragment.bottomBar.getCurrentItemPosition
                    ());
        }
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


    private void displayImage(String url, ImageView imageView) {
        if (!url.startsWith("http")) {
            File file = new File(ShellApp.getFileFolderPath(activity), url);
            Picasso.with(activity).load(file).into(imageView);
        } else {
            Picasso.with(activity).load(url).into(imageView);
        }
    }

}
