package io.itit.shell.domain;

import android.graphics.Color;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class Tab {
    public String name;
    public int iconRes;
    public String url;
    public int selectedColor = Color.RED;

    public Tab(String name, int iconRes,String url, int selectedColor) {
        this.name = name;
        this.iconRes = iconRes;
        this.selectedColor = selectedColor;
        this.url = url;
    }

    public Tab(String name, int iconRes,String url) {
        this.name = name;
        this.iconRes = iconRes;
        this.url = url;
    }
}
