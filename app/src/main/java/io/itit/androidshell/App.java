package io.itit.androidshell;

import io.itit.shell.ShellApp;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class App extends ShellApp {

    @Override
    public void onCreate() {
        super.onCreate();
        GuildImageList.add(R.drawable.guide_page);
        GuildImageList.add(R.drawable.guide_page2);
        GuildImageList.add(R.drawable.guide_page3);

        startPage = R.drawable.guide_page3;
    }
}
