package io.itit.shell.Utils;

import android.content.Context;
import android.util.AttributeSet;

import com.tencent.smtt.sdk.WebView;


/**
 * Created by Lee_3do on 2018/3/14.
 */

public class MyWebView extends WebView {
    public ScrollInterface mScrollInterface;

    public MyWebView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    //实时滑动监控
    //参数l代表滑动后当前位置，old代表原来原值
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mScrollInterface != null) {
            mScrollInterface.onSChanged(l, t, oldl, oldt);
        }
    }

    //供外部调用，监控滑动
    public void setOnCustomScroolChangeListener(ScrollInterface scrollInterface) {
        this.mScrollInterface = scrollInterface;
    }

    public interface ScrollInterface {
        public void onSChanged(int l, int t, int oldl, int oldt);
    }
}
