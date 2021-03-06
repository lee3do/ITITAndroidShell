package io.itit.shell.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.orhanobut.logger.Logger;
import com.umeng.socialize.UMShareAPI;

import io.itit.androidlibrary.ui.BaseActivity;
import io.itit.shell.R;
import io.itit.shell.Utils.AndroidBug5497Workaround;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends BaseActivity {

    public MainFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSwipeBackEnable(false);
        if (findFragment(MainFragment.class) == null) {
            mFragment = new MainFragment();
            loadRootFragment(R.id.fl_container, mFragment);
        }
        try {
            AndroidBug5497Workaround.assistActivity(this);
        }catch (Exception ignored){
            Logger.e(ignored,"MainActivity");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);

    }

    @Override
    public void onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport();
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        // 设置横向(和安卓4.x动画相同)
        return new DefaultHorizontalAnimator();
    }



}
