package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.InterCallback;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.PermissionActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.nativefull.ActivityLoadNativeFullV2;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.FragmentIntro4Binding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;


public class FragmentIntro4 extends AbsBaseFragment<FragmentIntro4Binding> {


    @Override
    public int getLayout() {
        return R.layout.fragment_intro4;
    }

    @Override
    public void initView() {
        binding.action.setOnClickListener(v-> gotoPermission());
        loadAds();
    }
    private void gotoPermission() {
        if (!SharePreferenceUtils.isOrganic(requireActivity())) {
            Admob.getInstance().loadAndShowInter((AppCompatActivity) requireActivity(),
                    getString(R.string.inter_intro4),
                    0, 30000,
                    new InterCallback() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            if (isAdded()) {
                                ActivityLoadNativeFullV2.open(requireActivity(),
                                        getString(R.string.native_full_inter_intro),
                                        () -> handleNavigate());
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError i) {
                            super.onAdFailedToLoad(i);
                            if (isAdded()) {
                                ActivityLoadNativeFullV2.open(requireActivity(),
                                        getString(R.string.native_full_inter_intro),
                                        () -> handleNavigate());
                            }
                        }
                    });
        } else {
            handleNavigate();
        }
    }

    private void handleNavigate() {
        Intent intent = new Intent(requireActivity(), PermissionActivity.class);
        startActivity(intent);
    }

    private void loadAds() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        Admob.getInstance().loadNativeAd(getActivity(), getString(R.string.native_onboarding4), new NativeCallback() {
            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                if (!isAdded() || binding == null) return;
                binding.frAds.removeAllViews();
                binding.frAds.setVisibility(View.GONE);

            }

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
        });

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        Animation anim = AnimationUtils.loadAnimation(requireActivity(), R.anim.pulse);
        binding.action.startAnimation(anim);
    }
}