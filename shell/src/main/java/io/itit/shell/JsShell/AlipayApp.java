package io.itit.shell.JsShell;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alipay.sdk.app.PayTask;
import com.tencent.smtt.sdk.WebView;

import java.util.HashMap;
import java.util.Map;

import io.itit.androidlibrary.utils.AppUtils;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2018/1/30.
 */

public class AlipayApp extends WebJsFunc {
    public AlipayApp(Activity activity, WebView webView, ShellFragment shellFragment) {
        super(activity, webView, shellFragment);
    }

    public Map<String, Object> getInfo(JsArgs.ArgsBean args){
        Map<String, Object> res = new HashMap<>();
        res.put("version",  AppUtils.getVersionName(activity,"com.eg.android.AlipayGphone"));
        return res;
    }

    public Boolean pay(JsArgs.ArgsBean args) {
        final String orderInfo = args.orderString;   // 订单信息
        Runnable payRunnable = () -> {
            PayTask alipay = new PayTask(activity);
            Map<String, String> result = alipay.payV2(orderInfo,true);
            Map<String,String> res1 = (Map<String, String>) JSON.parse(result.get("result"));
            Map<String,String> res2 = (Map<String, String>) JSON.parse(res1.get("alipay_trade_app_pay_response"));
            Map<String, String> res = new HashMap<>();
            res.put("code", res2.get("code"));
            evalJs(args.callback, res);
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        return false;
    }


}
