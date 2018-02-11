package io.itit.shell.JsShell;

import android.app.Activity;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.smtt.sdk.WebView;

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
        WxUtils.shareText(args.scene,args.text);
    }

    public void shareImage(JsArgs.ArgsBean args) {
        WxUtils.shareImageFile(activity,args.scene,args.path,args.thumbPath);
    }

    public void shareURL(JsArgs.ArgsBean args) {
        WxUtils.shareUrl(activity,args.scene,args.url,args.title,args.description,args.thumbPath);
    }


    public void login(JsArgs.ArgsBean args) {
        WxUtils.wxLogin(args.state);
    }

    public void registerApp(JsArgs.ArgsBean args) {
        WxUtils.registerApp(args.appId, activity);
    }

    public void pay(JsArgs.ArgsBean args) {
        PayReq request = new PayReq();
        request.packageValue = args.packageValue;
        request.prepayId = args.prepayId;
        request.partnerId = args.partnerId;
        request.nonceStr = args.nonceStr;
        request.timeStamp = args.timeStamp;
        request.sign = args.sign;

        WxUtils.msgApi.sendReq(request);
    }


}
