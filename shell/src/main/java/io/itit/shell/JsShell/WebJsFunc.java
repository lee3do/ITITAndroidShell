package io.itit.shell.JsShell;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cn.trinea.android.common.util.StringUtils;
import io.itit.androidlibrary.ui.BaseActivity;
import io.itit.shell.Utils.MyWebView;
import io.itit.shell.domain.JsArgs;
import io.itit.shell.ui.MainActivity;
import io.itit.shell.ui.ShellFragment;

/**
 * Created by Lee_3do on 2017/12/28.
 */

public class WebJsFunc {
    public BaseActivity activity;
    public MainActivity mainActivity;
    public MyWebView webView;
    public ShellFragment shellFragment;

    public String payCallback;
    public String uploadCallback;
    public String loginCallback;
    public String scanCallback;
    public String locationCallback;
    public String audioFinishCallback;
    public JsArgs.ArgsBean argsBean;
    public JsArgs.ArgsBean audioArgsBean;

    public UMShareListener shareListener = new UMShareListener() {
        /**
         * @descrption 分享开始的回调
         * @param platform 平台类型
         */
        @Override
        public void onStart(SHARE_MEDIA platform) {

        }

        /**
         * @descrption 分享成功的回调
         * @param platform 平台类型
         */
        @Override
        public void onResult(SHARE_MEDIA platform) {
            Toast.makeText(activity,"成功了",Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享失败的回调
         * @param platform 平台类型
         * @param t 错误原因
         */
        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            Toast.makeText(activity,"失败"+t.getMessage(),Toast.LENGTH_LONG).show();
        }

        /**
         * @descrption 分享取消的回调
         * @param platform 平台类型
         */
        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Toast.makeText(activity,"取消了",Toast.LENGTH_LONG).show();

        }
    };

    public WebJsFunc(Activity activity, MyWebView webView, ShellFragment shellFragment) {
        this.activity = (BaseActivity) activity;
        this.webView = webView;
        this.shellFragment = shellFragment;
        if(activity instanceof  MainActivity){
            this.mainActivity = (MainActivity) activity;
        }
    }

    @JavascriptInterface
    public void postMessage(String value) {
        Logger.d("invoke:" + value);
        JsArgs arg = JSON.parseObject(value, JsArgs.class);
        try {
            Method m = this.getClass().getMethod(arg.func, JsArgs.ArgsBean.class);
            activity.runOnUiThread(() -> {
                try {
                    Object res = m.invoke(this, arg.args);//yes
                    if (res != null && res instanceof Map) {
                        evalJs(arg.args.callback, (Map) res);
                    } else {
                        if (res != null && res instanceof Boolean) {

                        } else {
                            if (arg.args.callback != null) {
                                evalJs(arg.args.callback, new HashMap());
                            }
                        }
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.e(e, "");
                }
            });
        } catch (NoSuchMethodException e) {
            Logger.e(e, "");
        }
    }

    public void evalJs(String callback, Map args) {
        if (StringUtils.isEmpty(callback)) {
            return;
        }
        Logger.d("evaljs:" + callback + "," + JSON.toJSONString(args));
        activity.runOnUiThread(() -> {
            webView.evaluateJavascript("shellInvokeCallback('" + callback + "'," + JSON
                    .toJSONString(args) + ")", null);
        });
    }

    public void evalJs(String callback) {
        if (StringUtils.isEmpty(callback)) {
            return;
        }
        Logger.d("evaljs0:" + JSON.toJSONString(callback));
        activity.runOnUiThread(() -> {
            webView.evaluateJavascript("shellInvokeCallback('" + callback + "')", null);
        });
    }
}
