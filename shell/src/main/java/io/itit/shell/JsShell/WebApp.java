package io.itit.shell.JsShell;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.widget.ImageViewCompat;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.alibaba.fastjson.JSON;
import com.bigkoo.pickerview.OptionsPickerView;
import com.hwangjr.rxbus.RxBus;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tencent.smtt.sdk.WebView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.ListUtils;
import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.ToastUtils;
import es.dmoral.toasty.Toasty;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.network.domain.UploadData;
import io.itit.androidlibrary.network.http.RetrofitProvider;
import io.itit.androidlibrary.ui.ScanQrActivity;
import io.itit.androidlibrary.utils.AppUtils;
import io.itit.androidlibrary.utils.CommonUtil;
import io.itit.androidlibrary.utils.IOUtil;
import io.itit.androidlibrary.utils.NetWorkUtil;
import io.itit.androidlibrary.utils.VoiceRecorder;
import io.itit.androidlibrary.widget.ActionSheetDialog;
import io.itit.shell.ShellApp;
import io.itit.shell.Utils.AudioPlayerUtils;
import io.itit.shell.Utils.Locations;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.domain.PostMessage;
import io.itit.shell.ui.MainActivity;
import io.itit.shell.ui.MainFragment;
import io.itit.shell.ui.PresentPageActivity;
import io.itit.shell.ui.ShellFragment;
import io.itit.shell.ui.ShowImageActivity;
import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

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

    @SuppressLint("CheckResult")
    public Boolean request(JsArgs.ArgsBean args) {
        if (args.method.toLowerCase().equals("post")) {
            if (args.body == null) {
                RetrofitProvider.post(args.url, args.data, args.header, body -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("data", new String(body.bytes()));
                    res.put("code", 200);
                    evalJs(args.callback, res);
                }, error -> {
                    Logger.e(error, "request");
                    Map<String, Object> res = new HashMap<>();
                    res.put("code", 400);
                    evalJs(args.callback, res);
                });
            } else {
                RetrofitProvider.postWithBody(args.url, args.header, args.body, body -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("data", new String(body.bytes()));
                    res.put("code", 200);
                    evalJs(args.callback, res);
                }, error -> {
                    Logger.e(error, "request");
                    Map<String, Object> res = new HashMap<>();
                    res.put("code", 400);
                    evalJs(args.callback, res);
                });
            }

        } else {
            RetrofitProvider.get(args.url, args.data, args.header, body -> {
                Map<String, Object> res = new HashMap<>();
                res.put("data", new String(body.bytes()));
                res.put("code", 200);
                evalJs(args.callback, res);
            }, error -> {
                Logger.e(error, "request");
                Map<String, Object> res = new HashMap<>();
                res.put("code", 400);
                evalJs(args.callback, res);
            });
        }

        return false;
    }

    public void getLocation(JsArgs.ArgsBean args) {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        locationCallback = args.callback;
        if (EasyPermissions.hasPermissions(activity, perms)) {
            ToastUtils.show(activity, "定位中");
            Locations.location.init(activity, this);
        } else {
            EasyPermissions.requestPermissions(shellFragment, "请授予定位权限。", 10086, perms);
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
        if (args.type.equals("base64")) {
            List<String> images = new ArrayList<>();
            for (int i = 0; i < args.contents.size(); i++) {
                File file = AudioPlayerUtils.getInstance().base64ToFile(args.contents.get(i), ".png");
                images.add(file.getAbsolutePath());
            }

            intent.putExtra("URL", JSON.toJSONString(images));
            intent.putExtra("POS", 0);
        } else {
            intent.putExtra("URL", args.urls);
            intent.putExtra("POS", args.index);
        }


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
        if (shellFragment.showSegment) {
            shellFragment.mTab.setVisibility(View.VISIBLE);
        }
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
        RxBus.get().post(Consts.BusAction.REC_MSG, JSON.toJSONString(args));
    }

    public void setNavigationBarItems(JsArgs.ArgsBean args) {
        activity.runOnUiThread(() -> {
            shellFragment.leftBar.removeAllViews();
            shellFragment.rightBar.removeAllViews();

            if (!ListUtils.isEmpty(args.images)) {
                for (String image : args.images) {
                    ImageView imageView = new ImageView(activity);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(CommonUtil
                            .dipToPixel(33), CommonUtil.dipToPixel(24));
                    if (args.position.equals("left")) {
                        lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                    } else if (args.position.equals("right")) {
                        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                    }

                    imageView.setLayoutParams(lp);
                    imageView.setTag(image);
                    displayImage(image, imageView);

                    imageView.setOnClickListener(v -> {
                        Map<String, Object> res = new HashMap<>();
                        res.put("item", imageView.getTag());
                        Logger.d("Click:" + "pageNavigationItemClicked(" + JSON.toJSONString(res)
                                + ")");
                        webView.evaluateJavascript("pageNavigationItemClicked(" + JSON
                                .toJSONString(res) + ")", null);
                    });
                    if (args.position.equals("left")) {
                        shellFragment.leftBar.addView(imageView);
                    } else {
                        shellFragment.rightBar.addView(imageView);
                    }
                    ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(Color
                            .parseColor(ShellApp.appConfig.navigationBarColor)));
                }
            }
            if (!ListUtils.isEmpty(args.titles)) {
                for (String title : args.titles) {
                    TextView textView = new TextView(activity);
                    textView.setText(title);
                    textView.setTextColor(Color.parseColor(ShellApp.appConfig.navigationBarColor));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout
                            .LayoutParams.WRAP_CONTENT, CommonUtil.dipToPixel(30));
                    if (args.position.equals("left")) {
                        lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                    } else if (args.position.equals("right")) {
                        lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                    }
                    textView.setTextColor(Color.parseColor(ShellApp.appConfig.navigationBarColor));
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    textView.setGravity(Gravity.CENTER);
                    textView.setLayoutParams(lp);
                    textView.setOnClickListener(v -> {
                        Map<String, Object> res = new HashMap<>();
                        res.put("item", textView.getText());
                        webView.evaluateJavascript("pageNavigationItemClicked(" + JSON
                                .toJSONString(res) + ")", null);
                    });
                    if (args.position.equals("left")) {
                        shellFragment.leftBar.addView(textView);
                    } else {
                        shellFragment.rightBar.addView(textView);
                    }
                }
            }
        });

    }


    public void pushPage(JsArgs.ArgsBean args) {
        if (args.url != null && (args.url.startsWith("http") || args.url.startsWith("https"))) {
            return;
        }
        if (shellFragment.getParentFragment() instanceof MainFragment) {
            ((MainFragment) shellFragment.getParentFragment()).start(ShellFragment.newInstance
                    (args, true));
        } else {
            shellFragment.start(ShellFragment.newInstance(args, true));
        }
    }


    public void showWebView(JsArgs.ArgsBean args) {
        args.navigate = true;
        if (shellFragment.getParentFragment() instanceof MainFragment) {
            ((MainFragment) shellFragment.getParentFragment()).start(ShellFragment.newInstance
                    (args, true));
        } else {
            shellFragment.start(ShellFragment.newInstance(args, true));
        }
    }

    public void enablePullToRefresh(JsArgs.ArgsBean args) {
        if (args.enable == null) {
            shellFragment.enableRefresh(true);
            return;
        }
        shellFragment.enableRefresh(args.enable);
    }

    public void stopPullToRefresh(JsArgs.ArgsBean args) {
        shellFragment.stopPullToRefresh();
    }

    public void showRootViewController(JsArgs.ArgsBean args) {

        activity.startActivity(new Intent(activity, MainActivity.class));
        activity.finish();
    }

    public void presentPage(JsArgs.ArgsBean args) {
        Intent intent = new Intent(activity, PresentPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("ext", JSON.toJSONString(args));
        activity.startActivity(intent);
    }

    public void dismissPage(JsArgs.ArgsBean args) {
        if (activity instanceof PresentPageActivity) {
            activity.finishAndRemoveTask();
        }
    }

    public boolean showImagePicker(JsArgs.ArgsBean args) {
        ImagePicker.getInstance().setSelectLimit(args.limit);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        shellFragment.startActivityForResult(intent, 10086);
        uploadCallback = args.callback;
        return false;
    }


    public void popToRootPage(JsArgs.ArgsBean args) {
        activity.popTo(MainFragment.class, false);
    }

    public void popPage(JsArgs.ArgsBean args) {
        shellFragment.wv.destroy();
        shellFragment.pop();
    }

    public void loadPage(JsArgs.ArgsBean args) {
        webView.loadUrl(ShellApp.getFileFolderUrl(activity) + args.path);
    }

    public void showLoading(JsArgs.ArgsBean args) {
        shellFragment.showLoading(true,args.images,args.timeInterval);
        evalJs(args.callback);
    }

    public boolean downloadFile(JsArgs.ArgsBean args) {
        RetrofitProvider.getApiInstance().download(args.url).subscribeOn(io.reactivex.schedulers
                .Schedulers.io()).observeOn(io.reactivex.schedulers.Schedulers.io()).subscribe
                (body -> {
            File file = new File(ShellApp.getFileFolderPath(activity), args.path);
            InputStream is = body.byteStream();
            IOUtil.saveToFile(is, file);
            Logger.d("file path:" + file.getAbsolutePath());
            Map<String, Object> res = new HashMap<>();
            res.put("path", file.getAbsolutePath());
            activity.runOnUiThread(() -> evalJs(args.callback, res));

        });
        return false;
    }

    public Boolean uploadFile(JsArgs.ArgsBean args) {
        String path = args.fullpath;
        String url = args.url;
        Logger.d("path is " + path + ",URL is " + url);
        File file = new File(path);
        if (StringUtils.isEmpty(args.format) || !args.format.equals("jpeg")) {
            uploadFile(file, args);
            return false;
        }
        Luban.with(activity).load(file).setTargetDir(activity.getExternalFilesDir(Environment
                .DIRECTORY_DCIM).getAbsolutePath()).
                setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        Logger.v("压缩开始");
                    }

                    @Override
                    public void onSuccess(File file) {
                        Logger.v("压缩结束" + file.getAbsolutePath());
                        uploadFile(file, args);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Logger.e(throwable, "压缩失败");
                    }
                }).launch();


        return false;
    }

    private void uploadFile(File file, JsArgs.ArgsBean args) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", args.path, requestFile);
        Call<UploadData> call = RetrofitProvider.getApiInstance().uploadFile(body, args.url);
        call.enqueue(new Callback<UploadData>() {
            @Override
            public void onResponse(Call<UploadData> call, Response<UploadData> response) {
                if (response.body() != null) {
                    if (!response.body().success) {
                        ToastUtils.show(activity, "上传失败！");
                        return;
                    }
                    Map<String, Object> res = new HashMap<>();
                    res.put("response", JSON.toJSONString(response.body()));
                    evalJs(args.callback, res);
                } else {
                    ToastUtils.show(activity, "上传失败！");
                    Map<String, Object> res = new HashMap<>();
                    evalJs(args.callback, res);
                }
            }

            @Override
            public void onFailure(Call<UploadData> call, Throwable t) {
                Logger.e("uploadFile failed:" + t.getLocalizedMessage());
                ToastUtils.show(activity, "上传失败！");
                Map<String, Object> res = new HashMap<>();
                evalJs(args.callback, res);
            }
        });
    }

    public void unzip(JsArgs.ArgsBean args) throws IOException {
        ZipFile zfile = new ZipFile(new File(ShellApp.getFileFolderPath(activity), args.path));
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                // Logger.d("ze.getName() = " + ze.getName());
                String dirstr = ShellApp.getFileFolderPath(activity) + ze.getName();
                //dirstr.trim();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                // Log.d("upZipFile", "str = " + dirstr);
                File f = new File(dirstr);
                f.mkdir();
                continue;

            }
            //  Logger.d("ze.getName() = " + ze.getName());
            OutputStream os = new BufferedOutputStream(new FileOutputStream(io.itit
                    .androidlibrary.utils.FileUtils.getRealFileName(ShellApp.getFileFolderPath
                            (activity), ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
    }


    public Map<String, Object> startAudioRecord(JsArgs.ArgsBean args) {
        String[] perms = {Manifest.permission.RECORD_AUDIO};
        Map<String, Object> res = new HashMap<>();
        if (EasyPermissions.hasPermissions(activity, perms)) {
            this.argsBean = args;
            this.audioFinishCallback = args.finishCallback;
            VoiceRecorder.getInstance().startRecording(args.duration);
            res.put("path", VoiceRecorder.getInstance().getVoiceFilePath());
        } else {
            EasyPermissions.requestPermissions(shellFragment, "请授予录音权限。", 10086, perms);
        }
        return res;
    }

    public Map<String, Object> getAudioRecordStatus(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("isRecording", VoiceRecorder.getInstance().isRecording());
        return res;
    }


    public Map<String, Object> playAudio(JsArgs.ArgsBean args) {
        AudioPlayerUtils.getInstance().playBase64(activity, args.content);
        Logger.d("length:" + AudioPlayerUtils.getInstance().getRecordLong());
        Map<String, Object> res = new HashMap<>();
        res.put("duration", AudioPlayerUtils.getInstance().getRecordLong());
        return res;
    }


    public Map<String, Object> stopAudioRecord(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("path", VoiceRecorder.getInstance().stopRecoding());
        evalJs(this.audioFinishCallback, new HashMap());
        return res;
    }

    public void saveImageToAlbum(JsArgs.ArgsBean args) {
        if(!StringUtils.isEmpty(args.content)){
            File file = AudioPlayerUtils.getInstance().base64ToFile(args.content, ".png");
            try {
                MediaStore.Images.Media.insertImage(activity.getContentResolver(), file.getAbsolutePath(), "pic",
                        "description");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else{
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

    }

    public boolean showModal(JsArgs.ArgsBean args) {
        new MaterialDialog.Builder(activity).theme(Theme.LIGHT).title(args.title).content(args
                .message).positiveText("确定").negativeText("取消").onNegative((dialog, which) ->
                dialog.dismiss()).onPositive((dialog, which) -> {
            evalJs(args.callback);
            dialog.dismiss();
        }).show();
        return false;
    }

    public void scanQRCode(JsArgs.ArgsBean args) {
        String[] perms = {Manifest.permission.CAMERA};
        scanCallback = args.callback;
        if (EasyPermissions.hasPermissions(activity, perms)) {
            Intent intent = new Intent(activity, ScanQrActivity.class);
            activity.startActivity(intent);
        } else {
            EasyPermissions.requestPermissions(shellFragment, "请授予照相权限。", 10086, perms);
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


    public void enableLoadMore(JsArgs.ArgsBean args) {
        shellFragment.refreshLayout.setEnableLoadmore(true);
    }

    public Boolean showActionSheet(JsArgs.ArgsBean args) {
        ActionSheetDialog actionSheetDialog = new ActionSheetDialog(activity).builder();
        actionSheetDialog.setCancelable(true).setCanceledOnTouchOutside(false);
        for (String option : args.options) {
            actionSheetDialog.addSheetItem(option, null, which -> {
                Map<String, Object> res = new HashMap<>();
                res.put("option", option);
                evalJs(args.callback, res);
            });
        }
        actionSheetDialog.show();
        return false;
    }

    public Boolean showPickerView(JsArgs.ArgsBean args) {
        OptionsPickerView pvOptions = new OptionsPickerView.Builder(activity, (options1, option2,
                                                                               options3, v) -> {
            Map<String, Object> res1 = new HashMap<>();
            res1.put("value", args.items.get(options1));
            res1.put("index", options1);
            Map<String, Object> res = new HashMap<>();
            res.put("result", res1);
            evalJs(args.callback, res);
        }).setContentTextSize(22).setLineSpacingMultiplier(1.5f).build();
        pvOptions.setPicker(args.items);
        pvOptions.show();
        pvOptions.setSelectOptions(args.select);
        return false;
    }

    public Boolean showDatePickerView(JsArgs.ArgsBean args) {
        if (args.date == null) {
            args.date = new Date().getTime();
        }
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
        return false;
    }

    public void hideLoading(JsArgs.ArgsBean args) {
        shellFragment.showLoading(false,null, 0);
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
            file = new File(args.path);
            if (file.exists()) {
                res.put("url", file.getAbsolutePath());
            } else {
                res.put("url", ShellApp.getFileFolderUrl(activity) + args.path);
            }
        }
        return res;
    }

    public static String readFileAsBase64(String path, Integer length, Integer offset) {

        try {
            InputStream in = null;
            byte[] data = null;
            // 读取文件字节数组
            try {
                in = new FileInputStream(path);
                if (length != null && offset != null) {
                    data = new byte[offset];
                    in.read(data);
                    data = new byte[length];
                    in.read(data);
                } else {
                    data = new byte[in.available()];
                    in.read(data);
                }
                in.close();
            } catch (IOException e) {
                Logger.e(e, "");
                e.printStackTrace();
            }
            return Base64.encodeToString(data, Base64.NO_WRAP);
        } catch (Exception e) {
            Logger.e(e, "");
            return "";
        }
    }


    /**
     * 根据指定的Activity截图（带空白的状态栏）
     *
     * @param context 要截图的Activity
     * @return Bitmap
     */
    public static Bitmap shotActivity(Activity context) {
        View view = context.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, view.getMeasuredWidth()
                , view.getMeasuredHeight());
        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();
        return bitmap;
    }

    private Bitmap captureWebView() {
        float scale = webView.getScale();
        int width = webView.getWidth();
        int height = (int) (webView.getHeight() * scale);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
    }

    public void startCaptureSession(JsArgs.ArgsBean args) {
        shellFragment.startCaptureSession(args);
    }

    public void pauseCaptureSession(JsArgs.ArgsBean args) {

        shellFragment.stopCaptureSession(args);
    }

    public void resumeCaptureSession(JsArgs.ArgsBean args) {
        shellFragment.startCaptureSession(args);
    }

    public void stopCaptureSession(JsArgs.ArgsBean args) {
        shellFragment.stopCaptureSession(args);
    }

    public Map<String, Object> getFileAttribute(JsArgs.ArgsBean args) {
        long size = FileUtils.getFileSize(args.path);
        Map<String, Object> res = new HashMap<>();
        res.put("size", size);
        return res;
    }

    public Boolean capturePicture(JsArgs.ArgsBean args) {
        activity.runOnUiThread(() -> {
            shellFragment.capturePicture(args);
        });

        return false;
    }

    public Map<String, Object> screenshotToAlbum(JsArgs.ArgsBean args) {
        MediaStore.Images.Media.insertImage(activity.getContentResolver(), shotActivity(activity)
                , "itit", "io.ITIT.io");
        Map<String, Object> res = new HashMap<>();
        return res;
    }

    public Map<String, Object> readFile(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        if (args.type.equals("base64")) {
            String base64 = readFileAsBase64((String) getFilePath(args).get("url"), args.length,
                    args.offset);
            Logger.d("length:" + base64.length());
            res.put("content", base64);
        } else {
            StringBuilder sb = FileUtils.readFile((String) getFilePath(args).get("url"), "UTF-8");
            res.put("content", sb.toString());
        }

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
        res.put("exist", file.exists());
        return res;
    }


    public Map<String, Object> moveFile(JsArgs.ArgsBean args) throws IOException {
        boolean b = FileUtils.copyFile(args.source, new File(ShellApp.getFileFolderPath(activity)
                , args.dest).getAbsolutePath());
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
        Logger.d("displayImage:" + url);
        if (!url.startsWith("http")) {
            File file = new File(ShellApp.getFileFolderPath(activity), url);
            Picasso.with(activity).load(file).into(imageView);
        } else {
            Picasso.with(activity).load(url).into(imageView);
        }
    }

}
