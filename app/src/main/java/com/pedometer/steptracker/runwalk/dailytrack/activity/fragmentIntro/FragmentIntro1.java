package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.FragmentIntro1Binding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;


public class FragmentIntro1 extends AbsBaseFragment<FragmentIntro1Binding> {


    @Override
    public int getLayout() {
        return R.layout.fragment_intro1;
    }


    @Override
    public void initView() {
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        binding.action.setOnClickListener(view -> viewPager.setCurrentItem(1));
        binding.action.setVisibility(View.GONE);
        loadAdsIntro1();
    }

    private void loadAdsIntro1() {
        binding.frAds.setVisibility(View.VISIBLE);

        if (getActivity() == null || !isAdded()) {
            return;
        }

        Admob.getInstance().loadNativeAd(getActivity(), getString(R.string.native_onboarding1), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(@Nullable NativeAd nativeAd) {

                if (!isAdded() || getActivity() == null) return;

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View adView;

                if (!SharePreferenceUtils.isOrganic(getActivity())) {
                    adView = inflater.inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = inflater.inflate(R.layout.layout_native_language, null);
                }


                NativeAdView nativeAdView = (NativeAdView) adView;

                if (binding == null) return;

                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);

                Admob.getInstance().pushAdsToViewCustom(nativeAd, nativeAdView);

                binding.action.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad() {
                if (!isAdded() || binding == null) return;
                binding.frAds.removeAllViews();
                binding.action.setVisibility(View.VISIBLE);
            }
        });
    }

}
