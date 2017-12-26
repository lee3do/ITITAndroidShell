package io.itit.shell.ui;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.orhanobut.logger.Logger;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.ui.BaseBackFragment;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
import io.itit.shell.Utils.WebApp;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellFragment extends BaseBackFragment {
    private static final String Url = "url";
    private static final String Name = "name";
    private static final String CanBack = "canback";

    private String url;
    private String name;
    private boolean canBack;

    public WebView wv;
    public Toolbar toolbar;
    public TextView textView;

    public boolean hidden = true;


    public ShellFragment() {
        // Required empty public constructor
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RxBus.get().register(this);
        if (getArguments() != null) {
            url = ShellApp.getFileFolderUrl(getContext()) + getArguments().getString(Url);
            name = getArguments().getString(Name);
            canBack = getArguments().getBoolean(CanBack);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_shell, container, false);
        initWebview(view);

        initTitle(view);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                .navigationBarBackgroundColor));

        setSwipeBackEnable(canBack);
        if (canBack) {
            initToolbarNav(toolbar);
        }
        return attachToSwipeBack(view);
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
            wv.evaluateJavascript("pageHide()", null);
        } else {
            wv.evaluateJavascript("pageShow()", null);
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
        wv.setBackgroundColor(Color.parseColor(ShellApp.appConfig.pageBackgroundColor));
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new WebApp(getActivity(), wv, this), "appAndroid");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                for (String jsContent : ShellApp.jsContents) {
                    webView.evaluateJavascript(jsContent, null);
                }
                webView.evaluateJavascript("pageLoad()", null);

            }
        });

        wv.loadUrl(url);
    }

    @Override
    public boolean onBackPressedSupport() {
        if (wv.canGoBack()) {
            wv.goBack();
            return true;
        } else {
            return super.onBackPressedSupport();
        }
    }


}
