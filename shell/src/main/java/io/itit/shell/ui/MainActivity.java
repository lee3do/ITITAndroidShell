package io.itit.shell.ui;

import android.os.Bundle;

import io.itit.androidlibrary.ui.BaseActivity;
import io.itit.androidlibrary.widget.BottomBar;
import io.itit.androidlibrary.widget.BottomBarTab;
import io.itit.shell.AppConfig;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
import me.yokeyword.fragmentation.SupportFragment;

public class MainActivity extends BaseActivity {

    private SupportFragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        loadMultipleRootFragment(R.id.fl_tab_container, 0, mFragments);
        showHideFragment(mFragments[0]);
        backNeedConfirm = true;
    }

    private void initView() {
        initBar();
    }

    private void initBar() {
        BottomBar bottomBar = findViewById(R.id.bottomBar);
        mFragments = new SupportFragment[ShellApp.appConfig.tabBarItems.size()];
        for (int i = 0; i < ShellApp.appConfig.tabBarItems.size(); i++) {
            AppConfig.TabBarItemsBean tab = ShellApp.appConfig.tabBarItems.get(i);
            BottomBarTab t = new BottomBarTab(this, ShellApp.getFileFolderUrl(this)+tab.icon, tab.title);
            t.setColorString(ShellApp.appConfig.tabBarTintColor,ShellApp.appConfig.tabBarBackgroundColor);
            bottomBar.addItem(t);
            mFragments[i] = ShellFragment.newInstance(ShellApp.getFileFolderUrl(this)+tab.page, tab.title);

        }
        bottomBar.setOnTabSelectedListener(new BottomBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, int prePosition) {
                showHideFragment(mFragments[position], mFragments[prePosition]);
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {
                // 在FirstPagerFragment,FirstHomeFragment中接收, 因为是嵌套的Fragment
                // 主要为了交互: 重选tab 如果列表不在顶部则移动到顶部,如果已经在顶部,则刷新
                // EventBus.getDefault().post(new TabSelectedEvent(position));
            }
        });
    }
}
