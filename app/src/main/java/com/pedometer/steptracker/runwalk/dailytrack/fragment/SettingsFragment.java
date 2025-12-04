package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.mallegan.ads.util.AppOpenManager;
import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.activity.LanguageActivity;
import com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ActivitySettingsBinding;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;
import com.pedometer.steptracker.runwalk.dailytrack.utils.SharePreferenceUtils;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsFragment extends Fragment {

    private ActivitySettingsBinding binding;
    private boolean isBtnProcessing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivitySettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup profile section
        setupProfileSection();

        binding.btnShare.setOnClickListener(v -> {
            if (isBtnProcessing) return;
            isBtnProcessing = true;

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = "có link app thì điền vào";
            String sub = "Flash Alert App";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, "Share"));
//            AppOpenManager.getInstance().disableAppResumeWithActivity(com.pedometer.steptracker.runwalk.dailytrack.activity.HomeActivity.class);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBtnProcessing = false;
                }
            }, 1000);
        });

        binding.btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LanguageActivity.class);
            intent.putExtra("from_settings", true);
            startActivity(intent);
        });

        binding.btnRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=")));
            }
        });

        binding.btnPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://mohamedezzeldin.netlify.app/policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    private void loadAds() {
        if(!SharePreferenceUtils.isOrganic(requireContext())) {
            Context safeContext = getContext();
            if (safeContext == null) return;

            Admob.getInstance().loadNativeAd(safeContext, getString(R.string.native_setting), new NativeCallback() {

                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    super.onNativeAdLoaded(nativeAd);

                    if (!isAdded() || getContext() == null) return;

                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    NativeAdView adView = (NativeAdView) inflater.inflate(R.layout.layout_native_btn_top, null);

                    if (binding.frAds == null) return;

                    binding.frAds.removeAllViews();
                    binding.frAds.addView(adView);
                    Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                }

                @Override
                public void onAdFailedToLoad() {
                    super.onAdFailedToLoad();
                    if (!isAdded() || binding.frAds == null) return;
                    binding.frAds.setVisibility(View.GONE);
                }
            });
        } else {
            binding.frAds.removeAllViews();
            binding.frAds.setVisibility(View.GONE);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (!SharePreferenceUtils.isOrganic(requireContext())) {
            loadAds();
        } else {
            binding.frAds.setVisibility(View.GONE);
            binding.frAds.removeAllViews();
        }
    }

    private void setupProfileSection() {
        String name = ProfileDataManager.getName(requireContext());
        String gender = ProfileDataManager.getGender(requireContext());
        
        if (binding.btnProfile != null) {
            if (!name.isEmpty()) {
                if (binding.tvProfileName != null) {
                    binding.tvProfileName.setText(name);
                }
            } else {
                if (binding.tvProfileName != null) {
                    binding.tvProfileName.setText("About me");
                }
            }

            // Set avatar based on gender
            if (binding.ivProfileAvatar != null) {
                if (gender.equals("male")) {
                    binding.ivProfileAvatar.setImageResource(R.drawable.ic_male_avatar);
                } else {
                    binding.ivProfileAvatar.setImageResource(R.drawable.ic_female_avatar);
                }
            }

            binding.btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                intent.putExtra("edit_mode", true); // Set edit mode
                startActivity(intent);
            });
        }
    }
}

