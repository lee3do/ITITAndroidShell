package io.itit.shell.domain;

import java.util.List;
import java.util.Map;

/**
 * Created by Lee_3do on 2017/12/25.
 */

public class JsArgs {
    /**
     * func : pushPage
     * args : {"path":"/index.html","query":"a=1&b=2","callback":null}
     */

    public String func;
    public ArgsBean args;

    public static class ArgsBean {
        /**
         * path : /index.html
         * query : a=1&b=2
         * callback : null
         */
        public int badge;
        public Boolean navigate;
        public Boolean visible;
        public int index;
        public int limit;
        public double quality;
        public double latitude;
        public double longitude;
        public int height;
        public String title;
        public List<String> titles;
        public String position;
        public String source;
        public String dest;
        public List<String> images;
        public String image;
        public String string;
        public String content;
        public String url;
        public String urls;
        public String path;
        public String query;
        public String type;
        public String message;
        public String callback;
        public String key;
        public Object value;
        public Map<String,Object> args;
        public List<String> options;
        public List<String> items;
        public String mode;
        public long date;
    }
}
