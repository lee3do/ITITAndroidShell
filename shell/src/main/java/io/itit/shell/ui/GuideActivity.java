package io.itit.shell.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import cn.trinea.android.common.util.PreferencesUtils;
import io.itit.shell.R;
import io.itit.shell.ShellApp;

public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener {

    ViewPager viewpager;
    private List<ImageView> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        viewpager = findViewById(R.id.viewpager);
        initImages();
        viewpager.setOnPageChangeListener(this);
        viewpager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return ShellApp.GuildImageList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(images.get(position), ViewPager.LayoutParams.MATCH_PARENT,
                        ViewPager.LayoutParams.MATCH_PARENT);
                return images.get(position);
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                // destroyItem((View) container, position, object);
                container.removeView(images.get(position));
            }
        });
        viewpager.setCurrentItem(0);
    }

    private void initImages() {
        images = new ArrayList<>();
        for (int i = 0; i < ShellApp.GuildImageList.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewPager.LayoutParams
                    .MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT));
            Picasso.with(this).load(ShellApp.GuildImageList.get(i)).into(imageView);
            images.add(imageView);
            if ((i + 1) == ShellApp.GuildImageList.size()) {
                imageView.setOnClickListener(v -> {
                    PreferencesUtils.putBoolean(this, "isFirst", false);
                    getWindow().setFlags(~WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    startActivity(new Intent(GuideActivity.this, MainActivity.class));
                    finish();
                });
            }
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Logger.d("p:" + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
