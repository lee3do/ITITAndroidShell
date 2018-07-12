package io.itit.shell.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.trinea.android.common.util.PreferencesUtils;
import cn.trinea.android.common.util.StringUtils;
import io.itit.androidlibrary.utils.AppUtils;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
import io.itit.shell.domain.AppConfig;
import io.itit.shell.domain.JsArgs;
import pub.devrel.easypermissions.EasyPermissions;


public class WelcomeActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    ImageView welcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        welcome = findViewById(R.id.welcome);
        Picasso.with(this).load(ShellApp.startPage).into(welcome);
        initPermission();

    }

    public void initPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            initStartPage();
        } else {
            EasyPermissions.requestPermissions(this,
                    "为了加快程序运行速度，程序需要读写手机存储权限，请同意以下权限，否则无法正常使用本程序。", 10086, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int i, List<String> list) {
        initStartPage();
    }

    @Override
    public void onPermissionsDenied(int i, List<String> list) {
        Toast.makeText(this, "您拒绝给予权限,无法正常使用本程序!", Toast.LENGTH_LONG).show();
        finish();
    }


    private void copyAssets() {
        boolean needCopy = false;
        int version = PreferencesUtils.getInt(getApplicationContext(), "VERSION", -1);
        try {
            InputStream is = getApplication().getAssets().open("webroot/app.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            AppConfig appConfig = JSON.parseObject(new String(buffer), AppConfig.class);
            if (version < appConfig.version) {
                needCopy = true;
            }
            Logger.d("old version:" + version + ";new Version :" + appConfig.version);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (needCopy) {
            new Thread(() -> {
                try {
                    Logger.d("copy assets");
                    AppUtils.copyAssetDirToFiles(getApplicationContext(), "webroot");
                    AppUtils.copyAssetDirToFiles(getApplicationContext(), "js");
                    loadAppConfig();
                    PreferencesUtils.putInt(getApplicationContext(), "VERSION", ShellApp
                            .appConfig.version);
                } catch (IOException e) {
                    Logger.e(e, "");
                }
            }).start();
        } else {
            loadAppConfig();
        }
    }

    private void loadAppConfig() {
        ShellApp.loadAppConfig(this);
        ShellApp.loadAppJs(this, "AppBridge.js");
        ShellApp.loadAppJs(this, "WeixinBridge.js");
        ShellApp.loadAppJs(this, "XGBridge.js");
        ShellApp.loadAppJs(this, "AlipayBridge.js");
        loadFinish();
    }

    private void initStartPage() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        AlphaAnimation start_anima = new AlphaAnimation(0.5f, 1.0f);
        start_anima.setDuration(getDuaration());

        start_anima.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                new Handler().postDelayed(() -> copyAssets(), 100);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        welcome.startAnimation(start_anima);
    }

    private void loadFinish() {
        runOnUiThread(() -> {
            getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                    .LayoutParams.FLAG_FULLSCREEN);

            if (StringUtils.isEmpty(ShellApp.appConfig.launchPage)) {

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));

            } else {
                JsArgs.ArgsBean argsBean = new JsArgs.ArgsBean();
                argsBean.type = "fullScreen";
                argsBean.url = ShellApp.appConfig.launchPage;

                Intent intent = new Intent(WelcomeActivity.this, PresentPageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("ext", JSON.toJSONString(argsBean));
                WelcomeActivity.this.startActivity(intent);
            }

            finish();
        });
    }

    protected long getDuaration() {
        return 300;
    }

}