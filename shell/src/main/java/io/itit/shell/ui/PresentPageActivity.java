package io.itit.shell.ui;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;

import cn.trinea.android.common.util.StringUtils;
import io.itit.androidlibrary.ui.BaseActivity;
import io.itit.shell.R;
import io.itit.shell.domain.JsArgs;
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class PresentPageActivity extends BaseActivity {

    public ShellFragment mFragment;
    public String type = "";

    public static String bottomHalf = "bottomHalf";
    public static String alert = "alert";
    public static String popup = "popup";
    public static String topHalf = "topHalf";
    public static String custom = "custom";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_present_page);
        setSwipeBackEnable(false);
        JsArgs.ArgsBean argsBean = JSON.parseObject(getIntent().getStringExtra("ext"), JsArgs
                .ArgsBean.class);
        type = argsBean.type;
        if(!StringUtils.isEmpty(type)){
            argsBean.navigate = false;
            argsBean.visible = false;
        }
        Logger.d("type is "+type);
        if (findFragment(MainFragment.class) == null) {
            mFragment = ShellFragment.newInstance(argsBean, false);
            loadRootFragment(R.id.fl_container, mFragment);
        }
    }

    @Override
    public void onBackPressedSupport() {
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        Logger.d("type1 is "+type);
        if(type.equals(bottomHalf)||type.equals(topHalf)){
            return new DefaultVerticalAnimator();
        }
        return super.onCreateFragmentAnimator();
    }
}
