package io.itit.shell.JsShell;

import android.app.Activity;

import dagger.Module;
import dagger.Provides;
import io.itit.androidlibrary.ui.BaseActivity;
import io.itit.shell.Utils.MyWebView;
import io.itit.shell.ui.ShellFragment;

@Module

public class JsAppModule {

    public BaseActivity activity;
    public MyWebView webView;
    public ShellFragment shellFragment;

    public JsAppModule(Activity activity, MyWebView webView, ShellFragment shellFragment) {
        this.activity = (BaseActivity) activity;
        this.webView = webView;
        this.shellFragment = shellFragment;
    }

    @Provides
    @FragmentScope
    public  WebApp provideWebApp() {
        return new WebApp(activity,webView,shellFragment);
    }

    @Provides
    @FragmentScope
    public  AlipayApp provideAlipayApp() {
        return new AlipayApp(activity,webView,shellFragment);
    }


    @Provides
    @FragmentScope
    public  WxApp provideWxApp() {
        return new WxApp(activity,webView,shellFragment);
    }

    @Provides
    @FragmentScope
    public  XgApp provideXgApp() {
        return new XgApp(activity,webView,shellFragment);
    }

}
