package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;


import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.base.BaseActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivityIntroNewBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemConfiguration;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SystemUtil;

import java.util.ArrayList;

public class IntroActivityNew extends BaseActivity {
    private ActivityIntroNewBinding binding;


    @Override
    public void bind() {
        SystemUtil.setLocale(this);
        SystemConfiguration.setStatusBarColor(this, R.color.transparent, SystemConfiguration.IconColor.ICON_LIGHT);
        binding = ActivityIntroNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new FragmentIntro1());
        fragmentList.add(new FragmentIntro2());
        if (!SharePreferenceUtils.isOrganic(IntroActivityNew.this) && SystemUtil.isNetworkConnected(IntroActivityNew.this)) {
            fragmentList.add(new FragmentIntro2ads());
        }
        fragmentList.add(new FragmentIntro3());
        if (!SharePreferenceUtils.isOrganic(IntroActivityNew.this) && SystemUtil.isNetworkConnected(IntroActivityNew.this)) {
            fragmentList.add(new FragmentIntro3ads());
        }
        fragmentList.add(new FragmentIntro4());
        ViewIntroAdapter adapter = new ViewIntroAdapter(this,fragmentList, getSupportFragmentManager(), getLifecycle());
        binding.viewPager.setAdapter(adapter);
        if (!SharePreferenceUtils.isOrganic(IntroActivityNew.this) && SystemUtil.isNetworkConnected(IntroActivityNew.this)) {
            binding.viewPager.setOffscreenPageLimit(3);
        }else {
            binding.viewPager.setOffscreenPageLimit(1);
        }
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    protected void onDestroy() {
        super.onDestroy();

    }
}
