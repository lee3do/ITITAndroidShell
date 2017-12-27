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

import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import cn.trinea.android.common.util.PreferencesUtils;
import io.itit.androidlibrary.utils.AppUtils;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
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
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            initStartPage();
        } else {
            EasyPermissions.requestPermissions(this, "为了加快程序运行速度，程序需要读写手机存储权限，请同意以下权限，否则无法正常使用本程序。",
                    10086, perms);
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
        int version = PreferencesUtils.getInt(getApplicationContext(), "VERSION", -1);
//        //TODO 拷贝，加版本号判断
        if (true) {
            new Thread(() -> {
                try {
                    Logger.d("copy assets");
                    AppUtils.copyAssetDirToFiles(getApplicationContext(), "webroot");
                    AppUtils.copyAssetDirToFiles(getApplicationContext(), "js");
                    loadAppConfig();
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
        ShellApp.loadAppJs(this);
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
            if (PreferencesUtils.getBoolean(WelcomeActivity.this, "isFirst", true) && ShellApp
                    .GuildImageList.size() > 0) {
                startActivity(new Intent(WelcomeActivity.this, GuideActivity.class));
            } else {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            }
            finish();
        });
    }

    protected long getDuaration() {
        return 3000;
    }

}