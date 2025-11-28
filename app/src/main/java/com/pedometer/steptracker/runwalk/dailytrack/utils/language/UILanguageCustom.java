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
    private LanguageCustomAdapter adapterEng;

    private LanguageCustomAdapter adapterPor;


    private LanguageCustomAdapter adapterHindi;

    private LanguageCustomAdapter adapterLanguageOther;
    boolean isVisibleHindi = false;
    boolean isVisibleEng = false;

    boolean isVisiblePor = false;

    private Context context;
    private final ArrayList<LanguageModel> dataEng = new ArrayList<>();

    private final ArrayList<LanguageModel> dataPor = new ArrayList<>();

    private final ArrayList<LanguageModel> dataHindi = new ArrayList<>();

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
        binding.languageES.ivAvatar.setImageResource(R.drawable.flag_es);


        binding.languageHindi.ivAvatar.setImageResource(R.drawable.flag_hi);

        adapterEng = new LanguageCustomAdapter(dataEng,true);
        adapterEng.setOnItemClickListener(this);
        binding.rcvLanguageCollap1.setAdapter(adapterEng);

        adapterHindi = new LanguageCustomAdapter(dataHindi,false);
        adapterHindi.setOnItemClickListener(this);
        binding.rcvLanguageCollap2.setAdapter(adapterHindi);


        adapterLanguageOther = new LanguageCustomAdapter(dataOther,false);
        adapterLanguageOther.setOnItemClickListener(this);
        binding.rcvLanguage4.setAdapter(adapterLanguageOther);

        binding.languageHindi.imgCountries.setVisibility(GONE);
        binding.languageHindi.animHand.setVisibility(GONE);
        binding.languagePor.animHand.setVisibility(GONE);

        binding.languagePor.imgCountries.setImageResource(R.drawable.img_por);

        adapterPor = new LanguageCustomAdapter(dataPor,false);
        adapterPor.setOnItemClickListener(this);
        binding.rcvLanguageCollap3.setAdapter(adapterPor);
        binding.languageES.llNotColap.setOnClickListener(v -> {
            binding.languageES.imgSelected.setImageResource(R.drawable.ic_checked_language);
            binding.languageFR.imgSelected.setImageResource(R.drawable.ic_unselect_language);
            adapterPor.unselectAll();
            adapterEng.unselectAll();
            adapterHindi.unselectAll();
            adapterLanguageOther.unselectAll();
            adapterEng.hideAnimHand();
            isItemLanguageSelected = true;
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0, isItemLanguageSelected, "es");
            }

            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });
        binding.languageFR.llNotColap.setOnClickListener(v -> {
            binding.languageES.imgSelected.setImageResource(R.drawable.ic_unselect_language);
            binding.languageFR.imgSelected.setImageResource(R.drawable.ic_checked_language);
            adapterPor.unselectAll();
            adapterEng.unselectAll();
            adapterHindi.unselectAll();
            adapterEng.hideAnimHand();
            adapterLanguageOther.unselectAll();
            isItemLanguageSelected = true;
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0, isItemLanguageSelected, "fr");
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });
        binding.languageES.tvTitle.setText(context.getString(R.string.spanish));
        binding.languageHindi.tvTitle.setText(context.getString(R.string.hindi));
        binding.languageFR.tvTitle.setText(context.getString(R.string.french));
        binding.languagePor.tvTitle.setText(context.getString(R.string.portuguese));
        binding.languageEnglishCollapse.tvTitle.setText(context.getString(R.string.english));
        binding.languageFR.ivAvatar.setImageResource(R.drawable.flag_fr);
        binding.languagePor.ivAvatar.setImageResource(R.drawable.flag_collap_pt);
        binding.languageEnglishCollapse.ivAvatar.setImageResource(R.drawable.flag_collap_en);
        binding.languageHindi.itemCollap.setOnClickListener(v -> {
            isVisibleHindi = !isVisibleHindi;
            binding.rcvLanguageCollap2.setVisibility(isVisibleHindi ? View.VISIBLE : View.GONE);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0, isItemLanguageSelected, "");
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });
        binding.languageEnglishCollapse.itemCollap.setOnClickListener(v -> {
            isVisibleEng = !isVisibleEng;
            binding.rcvLanguageCollap1.setVisibility(isVisibleEng ? View.VISIBLE : View.GONE);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0, isItemLanguageSelected, "");
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);

        });

        binding.languagePor.itemCollap.setOnClickListener(v -> {
            isVisiblePor = !isVisiblePor;
            binding.rcvLanguageCollap3.setVisibility(isVisiblePor ? View.VISIBLE : View.GONE);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0, isItemLanguageSelected, "");
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public void upDateData(ArrayList<LanguageModel> dataEng1, ArrayList<LanguageModel> hindi, ArrayList<LanguageModel> dataPor1  , ArrayList<LanguageModel> dataOthe) {
        dataPor.clear();
        dataHindi.clear();
        dataEng.clear();
        dataOther.clear();
        if (dataPor1 != null && !dataPor1.isEmpty()) {
            dataPor.addAll(dataPor1);
        }
        if (hindi != null && !hindi.isEmpty()) {
            dataHindi.addAll(hindi);
        }
        if (dataEng1 != null && !dataEng1.isEmpty()) {
            dataEng.addAll(dataEng1);
        }
        if (dataOthe != null && !dataOthe.isEmpty()) {
            dataOther.addAll(dataOthe);
        }
        binding.languageES.tvTitle.setText(context.getString(R.string.spanish));
        binding.languageHindi.tvTitle.setText(context.getString(R.string.hindi));
        binding.languageFR.tvTitle.setText(context.getString(R.string.french));
        binding.languagePor.tvTitle.setText(context.getString(R.string.portuguese));
        binding.languageEnglishCollapse.tvTitle.setText(context.getString(R.string.english));
        adapterPor.notifyDataSetChanged();
        adapterHindi.notifyDataSetChanged();
        adapterEng.notifyDataSetChanged();
        adapterLanguageOther.notifyDataSetChanged();

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onItemNewClick(int position, LanguageModel language, LanguageCustomAdapter adapter) {
        isItemLanguageSelected = true;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClickListener(position, isItemLanguageSelected, language.isoLanguage);
        }
        binding.languageFR.imgSelected.setImageDrawable(null);
        binding.languageES.imgSelected.setImageDrawable(null);
        if (adapter == adapterEng) {
            adapterPor.unselectAll();
            adapterHindi.unselectAll();
            adapterLanguageOther.unselectAll();
        } else if (adapter == adapterPor) {
            adapterEng.unselectAll();
            adapterHindi.unselectAll();
            adapterLanguageOther.unselectAll();
        } else if (adapter == adapterHindi) {
            adapterPor.unselectAll();
            adapterEng.unselectAll();
            adapterLanguageOther.unselectAll();
        } else if (adapter == adapterLanguageOther) {
            adapterPor.unselectAll();
            adapterHindi.unselectAll();
            adapterEng.unselectAll();
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

