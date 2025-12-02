package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;


import android.view.LayoutInflater;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.FragmentIntro2Binding;


public class FragmentIntro2 extends AbsBaseFragment<FragmentIntro2Binding> {



    @Override
    public int getLayout() {
        return R.layout.fragment_intro2;
    }

    @Override
    public void initView() {
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        binding.action.setOnClickListener(view -> viewPager.setCurrentItem(2));
        loadAds();
    }

    private void loadAds() {
        if (!isAdded() || getActivity() == null) return;

        Admob.getInstance().loadNativeAd(getActivity(), getString(R.string.native_banner_intro), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                if (!isAdded() || getActivity() == null || binding == null) return;

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.ad_native_admob_banner_1, null);

                binding.frBanner.removeAllViews();
                binding.frBanner.addView(adView);
                binding.frBanner.setVisibility(View.VISIBLE);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }

            @Override
            public void onAdFailedToLoad() {
                if (!isAdded() || binding == null) return;
                binding.frBanner.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
    }

}
