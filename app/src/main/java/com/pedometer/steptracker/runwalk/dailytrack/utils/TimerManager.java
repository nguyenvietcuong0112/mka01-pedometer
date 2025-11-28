//package com.stepcounter.healthapplines.pedometer.steptracker.com.pedometer.steptracker.runwalk.dailytrack.utils;
//
//import android.annotation.SuppressLint;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//
//import com.google.android.gms.ads.nativead.NativeAd;
//import com.google.android.gms.ads.nativead.NativeAdView;
//import com.mallegan.ads.callback.NativeCallback;
//import com.mallegan.ads.util.Admob;
//import com.stepcounter.healthapplines.pedometer.steptracker.com.pedometer.steptracker.runwalk.dailytrack.R;
//import com.stepcounter.healthapplines.pedometer.steptracker.com.pedometer.steptracker.runwalk.dailytrack.databinding.LayoutDialogNativeAdsBinding;
//
//public class TimerManager {
//    private static TimerManager instance;
//    private final Handler handler;
//    private Runnable timerRunnable;
//    private boolean isRunning = false;
//    private AlertDialog currentDialog;
//
//    private static final int TIMER_DELAY = 10000;
//    Context currentActivityContext;
//    LayoutDialogNativeAdsBinding binding;
//
//    private TimerManager() {
//        handler = new Handler(Looper.getMainLooper());
//    }
//
//    public static TimerManager getInstance() {
//        if (instance == null) {
//            instance = new TimerManager();
//        }
//        return instance;
//    }
//
//    public void startTimer() {
//        if (isRunning) return;
//        isRunning = true;
//
//        scheduleTimer();
//    }
//
//    public void stopTimer() {
//        isRunning = false;
//        if (handler != null && timerRunnable != null) {
//            handler.removeCallbacks(timerRunnable);
//        }
//        dismissCurrentDialog();
//    }
//
//    private void scheduleTimer() {
//        timerRunnable = this::showDialog;
//
//        handler.postDelayed(timerRunnable, TIMER_DELAY);
//    }
//
//    private void showDialog() {
//        currentActivityContext = AppActivityTracker.getInstance().getCurrentActivity();
//
//        if (currentActivityContext != null) {
//            binding = LayoutDialogNativeAdsBinding.inflate(LayoutInflater.from(currentActivityContext));
//            dismissCurrentDialog();
//
//            new Handler().postDelayed(() -> {
//                binding.closeIcon.setVisibility(View.VISIBLE);
//            }, 5000);
//
//            binding.closeIcon.setOnClickListener(v -> {
//                dismissCurrentDialog();
//
//                if (isRunning) {
//                    scheduleTimer();
//                }
//            });
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(currentActivityContext);
//            builder.setView(binding.getRoot());
//
//            loadAdsNativePopup();
//
//            currentDialog = builder.create();
//
//            currentDialog.setCancelable(false);
//            currentDialog.setCanceledOnTouchOutside(false);
//
//            currentDialog.show();
//        } else {
//            Log.e("TimerManager", "Current activity context is null");
//        }
//    }
//
//    @SuppressLint("InflateParams")
//    public void loadAdsNativePopup() {
//        NativeAdView adView = (NativeAdView) LayoutInflater.from(currentActivityContext)
//                .inflate(R.layout.layout_native_language, null);
//
//        Admob.getInstance().loadNativeAd(currentActivityContext, currentActivityContext.getString(R.string.native_popup), new NativeCallback() {
//            @Override
//            public void onNativeAdLoaded(NativeAd nativeAd) {
//                binding.frAds.removeAllViews();
//                binding.frAds.addView(adView);
//                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
//            }
//
//            @Override
//            public void onAdFailedToLoad() {
//                binding.frAds.removeAllViews();
//                dismissCurrentDialog();
//            }
//        });
//
//    }
//
//
//    private void dismissCurrentDialog() {
//        if (currentDialog != null && currentDialog.isShowing()) {
//            currentDialog.dismiss();
//            currentDialog = null;
//        }
//    }
//
//
//}
