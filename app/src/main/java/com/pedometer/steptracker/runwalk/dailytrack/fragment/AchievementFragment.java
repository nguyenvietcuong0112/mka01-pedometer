package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.pedometer.steptracker.runwalk.dailytrack.R;

public class AchievementFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_achievement, container, false);
        
        // Remove bottom navigation from fragment layout since it's in MainActivity
        View bottomNav = view.findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            ((ViewGroup) bottomNav.getParent()).removeView(bottomNav);
        }
        
        return view;
    }
}

