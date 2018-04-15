package io.itit.androidshell;

import io.itit.shell.ShellApp;

/**
 * Created by Lee_3do on 2017/12/23.
 */

public class App extends ShellApp {

    @Override
    public void onCreate() {
        super.onCreate();
        startPage = R.drawable.start;
        if (BuildConfig.name.equals("lzhb")) {
            setBugly("fa0a542826");
        }


        if (BuildConfig.name.equals("wyhk")) {
            setBugly("fbaaf6bcba");
        }
    }
}
