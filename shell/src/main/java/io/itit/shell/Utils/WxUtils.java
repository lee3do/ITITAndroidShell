package io.itit.shell.Utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.itit.shell.ShellApp;

/**
 * Created by Lee_3do on 2018/1/30.
 */

public class WxUtils {
    public static IWXAPI msgApi;
    public static String appId;


    public static void openWx(Context context) {
        ComponentName componet = new ComponentName("com.tencent.mm", "com.tencent.mm.ui" + "" +
                "" + ".LauncherUI");
        //pkg 就是第三方应用的包名
        //cls 就是第三方应用的进入的第一个Activity
        Intent intent = new Intent();
        intent.setComponent(componet);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void shareUrl(Context context, int scene, String url, String title, String
            description, String thumbPath) {
        WXWebpageObject wxWebpageObject = new WXWebpageObject();
        wxWebpageObject.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = wxWebpageObject;
        msg.title = title;
        msg.description = description;
        Bitmap bmp = BitmapFactory.decodeFile(new File(ShellApp.getFileFolderPath(context),
                thumbPath).getAbsolutePath());
        msg.thumbData = Bitmap2Bytes(bmp);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = scene;
        msgApi.sendReq(req);
    }

    public static void shareText(int scene, String text) {
        WXTextObject textObject = new WXTextObject();
        textObject.text = text;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObject;
        msg.description = text;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = scene;
        msgApi.sendReq(req);
    }

    public static void shareImageFile(Context context, int scene, String path, String thumbPath) {
        Bitmap bmp = BitmapFactory.decodeFile(new File(ShellApp.getFileFolderPath(context), path)
                .getAbsolutePath());
        Bitmap bmp2 = BitmapFactory.decodeFile(new File(ShellApp.getFileFolderPath(context),
                thumbPath).getAbsolutePath());
        WXImageObject imageObject = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imageObject;

        msg.thumbData = Bitmap2Bytes(bmp2);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = scene;
        msgApi.sendReq(req);
    }

    public static void wxLogin(String state) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = state;
        msgApi.sendReq(req);
    }

    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System
                .currentTimeMillis();
    }

    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static void registerApp(String appId, Context context) {
        if (msgApi == null) {
            WxUtils.appId = appId;
            msgApi = WXAPIFactory.createWXAPI(context, null);
            // 将该app注册到微信
            msgApi.registerApp(appId);
        }
    }
}
