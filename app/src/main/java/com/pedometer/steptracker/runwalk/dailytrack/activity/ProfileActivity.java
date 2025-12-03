package com.pedometer.steptracker.runwalk.dailytrack.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ProfileGenderFragment;
import com.pedometer.steptracker.runwalk.dailytrack.fragment.ProfileInfoFragment;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;

public class ProfileActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ProfilePagerAdapter adapter;

    public boolean isEditMode() {
        return isEditMode;
    }

    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Check if this is edit mode (from Settings)
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);

        // If edit mode, allow editing even if profile is completed
        if (!isEditMode) {
            // Check if profile already completed (onboarding mode)
            if (ProfileDataManager.isProfileCompleted(this)) {
                goToMain();
                return;
            }
        }

        viewPager = findViewById(R.id.viewPager);
        adapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(adapter);
        // In edit mode, allow swipe to go back to gender page
        viewPager.setUserInputEnabled(isEditMode);

        // If edit mode, start from info page (page 1)
        if (isEditMode) {
            viewPager.setCurrentItem(1, false);
        }

        // Setup fragments after ViewPager is ready
        viewPager.post(() -> {
            setupFragments();
            // Also setup when page changes
            viewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    // Setup fragment when it becomes visible
                    setupFragments();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-setup fragments in case they were recreated
        if (viewPager != null) {
            viewPager.post(() -> setupFragments());
        }
    }

    private void setupFragments() {
        // Use FragmentManager to find fragments
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        
        for (Fragment fragment : fragments) {
            if (fragment instanceof ProfileGenderFragment && !fragment.isDetached()) {
                ProfileGenderFragment genderFragment = (ProfileGenderFragment) fragment;
                genderFragment.setOnGenderSelectedListener(new ProfileGenderFragment.OnGenderSelectedListener() {
                    @Override
                    public void onGenderSelected(String gender) {
                        handleGenderSelected(gender);
                    }

                    @Override
                    public void onSkip() {
                        handleGenderSkipped();
                    }
                });
            } else if (fragment instanceof ProfileInfoFragment && !fragment.isDetached()) {
                ProfileInfoFragment infoFragment = (ProfileInfoFragment) fragment;
                infoFragment.setOnInfoCompletedListener(new ProfileInfoFragment.OnInfoCompletedListener() {
                    @Override
                    public void onInfoCompleted() {
                        handleInfoCompleted();
                    }

                    @Override
                    public void onSkip() {
                        handleInfoSkipped();
                    }
                });
            }
        }
    }

    public void handleGenderSelected(String gender) {
        viewPager.setCurrentItem(1, true);
    }

    public void handleGenderSkipped() {
        ProfileDataManager.setProfileCompleted(this, true);
        goToMain();
    }

    public void handleInfoCompleted() {
        ProfileDataManager.setProfileCompleted(this, true);
        if (isEditMode) {
            // In edit mode, just finish and go back to Settings
            finish();
        } else {
            // In onboarding mode, go to MainActivity
            goToMain();
        }
    }

    public void handleInfoSkipped() {
        ProfileDataManager.setProfileCompleted(this, true);
        if (isEditMode) {
            // In edit mode, just finish and go back to Settings
            finish();
        } else {
            // In onboarding mode, go to MainActivity
            goToMain();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private static class ProfilePagerAdapter extends FragmentStateAdapter {

        public ProfilePagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new ProfileGenderFragment();
            } else {
                return new ProfileInfoFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}

