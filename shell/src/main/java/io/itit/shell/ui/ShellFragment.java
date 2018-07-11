package io.itit.shell.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.trinea.android.common.util.StringUtils;
import cn.trinea.android.common.util.ToastUtils;
import io.itit.androidlibrary.Consts;
import io.itit.androidlibrary.ui.BaseBackFragment;
import io.itit.androidlibrary.ui.ScanQrActivity;
import io.itit.androidlibrary.utils.VoiceRecorder;
import io.itit.androidlibrary.widget.LoadingDialog;
import io.itit.shell.JsShell.AlipayApp;
import io.itit.shell.JsShell.WebApp;
import io.itit.shell.JsShell.WxApp;
import io.itit.shell.JsShell.XgApp;
import io.itit.shell.R;
import io.itit.shell.ShellApp;
import io.itit.shell.Utils.AndroidBug54971Workaround;
import io.itit.shell.Utils.Locations;
import io.itit.shell.Utils.MyWebView;
import io.itit.shell.domain.AppConfig;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.wxapi.StatusBarUtil;
import pub.devrel.easypermissions.EasyPermissions;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellFragment extends BaseBackFragment implements EasyPermissions.PermissionCallbacks {
    private static final String Url = "url";
    private static final String Name = "name";
    private static final String CanBack = "canback";
    private static final String Type = "Type";
    private static final String Navigate = "Navigate";
    public LoadingDialog loadingDialog;
    public boolean showSegment = false;

    public String url;
    private String name;
    private String type;
    private String query;
    private int height;
    private boolean canBack;
    private boolean navigate;

    public MyWebView wv;
    public Toolbar toolbar;
    public LinearLayout leftBar;
    public LinearLayout rightBar;
    public ImageView centerImage;
    public TextView textView;
    public LinearLayout containerView;
    public SmartRefreshLayout refreshLayout;
    public TabLayout mTab;
    public ViewPager mViewPager;
    public List<String> mTitles = new ArrayList<>();

    public boolean hidden = true;
    public WebApp webApp;
    public WxApp wxApp;
    public XgApp xgApp;
    public AlipayApp alipayApp;

    public ShellFragment() {
        // Required empty public constructor
    }

    public static ShellFragment newInstance(JsArgs.ArgsBean argsBean, boolean canBack) {
        ShellFragment fragment = new ShellFragment();
        Bundle args = new Bundle();
        if (StringUtils.isEmpty(argsBean.path)) {
            args.putString(Url, argsBean.url);
        } else {
            args.putString(Url, argsBean.path);
        }
        args.putString(Name, argsBean.title);
        args.putString(Type, argsBean.type);
        args.putBoolean(CanBack, canBack);
        args.putString("query", argsBean.query);
        args.putBoolean(Navigate, argsBean.navigate == null ? true : argsBean.navigate);
        args.putInt("height", argsBean.height);
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
            height = getArguments().getInt("height", 400);
            Logger.d("shell fragment create:" + url);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_shell, container, false);

        mTab = view.findViewById(R.id.tab);
        mViewPager = view.findViewById(R.id.viewPager);
        ImageView backView = view.findViewById(R.id.back);
        toolbar = view.findViewById(R.id.toolbar);
        leftBar = view.findViewById(R.id.leftBar);
        rightBar = view.findViewById(R.id.rightBar);
        toolbar = view.findViewById(R.id.toolbar);
        centerImage = view.findViewById(R.id.center_image);


        initWebview(view);

        initPullToRefresh(view);


        initTitle(view);

        toolbar.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                .navigationBarBackgroundColor));
        mTab.setBackgroundColor(Color.parseColor(ShellApp.appConfig.navigationBarBackgroundColor));
