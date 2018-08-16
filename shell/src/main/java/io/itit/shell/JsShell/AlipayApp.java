package io.itit.shell.JsShell;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.alipay.sdk.app.PayTask;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.Map;

import io.itit.androidlibrary.utils.AppUtils;
import io.itit.shell.Utils.MyWebView;
import io.itit.shell.domain.AliPayRes;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2018/1/30.
 */

public class AlipayApp extends WebJsFunc {
    public AlipayApp(Activity activity, MyWebView webView, ShellFragment shellFragment) {
        super(activity, webView, shellFragment);
    }

    public Map<String, Object> getInfo(JsArgs.ArgsBean args) {
        Map<String, Object> res = new HashMap<>();
        res.put("version", AppUtils.getVersionName(activity, "com.eg.android.AlipayGphone"));
        return res;
    }

    public Boolean pay(JsArgs.ArgsBean args) {
        final String orderInfo = args.orderString;   // 订单信息
        Runnable payRunnable = () -> {
            try {
                PayTask alipay = new PayTask(activity);
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Logger.d(JSON.toJSONString(result));

                AliPayRes res1 = JSON.parseObject(result.get("result"), AliPayRes.class);
                Map<String, String> res = new HashMap<>();
                res.put("code", res1.alipay_trade_app_pay_response.code);
                evalJs(args.callback, res);

            } catch (Exception e) {
                Logger.e(e, "pay");
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
        return false;
    }


}
