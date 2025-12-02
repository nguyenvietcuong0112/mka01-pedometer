package com.pedometer.steptracker.runwalk.dailytrack.utils.language;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;


import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.adapter.LanguageModel;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.LayoutLanguageCustomBinding;

import java.util.ArrayList;

public class UILanguageCustom extends RelativeLayout implements LanguageCustomAdapter.OnItemClickListener {

    private LanguageCustomAdapter adapterLanguageOther;
    private Context context;

    private final ArrayList<LanguageModel> dataOther = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private LayoutLanguageCustomBinding binding;

    private boolean isItemLanguageSelected = false;

    public UILanguageCustom(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public UILanguageCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {

        binding = LayoutLanguageCustomBinding.inflate(LayoutInflater.from(context), this, true);

        adapterLanguageOther = new LanguageCustomAdapter(dataOther);
        adapterLanguageOther.setOnItemClickListener(this);
        binding.rcvLanguage4.setAdapter(adapterLanguageOther);


    }

    public void upDateData(ArrayList<LanguageModel> dataOthe) {
        dataOther.clear();
        if (dataOthe != null && !dataOthe.isEmpty()) {
            dataOther.addAll(dataOthe);
        }
        adapterLanguageOther.notifyDataSetChanged();

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onItemNewClick(int position, LanguageModel language) {
        isItemLanguageSelected = true;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClickListener(position, isItemLanguageSelected, language.isoLanguage);
        }
    }

    @Override
    public void onPreviousPosition(int pos) {
        if (onItemClickListener != null) {
            onItemClickListener.onPreviousPosition(pos);
        }
    }

    public interface OnItemClickListener {
        void onItemClickListener(int position, boolean isItemLanguageSelected, String codeLang);

        void onPreviousPosition(int pos);
    }
}
