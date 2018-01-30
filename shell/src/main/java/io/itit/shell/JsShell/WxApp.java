package io.itit.shell.JsShell;

import android.app.Activity;

import com.tencent.smtt.sdk.WebView;

import io.itit.shell.ShellApp;
import io.itit.shell.Utils.WxUtils;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2018/1/30.
 */

public class WxApp extends WebJsFunc {



    public WxApp(Activity activity, WebView webView, ShellFragment shellFragment) {
        super(activity, webView, shellFragment);
    }

    public void openWXApp(JsArgs.ArgsBean args) {
        WxUtils.openWx(activity);
    }

    public void shareText(JsArgs.ArgsBean args) {
        WxUtils.shareText(args.scene,args.text, ShellApp.getWx(activity));
    }

    public void shareImage(JsArgs.ArgsBean args) {
        WxUtils.shareImageFile(activity,args.scene,args.path,args.thumbPath, ShellApp.getWx(activity));
    }

    public void shareURL(JsArgs.ArgsBean args) {
        WxUtils.shareUrl(activity,args.scene,args.url,args.title,args.description,args.thumbPath, ShellApp.getWx(activity));
    }


    public void login(JsArgs.ArgsBean args) {
        WxUtils.wxLogin(args.state, ShellApp.getWx(activity));
    }

}
