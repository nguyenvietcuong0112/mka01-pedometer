package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;


import android.view.LayoutInflater;
import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.FragmentIntro3Binding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;


public class FragmentIntro3 extends AbsBaseFragment<FragmentIntro3Binding> {
    @Override
    public int getLayout() {
        return R.layout.fragment_intro3;
    }
    @Override
    public void initView() {
        ViewPager2 viewPager= requireActivity().findViewById(R.id.viewPager);
        binding.action.setOnClickListener(view->{
            if(!SharePreferenceUtils.isOrganic(requireActivity())){
                viewPager.setCurrentItem(4);
                binding.frAds.setVisibility(View.VISIBLE);
            }else {
                viewPager.setCurrentItem(3);
                binding.frAds.setVisibility(View.GONE);
            }
        });
        loadAds();
    }

    private void loadAds() {
        if(!SharePreferenceUtils.isOrganic(getContext())) {
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
        } else {
            binding.frBanner.removeAllViews();
            binding.frBanner.setVisibility(View.GONE);
        }

    }

}
