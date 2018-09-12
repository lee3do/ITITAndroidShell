package io.itit.shell;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.view.CropImageView;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreater;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreater;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.FileUtils;
import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.StringUtils;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.ITITApplication;
import io.itit.androidlibrary.utils.PicassoImageLoader;
import io.itit.shell.domain.AppConfig;
import me.yokeyword.fragmentation.Fragmentation;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class ShellApp extends Application {
    public Boolean useBugly = false;
    public static String buglyAppId = "fa0a542826";
    public static boolean UseWx = true;
    public static boolean UseAli = true;
    public static boolean UseXg = true;

    public static List<Integer> GuildImageList = new ArrayList<>();
    public static int startPage;
    public static AppConfig appConfig;
    public static List<String> jsContents = new ArrayList<>();

    public static Map<String, Object> variables = new HashMap<>();

    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
                return new MaterialHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于
                // %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreater(new DefaultRefreshFooterCreater() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                ClassicsFooter.REFRESH_FOOTER_PULLUP = "";
                ClassicsFooter.REFRESH_FOOTER_RELEASE = "";
                ClassicsFooter.REFRESH_FOOTER_LOADING = "";
                ClassicsFooter.REFRESH_FOOTER_FINISH = "";

                return new ClassicsFooter(context).setDrawableSize(2).setFinishDuration(100);
              //  return new BallPulseFooter(context);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLog();
        initX5();
        initGallery();
        ITITApplication.appContext = this;
    }

    private void initGallery() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new PicassoImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(false);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
    }

    public static void loadAppConfig(Context context) {
        StringBuilder sb = FileUtils.readFile(context.getFilesDir().getAbsolutePath() +
                "/webroot/app.json", "UTF-8");
        appConfig = JSON.parseObject(sb.toString(), AppConfig.class);
        if(appConfig.debug){
            Logger.addLogAdapter(new LogAdapter() {
                @Override
                public boolean isLoggable(int priority, String tag) {
                    if(priority>Logger.INFO){
                        return true;
                    }
                    return false;
                }

                @Override
                public void log(int priority, String tag, String message) {

                }
            });
        }
    }


    public static void loadAppJs(Context context, String name) {
//        StringBuilder sb = FileUtils.readFile(context.getFilesDir().getAbsolutePath() +
//                "/js/app.js", "UTF-8");
//        jsContents.add(sb.toString());
        File file = new File(context.getFilesDir().getAbsolutePath() + "/js/" + name);
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(file), "UTF-8");
            char buff[] = new char[1024];
            CharArrayWriter fromFile = new CharArrayWriter();
            do {
                int numread = in.read(buff);
                if (numread <= 0) {
                    break;
                }
                fromFile.write(buff, 0, numread);
            } while (true);
            String wholeJS = fromFile.toString();
            jsContents.add(wholeJS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StringBuilder readFile(String filePath, String charsetName) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    private void initLog() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder().tag(Consts.LOG_TAG)
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy){
            @Override
            public boolean isLoggable(int priority, String tag) {
                return true;
            }
        });
    }

    private void initX5() {
        QbSdk.initX5Environment(this, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.d("ITIT", "onCoreInitFinished");
            }

            @Override
            public void onViewInitFinished(boolean b) {
                Log.d("ITIT", "onViewInitFinished:" + b);
            }
        });
    }

    public void setBugly(String buglyAppId) {
        this.useBugly = true;
        this.buglyAppId = buglyAppId;
        Bugly.init(getApplicationContext(), buglyAppId, true);
        Fragmentation.builder().handleException(CrashReport::postCatchedException).install();
    }

    public void initUmeng(String appkey,String name,String s2){
        UMConfigure.init(this,appkey
                ,name,UMConfigure.DEVICE_TYPE_PHONE,s2);//58edcfeb310c93091c000be2 5965ee00734be40b580001a0
    }

    public void initUmengWx(String appid,String appkey){
        PlatformConfig.setWeixin(appid, appkey);
    }


    public void initUmengQQ(String appid,String appkey){
        PlatformConfig.setQQZone(appid, appkey);
    }


    public void initUmengWeibo(String appid,String appkey,String redirectUrl){
        PlatformConfig.setSinaWeibo(appid, appkey,redirectUrl);
    }


    public static String getFileFolderUrl(Context context) {
        String url = PreferencesUtils.getString(context,"SERVER");
        if(!StringUtils.isEmpty(url)){
            return url + File.separator ;
        }else if(StringUtils.isEmpty(ShellApp.appConfig.serverRoot)){
            return "file:" + context.getFilesDir().getAbsolutePath() + File.separator + "webroot/";
        }else{
            return ShellApp.appConfig.serverRoot + File.separator;
        }

    }

    public static String getFileFolderPath(Context context) {
//        Logger.d(ShellApp.appConfig.serverRoot);
        String url = PreferencesUtils.getString(context,"SERVER");
        if(!StringUtils.isEmpty(url)){
            return url + File.separator ;
        }else if(StringUtils.isEmpty(ShellApp.appConfig.serverRoot)){
            return context.getFilesDir().getAbsolutePath() + File.separator + "webroot/";
        }else{
            return ShellApp.appConfig.serverRoot + File.separator;
        }
    }


}
