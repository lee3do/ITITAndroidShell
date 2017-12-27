package io.itit.shell.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;

import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.ui.BaseMainFragment;
import io.itit.androidlibrary.widget.BottomBar;
import io.itit.androidlibrary.widget.BottomBarTab;
import io.itit.shell.AppConfig;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
import io.itit.shell.domain.JsArgs;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends BaseMainFragment {

    public SupportFragment[] mFragments;
    public BottomBar bottomBar;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        initView(view);
        loadMultipleRootFragment(R.id.fl_tab_container, 0, mFragments);
        RxBus.get().register(this);
        showHideFragment(mFragments[0]);
        return view;
    }

    private void initView(View view) {
        initBar(view);
    }

    private void initBar(View view) {
        bottomBar = view.findViewById(R.id.bottomBar);
        mFragments = new SupportFragment[ShellApp.appConfig.tabBarItems.size()];
        for (int i = 0; i < ShellApp.appConfig.tabBarItems.size(); i++) {
            AppConfig.TabBarItemsBean tab = ShellApp.appConfig.tabBarItems.get(i);
            BottomBarTab t = new BottomBarTab(getContext(), ShellApp.getFileFolderUrl(getContext()) + tab.icon,
                    tab.title);
            t.setColorString(ShellApp.appConfig.tabBarTintColor, ShellApp.appConfig
                    .tabBarBackgroundColor);
            bottomBar.addItem(t);
            mFragments[i] = ShellFragment.newInstance( tab.page, tab.title, false);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxBus.get().unregister(this);
    }

    @Subscribe(tags = {@Tag(Consts.BusAction.UpdateUnRead)})
    public void updateUnRead(JsArgs.ArgsBean args) {
        BottomBarTab tab = bottomBar.getItem(args.index);
        tab.setUnreadCount(args.badge);
    }

    public void startBrotherFragment(SupportFragment fragment) {
        start(fragment, STANDARD);
    }
}
