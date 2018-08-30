package io.itit.shell.JsShell;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;

import io.itit.androidlibrary.utils.AppUtils;
import io.itit.shell.Utils.MyWebView;
import io.itit.shell.Utils.WxUtils;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2018/1/30.
 */

public class WxApp extends WebJsFunc {

    public WxApp(Activity activity, MyWebView webView, ShellFragment shellFragment) {
        super(activity, webView, shellFragment);
    }

    public void openWXApp(JsArgs.ArgsBean args) {
        WxUtils.openWx(activity);
    }

    public void shareText(JsArgs.ArgsBean args) {
        new ShareAction(activity).withText(args.text).setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA
                .QQ, SHARE_MEDIA.WEIXIN).setCallback(shareListener).open();
//        WxUtils.shareText(args.scene,args.text);
    }

    public void shareImage(JsArgs.ArgsBean args) {
        new ShareAction(activity).withText("111").setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA
                .QQ, SHARE_MEDIA.WEIXIN).setCallback(shareListener).open();
//        WxUtils.shareImageFile(activity,args.scene,args.path,args.thumbPath);
    }

    public void shareURL(JsArgs.ArgsBean args) {
        new ShareAction(activity).withText("111").setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA
                .QQ, SHARE_MEDIA.WEIXIN).setCallback(shareListener).open();
//        WxUtils.shareUrl(activity, args.scene, args.url, args.title, args.description, args
//                .thumbPath);
    }

    public Boolean login(JsArgs.ArgsBean args) {
        loginCallback = args.callback;
        Logger.d("login:" + args.callback + "," + shellFragment.url + webView.toString());
        WxUtils.wxLogin(args.state);
        return false;
    }

    public Map<String, Object> getInfo(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("isWXAppInstalled", AppUtils.isInstalled(activity, "com.tencent.mm"));
        res.put("isWXAppSupport", WxUtils.msgApi.getWXAppSupportAPI());
        res.put("version", AppUtils.getVersionName(activity, "com.tencent.mm"));
        res.put("appId", WxUtils.appId);
        return res;
    }

    public void registerApp(JsArgs.ArgsBean args) {
        WxUtils.registerApp(args.appId, activity);
    }

    public Boolean pay(JsArgs.ArgsBean args) {
        PayReq request = new PayReq();
        request.packageValue = "Sign=WXPay";
        request.prepayId = args.prepayId;
        request.partnerId = args.partnerId;
        request.nonceStr = args.nonceStr;
        request.timeStamp = args.timeStamp;
        request.sign = args.sign;
        request.appId = WxUtils.appId;
        Logger.d(JSON.toJSONString(request));
        payCallback = args.callback;
        Logger.d("pay:" + args.callback + "," + shellFragment.url + webView.toString());
        WxUtils.msgApi.sendReq(request);
        return false;
    }


}
