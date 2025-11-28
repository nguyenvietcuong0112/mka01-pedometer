package com.pedometer.steptracker.runwalk.dailytrack.adapter;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ItemLanguageBinding;

import java.util.List;


public class LanguageStartAdapter extends RecyclerView.Adapter<LanguageStartAdapter.LanguageViewHolder> {

    private Context context;
    private List<LanguageModel> lists;
    private IClickLanguage iClickLanguage;

    public interface IClickLanguage {
        void onClick(LanguageModel model);
    }

    public LanguageStartAdapter(Context context, List<LanguageModel> lists, IClickLanguage iClickLanguage) {
        this.context = context;
        this.lists = lists;
        this.iClickLanguage = iClickLanguage;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLanguageBinding binding = ItemLanguageBinding.inflate(LayoutInflater.from(context), parent, false);
        return new LanguageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageModel data = lists.get(position);
        holder.bind(data, context, position);

        holder.binding.rlItem.setOnClickListener(view -> {
            setSelectLanguage(data.getIsoLanguage());
            iClickLanguage.onClick(data);
            for (LanguageModel item : lists) {
//                item.setHandVisible(false);
            }
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    private void setSelectLanguage(String code) {
        for (LanguageModel data : lists) {
            data.setCheck(data.getIsoLanguage().equals(code));
        }
        notifyDataSetChanged();
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        private final ItemLanguageBinding binding;

        public LanguageViewHolder(ItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(LanguageModel data, Context context, int position) {
//            if (data.isHandVisible() && position == 2) {
//                binding.animHand.setVisibility(View.VISIBLE);
//            } else {
//                binding.animHand.setVisibility(View.INVISIBLE);
//            }

            binding.ivAvatar.setImageDrawable(context.getDrawable(data.getImage()));
            binding.tvTitle.setText(data.getLanguageName());

            if (data.getCheck()) {
                binding.getRoot().setBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.v2.setVisibility(View.VISIBLE);
                binding.rlItem.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_green_language));

            } else {
                binding.getRoot().setBackgroundColor(Color.parseColor("#FFFFFF"));
                binding.rlItem.setBackgroundColor(Color.TRANSPARENT);
                binding.rlItem.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_green_language));
                binding.v2.setVisibility(View.GONE);
            }
        }
    }
}