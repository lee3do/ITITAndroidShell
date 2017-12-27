package io.itit.shell.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import io.itit.shell.R;


public class ShowImageActivity extends FragmentActivity {

    ArrayList<ImageView> images = new ArrayList<>();
    List<String> imagesUrls;
    int pos;
    ViewPager pager;

    @Override
    protected void onNewIntent(Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        pager = findViewById(R.id.viewpager);
        imagesUrls = JSON.parseArray(getIntent().getExtras().getString("URL"),String.class) ;
        pos = getIntent().getIntExtra("POS",0);
        initView();
    }


    private void initView() {
        for (int i = 0; i < imagesUrls.size(); i++) {
            images.add(new ImageView(getApplicationContext()));
        }
        pager.setAdapter(new MyPagerAdapter());
        pager.setCurrentItem(pos);
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {

            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(images.get(position));

        }

        @Override
        public int getItemPosition(Object object) {

            return super.getItemPosition(object);
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = images.get(position);
            container.addView(imageView);
            Logger.d(imagesUrls.get(position));
            Picasso.with(ShowImageActivity.this).load(imagesUrls.get(position)).into(imageView);
            imageView.setOnClickListener(l -> finish());
            return images.get(position);
        }

    }

}
