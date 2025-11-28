package com.pedometer.steptracker.runwalk.dailytrack.utils;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.MainActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.FragmentBottomSheetExitBinding;

import androidx.appcompat.app.AppCompatActivity;

public class CustomBottomSheetDialogExitFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetExitBinding binding;
    private AppCompatActivity mActivity;

    public CustomBottomSheetDialogExitFragment() {

    }

    public static CustomBottomSheetDialogExitFragment newInstance() {
        return new CustomBottomSheetDialogExitFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AppCompatActivity) {
            mActivity = (AppCompatActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBottomSheetExitBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null) {
            getDialog().setOnShowListener(dialog -> {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (bottomSheet != null) {
                    BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setDraggable(false);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            });
        }

        binding.close.setOnClickListener(v -> {
            dismiss();
            if (mActivity != null) {
                mActivity.finishAffinity();
            }
        });

        loadAds();
    }

    private void loadAds() {
        if (mActivity == null  || binding == null) return;

        Admob.getInstance().loadNativeAd(
                mActivity,
                mActivity.getString(R.string.native_popup_exit),
                new NativeCallback() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        if (binding == null || mActivity == null) return;

                        binding.frAds.removeAllViews();
                        NativeAdView adView = (NativeAdView) LayoutInflater.from(mActivity)
                                .inflate(R.layout.layout_native_intro, null);
                        binding.frAds.addView(adView);
                        Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                    }

                    @Override
                    public void onAdFailedToLoad() {
                        if (binding != null) {
                            binding.frAds.removeAllViews();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
