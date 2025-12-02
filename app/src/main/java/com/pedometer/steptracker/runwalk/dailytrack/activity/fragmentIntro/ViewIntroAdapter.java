package com.pedometer.steptracker.runwalk.dailytrack.activity.fragmentIntro;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewIntroAdapter extends FragmentStateAdapter {
    FragmentActivity fragmentActivity;
    private ArrayList<Fragment> fragmentList;

    public ViewIntroAdapter(FragmentActivity fragmentActivity, ArrayList<Fragment> list, @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        this.fragmentList = list;
        this.fragmentActivity = fragmentActivity;

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }


    @Override
    public int getItemCount() {
        if (fragmentList == null) {
            return 0;
        }
        return fragmentList.size();
    }
}