//        mTab.setTabTextColors(Color.parseColor(ShellApp.appConfig
//                .navigationBarColor),Color.parseColor(ShellApp.appConfig
//                .navigationBarColor));

        //      mTab.setSelectedTabIndicatorColor(Color.parseColor(ShellApp.appConfig
        // .navigationBarColor));

        setSwipeBackEnable(canBack);
        containerView = view.findViewById(R.id.container);


        if (canBack) {
            // initToolbarNav(toolbar);
            backView.setVisibility(View.VISIBLE);
            backView.setOnClickListener(v -> {
                _mActivity.onBackPressed();
            });
        }

        ImageViewCompat.setImageTintList(backView, ColorStateList.valueOf(Color.parseColor
                (ShellApp.appConfig.navigationBarColor)));
        Logger.d("statusBarHidden:"+ShellApp.appConfig.statusBarHidden);
        if (!navigate || (ShellApp.appConfig.statusBarHidden!=null&&ShellApp.appConfig.statusBarHidden)) {
            toolbar.setVisibility(View.GONE);
        }
        initSize(view);
        for (AppConfig.Pages page : ShellApp.appConfig.pages) {
            if (page.page.equals(url)) {
                if (page.hideNavigationBar != null && page.hideNavigationBar) {
                    toolbar.setVisibility(View.GONE);
                }
                if (!StringUtils.isEmpty(page.navigationBarBackgroundColor)) {
                    toolbar.setBackgroundColor(Color.parseColor(page.navigationBarBackgroundColor));
                    mTab.setBackgroundColor(Color.parseColor(page.navigationBarBackgroundColor));
                }
                if (!StringUtils.isEmpty(page.navigationBarColor)) {
                    textView.setTextColor(Color.parseColor(page.navigationBarColor));
                    ImageViewCompat.setImageTintList(backView, ColorStateList.valueOf(Color
                            .parseColor(page.navigationBarColor)));

//                    mTab.setTabTextColors(ColorStateList.valueOf(Color.parseColor(page
//                            .navigationBarColor)));
//                    mTab.setSelectedTabIndicatorColor(Color.parseColor(page.navigationBarColor));
                }
                if (!StringUtils.isEmpty(page.pageBackgroundColor)) {
                    containerView.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                            .pageBackgroundColor));
                }
                if (page.enableBounces != null) {
                    if (page.enableBounces) {
                        refreshLayout.setEnableOverScrollBounce(true);
                    } else {
                        refreshLayout.setEnableOverScrollBounce(false);
                    }
                }
                break;
            }
        }
        AndroidBug54971Workaround.assistActivity(view.findViewById(R.id.rl_layout));
        return attachToSwipeBack(view);
    }

    public void enableRefresh(boolean value) {
        refreshLayout.setEnableRefresh(value);
    }

    private void initPullToRefresh(View view) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadmore(false);
        refreshLayout.setEnableOverScrollBounce(true);


        refreshLayout.setOnRefreshListener(refreshlayout -> {
            refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
            wv.evaluateJavascript("pagePullToRefresh()", null);
        });

        refreshLayout.setOnLoadmoreListener(refreshlayout -> {
            Logger.d("onLoadmore");
            wv.evaluateJavascript("pageScrollToBottom()", null);
            refreshlayout.finishLoadmore(100);//传入false表示加载失败
        });

        //状态栏透明和间距处理
        StatusBarUtil.immersive(getActivity());

        if (ShellApp.appConfig.statusBarStyle != null && ShellApp.appConfig.statusBarStyle.equals
                ("light")) {
            StatusBarUtil.darkMode(getActivity(), false);
        } else {
            StatusBarUtil.darkMode(getActivity(), true);
        }

        StatusBarUtil.setPaddingSmart(getActivity(), toolbar);
    }

    private void initTitle(View view) {
        textView = view.findViewById(R.id.toolbar_title);
        textView.setTextColor(Color.parseColor(ShellApp.appConfig.navigationBarTitleColor));
        textView.setText(name);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ShellApp.appConfig
                .navigationBarTitleFontSize);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        this.hidden = hidden;
        if (hidden) {
        } else {
        }
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(Consts.BusAction.LoginSuccess)})
    public void loginSuccess(String message) {
        Logger.d("loginSuccess" + message + "," + wxApp.loginCallback + "," + url + wv.toString());
        if (StringUtils.isEmpty(wxApp.loginCallback)) {
            return;
        }
        Map<String, Object> res = new HashMap<>();
        res.put("code", message);
        wxApp.evalJs(wxApp.loginCallback, res);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(Consts.BusAction.PAY_FINISH)})
    public void paySuccess(Integer message) {
        Logger.d("paySuccess:" + message + "," + wxApp.payCallback + "," + url + wv.toString());
        if (StringUtils.isEmpty(wxApp.payCallback)) {
            return;
        }
        Map<String, Object> res = new HashMap<>();
        res.put("code", message);
        wxApp.evalJs(wxApp.payCallback, res);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(Consts.BusAction.REC_MSG)})
    public void pageMessage(String message) {
        Logger.d("pageMessage:" + url + "," + message);
        wv.evaluateJavascript("pageMessage(" + message + ")", null);
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = {@Tag(Consts.BusAction.SCAN_SUCCESS)})
    public void scanSuccess(String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("text", message);
        webApp.evalJs(webApp.scanCallback, res);
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
        Logger.d("onDestroy:" + url);
        wv.evaluateJavascript("pageUnload()", null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebview(View view) {
        if (ShellApp.appConfig.debug) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        wv = view.findViewById(R.id.wv);
        wv.setHorizontalScrollBarEnabled(false);//水平不显示
        wv.setVerticalScrollBarEnabled(false); //垂直不显示

        wv.setBackgroundColor(Color.parseColor(ShellApp.appConfig.pageBackgroundColor));
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        if (ShellApp.appConfig.debug) {
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            wv.clearCache(true);
        }

        webApp = new WebApp(getActivity(), wv, this);
        wv.addJavascriptInterface(webApp, "AppBridge");

        if (ShellApp.UseWx) {
            wxApp = new WxApp(getActivity(), wv, this);
            wv.addJavascriptInterface(wxApp, "WeixinBridge");
        }

        if (ShellApp.UseXg) {
            xgApp = new XgApp(getActivity(), wv, this);
            wv.addJavascriptInterface(xgApp, "XGBridge");
        }

        if (ShellApp.UseAli) {
            alipayApp = new AlipayApp(getActivity(), wv, this);
            wv.addJavascriptInterface(alipayApp, "AlipayBridge");
        }

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logger.d(ShellApp.getFileFolderUrl(getContext()) + url);
                if (url.startsWith("http") || url.startsWith("https")) {
                    wv.loadUrl(url);
                } else {
                    wv.loadUrl(ShellApp.getFileFolderUrl(getContext()) + url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                for (String jsContent : ShellApp.jsContents) {
                    webView.evaluateJavascript(jsContent, null);
                }
                Logger.d(url + "height:" + wv.getHeight());
                Map<String, Object> queryMap = new HashMap<>();
                if (!StringUtils.isEmpty(query)) {
                    for (String s : query.split("&")) {
                        queryMap.put(s.split("=")[0], s.split("=")[1]);
                    }
                }
                webView.evaluateJavascript("pageLoad(" + JSON.toJSONString(queryMap) + ")", null);
            }

        });

        wv.setOnCustomScroolChangeListener((l, t, oldl, oldt) -> {
            float webViewContentHeight = wv.getContentHeight() * wv.getScale();
            //WebView的现高度
            float webViewCurrentHeight = (wv.getView().getHeight() + wv.getView().getScrollY());
            Logger.d(webViewContentHeight + "::" + webViewCurrentHeight);
            if ((webViewContentHeight - webViewCurrentHeight) <= 5) {
                Logger.d("WebView滑动到了底端");
                wv.evaluateJavascript("pageScrollToBottom()", null);
            }
        });


        Logger.d("url is " + url);
        if (url.startsWith("http") || url.startsWith("https")) {
            wv.loadUrl(url);
        } else {
            wv.loadUrl(ShellApp.getFileFolderUrl(getContext()) + url);
        }


    }

    private void initSize(View view) {
        LinearLayout linearLayoutTop = view.findViewById(R.id.empty_top);
        LinearLayout linearLayoutBottom = view.findViewById(R.id.empty_bottom);
        LinearLayout linearLayoutLeft = view.findViewById(R.id.empty_left);
        LinearLayout linearLayoutRight = view.findViewById(R.id.empty_right);

        LinearLayout centerView = view.findViewById(R.id.center);
        LinearLayout.LayoutParams lp;
        if (type.equals(PresentPageActivity.topHalf)) {
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 5);
            centerView.setLayoutParams(lp);
            linearLayoutBottom.setLayoutParams(lp);

        } else if (type.equals(PresentPageActivity.bottomHalf)) {
            lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 5);
            centerView.setLayoutParams(lp);
            linearLayoutTop.setLayoutParams(lp);

        } else if (type.equals(PresentPageActivity.alert) || type.equals(PresentPageActivity
                .popup) || type.equals(PresentPageActivity.custom)) {
            if (type.equals(PresentPageActivity.alert)) {
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2);
                centerView.setLayoutParams(lp);
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4);
                linearLayoutTop.setLayoutParams(lp);
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4);
                linearLayoutBottom.setLayoutParams(lp);
            }

            if (type.equals(PresentPageActivity.popup)) {
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 3);
                centerView.setLayoutParams(lp);
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 3);
                linearLayoutTop.setLayoutParams(lp);
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4);
                linearLayoutBottom.setLayoutParams(lp);
            }

            if (type.equals(PresentPageActivity.custom)) {

                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0,
                        height / 80);
                centerView.setLayoutParams(lp);

                int w = (10 - height / 80) / 2;
                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, w +
                        1);
                linearLayoutBottom.setLayoutParams(lp);

                lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, w);
                linearLayoutTop.setLayoutParams(lp);
            }


            lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
            linearLayoutLeft.setLayoutParams(lp);
            linearLayoutRight.setLayoutParams(lp);

            lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 8);
            refreshLayout.setLayoutParams(lp);

        } else {
            containerView.setBackgroundColor(Color.parseColor(ShellApp.appConfig
                    .pageBackgroundColor));
        }
        linearLayoutBottom.setOnClickListener(c -> getActivity().finish());
        linearLayoutLeft.setOnClickListener(c -> getActivity().finish());
        linearLayoutRight.setOnClickListener(c -> getActivity().finish());
        linearLayoutTop.setOnClickListener(c -> getActivity().finish());
    }


    public void showLoading(Boolean isShow) {
        Logger.d("isShow:" + isShow);
        if (isShow) {
            loadingDialog = LoadingDialog.show(getActivity(), "", true, null);
        } else {
            if (loadingDialog == null) {
                return;
            }
            loadingDialog.hide();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d(requestCode);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 10086) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra
                        (ImagePicker.EXTRA_RESULT_ITEMS);
                List<String> strings = new ArrayList<>();
                for (ImageItem image : images) {
                    strings.add(image.path);
                }
                List<String> imagePath = new ArrayList<>();
                Luban.with(getActivity()).load(strings).setTargetDir(getActivity()
                        .getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath()).
                        setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                                Logger.v("压缩开始");
                            }

                            @Override
                            public void onSuccess(File file) {
                                imagePath.add(file.getAbsolutePath());
                                if (imagePath.size() == strings.size()) {
                                    Map<String, Object> res = new HashMap<>();
                                    res.put("paths", imagePath);
                                    webApp.evalJs(webApp.uploadCallback, res);
                                }

                            }

                            @Override
                            public void onError(Throwable throwable) {
                                Logger.e(throwable, "压缩失败");
                            }
                        }).launch();

            }
        }
    }


    public void stopPullToRefresh() {
        refreshLayout.finishRefresh();
    }

    public void setNavigationBarSegment(JsArgs.ArgsBean args) {
        showSegment = true;
        textView.setText("");
        centerImage.setImageBitmap(null);
        mTitles = args.items;
        if (mTitles.size() > 4) {
            mTab.setTabMode(TabLayout.MODE_SCROLLABLE);
        } else {
            mTab.setTabMode(TabLayout.MODE_FIXED);
        }

        mTab.setVisibility(View.VISIBLE);
        if (!StringUtils.isEmpty(args.color)) {
            mTab.setTabTextColors(Color.parseColor(args.color), Color.parseColor(args
                    .selectedColor));
        }


        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mTitles.size();
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                return 1;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return false;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object
                    object) {

            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles.get(position);
            }
        });
        mTab.setupWithViewPager(mViewPager);

        mTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Map<String, Object> res = new HashMap<>();
                res.put("index", tab.getPosition());
                wv.evaluateJavascript("pageNavigationBarSegmentSelected(" + JSON.toJSONString
                        (res) + ")" + "", null);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int i, List<String> list) {
        if (list.contains(Manifest.permission.RECORD_AUDIO)) {
            VoiceRecorder.getInstance().startRecording();
        }

        if (list.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            ToastUtils.show(getActivity(), "定位中");
            Locations.location.init(getActivity(), webApp);
        }


        if (list.contains(Manifest.permission.CAMERA)) {
            Intent intent = new Intent(getActivity(), ScanQrActivity.class);
            startActivity(intent);
        }


    }

    @Override
    public boolean onBackPressedSupport() {
        return !canBack;
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        if (list.contains(Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(getActivity(), "您拒绝给予权限,无法正常录音!", Toast.LENGTH_LONG).show();
        }

        if (list.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(getActivity(), "您拒绝给予权限,无法正常定位!", Toast.LENGTH_LONG).show();
        }

        if (list.contains(Manifest.permission.CAMERA)) {
            Toast.makeText(getActivity(), "您拒绝给予权限,无法正常拍照!", Toast.LENGTH_LONG).show();
        }
    }
}
