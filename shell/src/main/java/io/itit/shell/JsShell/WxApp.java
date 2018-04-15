package io.itit.shell.JsShell;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.smtt.sdk.WebView;

import java.util.HashMap;
import java.util.Map;

import io.itit.androidlibrary.utils.AppUtils;
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

    public Boolean login(JsArgs.ArgsBean args) {
        loginCallback = args.callback;
        WxUtils.wxLogin(args.state);
        return false;
    }

    public Map<String, Object> getInfo(JsArgs.ArgsBean args){
        Map<String, Object> res = new HashMap<>();
        res.put("isWXAppInstalled",AppUtils.isInstalled(activity,"com.tencent.mm"));
        res.put("isWXAppSupport",WxUtils.msgApi.getWXAppSupportAPI());
        res.put("version",  AppUtils.getVersionName(activity,"com.tencent.mm"));
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
        Logger.d("pay:"+args.callback+","+shellFragment.url);
        WxUtils.msgApi.sendReq(request);
        return false;
    }


}
