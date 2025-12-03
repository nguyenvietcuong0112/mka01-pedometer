package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;

public class ProfileGenderFragment extends Fragment {

    private FrameLayout femaleContainer, maleContainer;
    private Button btnNext, btnSkip;
    private String selectedGender = "";

    public interface OnGenderSelectedListener {
        void onGenderSelected(String gender);
        void onSkip();
    }

    private OnGenderSelectedListener listener;

    public void setOnGenderSelectedListener(OnGenderSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_gender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        femaleContainer = view.findViewById(R.id.femaleContainer);
        maleContainer = view.findViewById(R.id.maleContainer);
        btnNext = view.findViewById(R.id.btnNext);
        btnSkip = view.findViewById(R.id.btnSkip);

        // Load saved gender if exists
        String savedGender = ProfileDataManager.getGender(requireContext());
        if (!savedGender.isEmpty()) {
            selectedGender = savedGender;
            updateSelection();
        }

        femaleContainer.setOnClickListener(v -> {
            selectedGender = "female";
            updateSelection();
        });

        maleContainer.setOnClickListener(v -> {
            selectedGender = "male";
            updateSelection();
        });

        btnNext.setOnClickListener(v -> {
            if (!selectedGender.isEmpty()) {
                ProfileDataManager.saveGender(requireContext(), selectedGender);
                if (listener != null) {
                    listener.onGenderSelected(selectedGender);
                } else {
                    // Fallback: call activity directly
                    if (getActivity() instanceof com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) {
                        ((com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) getActivity()).handleGenderSelected(selectedGender);
                    }
                }
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSkip();
            } else {
                // Fallback: call activity directly
                if (getActivity() instanceof com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) {
                    ((com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) getActivity()).handleGenderSkipped();
                }
            }
        });

        updateNextButton();
    }

    private void updateSelection() {
        if (selectedGender.equals("female")) {
            femaleContainer.setBackgroundResource(R.drawable.bg_gender_circle_selected);
            maleContainer.setBackgroundResource(R.drawable.bg_gender_circle);
        } else if (selectedGender.equals("male")) {
            femaleContainer.setBackgroundResource(R.drawable.bg_gender_circle);
            maleContainer.setBackgroundResource(R.drawable.bg_gender_circle_selected);
        }
        updateNextButton();
    }

    private void updateNextButton() {
        btnNext.setEnabled(!selectedGender.isEmpty());
        btnNext.setAlpha(selectedGender.isEmpty() ? 0.5f : 1.0f);
    }
}

