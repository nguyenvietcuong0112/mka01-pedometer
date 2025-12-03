package com.pedometer.steptracker.runwalk.dailytrack.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.utils.ProfileDataManager;

public class ProfileInfoFragment extends Fragment {

    private EditText etName, etHeight, etWeight, etWeightGoal;
    private Button btnNext, btnSkip;

    public interface OnInfoCompletedListener {
        void onInfoCompleted();
        void onSkip();
    }

    private OnInfoCompletedListener listener;

    public void setOnInfoCompletedListener(OnInfoCompletedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etHeight = view.findViewById(R.id.etHeight);
        etWeight = view.findViewById(R.id.etWeight);
        etWeightGoal = view.findViewById(R.id.etWeightGoal);
        btnNext = view.findViewById(R.id.btnNext);
        btnSkip = view.findViewById(R.id.btnSkip);

        // Load saved data if exists
        loadSavedData();

        btnNext.setOnClickListener(v -> {
            saveData();
            if (listener != null) {
                listener.onInfoCompleted();
            } else {
                // Fallback: call activity directly
                if (getActivity() instanceof com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) {
                    ((com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) getActivity()).handleInfoCompleted();
                }
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSkip();
            } else {
                // Fallback: call activity directly
                if (getActivity() instanceof com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) {
                    ((com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) getActivity()).handleInfoSkipped();
                }
            }
        });

        // In edit mode, hide skip button and change Next to Save
        checkEditMode();
    }

    private void loadSavedData() {
        String name = ProfileDataManager.getName(requireContext());
        float height = ProfileDataManager.getHeight(requireContext());
        float weight = ProfileDataManager.getWeight(requireContext());
        float weightGoal = ProfileDataManager.getWeightGoal(requireContext());

        if (!name.isEmpty()) {
            etName.setText(name);
        }
        if (height > 0) {
            etHeight.setText(String.valueOf((int) height));
        }
        if (weight > 0) {
            etWeight.setText(String.valueOf(weight));
        }
        if (weightGoal > 0) {
            etWeightGoal.setText(String.valueOf(weightGoal));
        }
    }

    private void saveData() {
        String name = etName.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String weightGoalStr = etWeightGoal.getText().toString().trim();

        if (!name.isEmpty()) {
            ProfileDataManager.saveName(requireContext(), name);
        }

        try {
            if (!heightStr.isEmpty()) {
                float height = Float.parseFloat(heightStr);
                ProfileDataManager.saveHeight(requireContext(), height);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        try {
            if (!weightStr.isEmpty()) {
                float weight = Float.parseFloat(weightStr);
                ProfileDataManager.saveWeight(requireContext(), weight);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }

        try {
            if (!weightGoalStr.isEmpty()) {
                float weightGoal = Float.parseFloat(weightGoalStr);
                ProfileDataManager.saveWeightGoal(requireContext(), weightGoal);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
    }

    private void checkEditMode() {
        if (getActivity() instanceof com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) {
            com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity activity = 
                (com.pedometer.steptracker.runwalk.dailytrack.activity.ProfileActivity) getActivity();
            if (activity != null && activity.isEditMode()) {
                if (btnSkip != null) {
                    btnSkip.setVisibility(View.GONE);
                }
                if (btnNext != null) {
                    btnNext.setText("Save");
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkEditMode();
    }
}

