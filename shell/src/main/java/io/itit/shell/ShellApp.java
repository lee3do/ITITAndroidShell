package io.itit.shell;

import android.app.Application;
import android.content.Context;
import android.os.BatteryManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.smtt.sdk.QbSdk;

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
import io.itit.androidlibrary.Consts;
import me.yokeyword.fragmentation.Fragmentation;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class ShellApp extends Application {
    public Boolean useBugly = false;
    public String appId = "";
    public static IWXAPI msgApi;
    public static List<Integer> GuildImageList = new ArrayList<>();
    public static int startPage;
    public static AppConfig appConfig;
    public static List<String> jsContents = new ArrayList<>();

    public static Map<String,Object> variables = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initLog();
        initX5();
        initReceiver();
    }

    private void initReceiver() {


    }


    public static void loadAppConfig(Context context) {
        StringBuilder sb = FileUtils.readFile(context.getFilesDir().getAbsolutePath() +
                "/webroot/app.json", "UTF-8");
        appConfig = JSON.parseObject(sb.toString(), AppConfig.class);
    }


    public static void loadAppJs(Context context) {
//        StringBuilder sb = FileUtils.readFile(context.getFilesDir().getAbsolutePath() +
//                "/js/app.js", "UTF-8");
//        jsContents.add(sb.toString());
        File file = new File(context.getFilesDir().getAbsolutePath() + "/js/app.js");
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
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
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

    public void setBugly(String appId) {
        this.useBugly = true;
        this.appId = appId;
        Bugly.init(getApplicationContext(), appId, false);
        Fragmentation.builder().handleException(CrashReport::postCatchedException).install();
    }

    public void setWx(String appId) {
        msgApi = WXAPIFactory.createWXAPI(this, null);
        boolean res = msgApi.registerApp(appId);
        Logger.d("res api " + res);
    }

    public static String getFileFolderUrl(Context context) {
        return "file:" + context.getFilesDir().getAbsolutePath() + File.separator + "webroot";
    }



}
