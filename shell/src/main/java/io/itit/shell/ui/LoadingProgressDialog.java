package io.itit.shell.ui;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import io.itit.shell.R;
import io.itit.shell.ShellApp;
import io.reactivex.annotations.NonNull;

public class LoadingProgressDialog extends Dialog {

    private static final String TAG = "LoadingDialog";

    private String mMessage; // 加载中文字
    private List<String> images; // 旋转图片
    private boolean mCancelable;
    private int timeInterval;
    ImageView iv_loading;
    public static LoadingProgressDialog loadingProgressDialog;


    private LoadingProgressDialog(@NonNull Context context, String message, List<String> images,
                                 int timeInterval) {
        this(context, R.style.LoadingDialog, message, images, false, timeInterval);
    }

    private LoadingProgressDialog(@NonNull Context context, int themeResId, String message,
                                 List<String> images, boolean cancelable, int timeInterval) {
        super(context, themeResId);
        mMessage = message;
        this.images = images;
        mCancelable = cancelable;
        this.timeInterval = timeInterval;
    }

    public static LoadingProgressDialog getInstance(@NonNull Context context, String message, List<String> images,
                                             int timeInterval){
        if(loadingProgressDialog==null){
            loadingProgressDialog = new LoadingProgressDialog(context, message, images,
                    timeInterval);
        }
        loadingProgressDialog.mMessage = message;
        loadingProgressDialog.images = images;
        loadingProgressDialog.timeInterval = timeInterval;
        loadingProgressDialog.initView();
        return loadingProgressDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setContentView(R.layout.progress_dialog);
        // 设置窗口大小
        WindowManager windowManager = getWindow().getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        // 设置窗口背景透明度
        attributes.alpha = 0.3f;
        // 设置窗口宽高为屏幕的三分之一（为了更好地适配，请别直接写死）
        attributes.width = screenWidth / 3;
        attributes.height = attributes.width;
        getWindow().setAttributes(attributes);
        setCancelable(mCancelable);

        TextView tv_loading = findViewById(R.id.tv_loading);
        iv_loading = findViewById(R.id.iv_loading);
        tv_loading.setText(mMessage);

        // 先对imageView进行测量，以便拿到它的宽高（否则getMeasuredWidth为0）
        iv_loading.measure(0, 0);


        if (images != null && images.size() > 0 && timeInterval > 0) {
            changeImage(0);
            tv_loading.setVisibility(View.GONE);
        } else {

        }

    }

    public void changeImage(int i) {
        iv_loading.setImageURI(Uri.fromFile(new File(ShellApp.getFileFolderPath(getContext()),
                images.get(i % images.size()))));
        int t = i + 1;
        new Handler().postDelayed(() -> {
            changeImage(t);
        }, 200);
    }
}