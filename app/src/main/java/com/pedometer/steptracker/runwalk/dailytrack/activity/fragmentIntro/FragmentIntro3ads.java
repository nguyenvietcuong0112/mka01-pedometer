package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;


import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.FragmentAds2Binding;


public class FragmentIntro3ads extends AbsBaseFragment<FragmentAds2Binding> {

    ViewPager2 viewPager;
    @Override
    public int getLayout() {
        return R.layout.fragment_ads2;
    }
    @Override
    public void initView() {
        viewPager = requireActivity().findViewById(R.id.viewPager);
        loadNativeFull(getString(R.string.native_full_intro3));
    }

    private void loadNativeFull(String adId) {
        Admob.getInstance().loadNativeAds(requireActivity(), adId, 1, new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAdsFull.setVisibility(View.GONE);
                binding.animLoading.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = (NativeAdView) LayoutInflater.from(requireActivity())
                        .inflate(R.layout.native_full, null);
                ImageView closeButton = adView.findViewById(R.id.close);
                closeButton.setVisibility(View.INVISIBLE);
                MediaView mediaView = adView.findViewById(R.id.ad_media);
                closeButton.setOnClickListener(v -> mediaView.performClick());
                new CountDownTimer(5000, 1000) {
                    public void onTick(long millisUntilFinished) {

                    }

                    public void onFinish() {
                    }
                }.start();
                binding.frAdsFull.removeAllViews();
                binding.frAdsFull.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
        });
    }
}
