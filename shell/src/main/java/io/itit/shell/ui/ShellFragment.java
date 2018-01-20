package io.itit.shell.ui;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import cn.trinea.android.common.util.StringUtils;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.ui.BaseBackFragment;
import io.itit.androidlibrary.widget.LoadingDialog;
import io.itit.shell.JsShell.WebApp;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
import io.itit.shell.domain.AppConfig;
import io.itit.shell.domain.JsArgs;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellFragment extends BaseBackFragment {
    private static final String Url = "url";
    private static final String Name = "name";
    private static final String CanBack = "canback";
    private static final String Type = "Type";
    private static final String Navigate = "Navigate";
    public LoadingDialog loadingDialog;

    private String url;
    private String name;
    private String type;
    private String query;
    private boolean canBack;
    private boolean navigate;

    public WebView wv;
    public Toolbar toolbar;
    public TextView textView;
    public LinearLayout containerView;
    SmartRefreshLayout refreshLayout;

    public boolean hidden = true;


    public ShellFragment() {
        // Required empty public constructor
    }

    public static ShellFragment newInstance(JsArgs.ArgsBean argsBean, boolean canBack) {
        ShellFragment fragment = new ShellFragment();
        Bundle args = new Bundle();
        args.putString(Url, argsBean.path);
        args.putString(Name, argsBean.title);
        args.putString(Type, argsBean.type);
        args.putBoolean(CanBack, canBack);
        args.putBoolean(Navigate, argsBean.navigate);
        fragment.setArguments(args);
        return fragment;
    }

    public static ShellFragment newInstance(String url, String name, boolean canBack) {
        ShellFragment fragment = new ShellFragment();
        Bundle args = new Bundle();
        args.putString(Url, url);
        args.putString(Name, name);
        args.putBoolean(CanBack, canBack);
        fragment.setArguments(args);
        return fragment;
    }

    public static ShellFragment newInstance(String url, String name, String query, boolean
            canBack) {
        ShellFragment fragment = new ShellFragment();
        Bundle args = new Bundle();
        args.putString(Url, url);
        args.putString(Name, name);
        args.putBoolean(CanBack, canBack);
        args.putString("query", query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
        if (getArguments() != null) {
            url = getArguments().getString(Url);
            name = getArguments().getString(Name, "");
            canBack = getArguments().getBoolean(CanBack, false);
            navigate = getArguments().getBoolean(Navigate, true);
            query = getArguments().getString("query", "");
            type = getArguments().getString(Type, "");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_shell, container, false);
        initPullToRefresh(view);
        initWebview(view);

        initTitle(view);
        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                .navigationBarBackgroundColor));

        setSwipeBackEnable(canBack);
        containerView = view.findViewById(R.id.container);
        containerView.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                .pageBackgroundColor));

        if (canBack) {
            initToolbarNav(toolbar);
        }
        if (!navigate) {
            toolbar.setVisibility(View.GONE);
        }
        initSize(view);
        for (AppConfig.Pages page : ShellApp.appConfig.pages) {
            if (page.page.equals(url)) {
                if (page.hideNavigationBar!=null&&page.hideNavigationBar) {
                    toolbar.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(page.navigationBarBackgroundColor)) {
                    toolbar.setBackgroundColor(Color.parseColor(page
                            .navigationBarBackgroundColor));
                }
                if (!StringUtils.isEmpty(page.navigationBarColor)) {
                    textView.setTextColor(Color.parseColor(page.navigationBarColor));
                }
                if (!StringUtils.isEmpty(page.pageBackgroundColor)) {
                    containerView.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                            .pageBackgroundColor));
                }
                if (page.enableBounces!=null) {
                    if (page.enableBounces) {
                        refreshLayout.setEnableOverScrollBounce(true);
                    } else {
                        refreshLayout.setEnableOverScrollBounce(false);
                    }
                }
                break;
            }
        }

        return attachToSwipeBack(view);
    }

    public void enableRefresh(boolean value) {
        refreshLayout.setEnableRefresh(value);
    }

    private void initPullToRefresh(View view) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            // refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            wv.evaluateJavascript("pagePullToRefresh()", null);
        });
        refreshLayout.setOnLoadmoreListener(refreshlayout -> {
            Logger.d("onLoadmore");
            // refreshlayout.finishLoadmore(2000/*,false*/);//传入false表示加载失败
        });
    }

    private void initTitle(View view) {
        textView = view.findViewById(R.id.toolbar_title);
        textView.setTextColor(Color.parseColor(ShellApp.appConfig.navigationBarColor));
        textView.setText(name);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (hidden) {
        } else {
        }
    }


    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(Consts.BusAction.REC_MSG)})
    public void pageMessage(String message) {
        Logger.d("pageMessage" + message);
        wv.evaluateJavascript("pageMessage(" + message + ")", null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hidden) {
            wv.evaluateJavascript("pageShow()", null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!hidden) {
            wv.evaluateJavascript("pageHide()", null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxBus.get().unregister(this);
        wv.evaluateJavascript("pageUnload()", null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebview(View view) {
        wv = view.findViewById(R.id.wv);
        wv.setHorizontalScrollBarEnabled(false);//水平不显示
        wv.setVerticalScrollBarEnabled(false); //垂直不显示

        wv.setBackgroundColor(Color.parseColor(ShellApp.appConfig.pageBackgroundColor));
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new WebApp(getActivity(), wv, this), "appAndroid");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logger.d(ShellApp.getFileFolderUrl(getContext()) + url);
                view.loadUrl(ShellApp.getFileFolderUrl(getContext()) + url);
                return true;
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                for (String jsContent : ShellApp.jsContents) {
                    webView.evaluateJavascript(jsContent, null);
                }
                webView.evaluateJavascript("pageLoad('" + query + "')", null);
            }
        });
        Logger.d("url is " + ShellApp.getFileFolderUrl(getContext()) + url);
        wv.loadUrl(ShellApp.getFileFolderUrl(getContext()) + url);
    }

    private void initSize(View view) {
        LinearLayout linearLayoutTop = view.findViewById(R.id.empty_top);
        LinearLayout linearLayoutBottom = view.findViewById(R.id.empty_bottom);
        LinearLayout.LayoutParams lp;
        if (type.equals(PresentPageActivity.topHalf)) {
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 5);
            refreshLayout.setLayoutParams(lp);
            linearLayoutBottom.setLayoutParams(lp);
        } else if (type.equals(PresentPageActivity.bottomHalf)) {
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 5);
            refreshLayout.setLayoutParams(lp);
            linearLayoutTop.setLayoutParams(lp);
        }
        linearLayoutBottom.setOnClickListener(c -> getActivity().finish());
        linearLayoutTop.setOnClickListener(c -> getActivity().finish());
    }


    public void showLoading(Boolean isShow) {
        Logger.d("isShow:" + isShow);
        if (isShow) {
            loadingDialog = LoadingDialog.show(getActivity(), "", true, null);
        } else {
            loadingDialog.hide();
        }
    }


    public void stopPullToRefresh() {
        refreshLayout.finishRefresh();
    }
}
