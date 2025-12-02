package com.pedometer.steptracker.runwalk.dailytrack.utils.language;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.pedometer.steptracker.runwalk.dailytrack.R;
import com.pedometer.steptracker.runwalk.dailytrack.adapter.LanguageModel;
import com.pedometer.steptracker.runwalk.dailytrack.databinding.ItemCustomLanguageBinding;

import java.util.ArrayList;

public class LanguageCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<LanguageModel> data = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private int checkedPosition = -1;


    public LanguageCustomAdapter( ArrayList<LanguageModel> newData) {

        this.data = newData;

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCustomLanguageBinding binding = ItemCustomLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

        return new TabItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TabItemViewHolder) holder).bind(position);
    }

    class TabItemViewHolder extends RecyclerView.ViewHolder {

        private ItemCustomLanguageBinding binding;

        public TabItemViewHolder(ItemCustomLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }

        public void bind(int position) {
            LanguageModel languageModel = data.get(position);
            binding.txtName.setText(languageModel.getLanguageName());
            if (languageModel.getImage() == 0) {
                binding.imgFlag.setVisibility(View.GONE);
            } else {
                binding.imgFlag.setVisibility(View.VISIBLE);
                binding.imgFlag.setImageResource(languageModel.getImage());
            }

            boolean isEnglish = languageModel.getLanguageName().equals(
                    binding.getRoot().getContext().getString(R.string.english_us)
            );
            boolean isNoneSelected = (checkedPosition == -1);

            if (isEnglish && isNoneSelected) {
                binding.animHand.setVisibility(View.VISIBLE);
            } else {
                binding.animHand.setVisibility(View.GONE);
            }

            if (checkedPosition == -1) {
                binding.ctr1.setBackgroundResource(R.drawable.bg_language_custom);
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    binding.ctr1.setBackgroundResource(R.drawable.bg_language_custom_check);
                } else {
                    binding.ctr1.setBackgroundResource(R.drawable.bg_language_custom);
                }
            }
            binding.getRoot().setOnClickListener(v -> {
                onItemClickListener.onPreviousPosition(checkedPosition);
                binding.ctr1.setBackgroundResource(R.drawable.bg_language_custom_check);
                if (checkedPosition != getAdapterPosition()) {
                    notifyItemChanged(checkedPosition);
                    checkedPosition = getAdapterPosition();
                    onItemClickListener.onItemNewClick(position, languageModel);
                }
                notifyDataSetChanged();
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemNewClick(int pos, LanguageModel itemTabModel);

        void onPreviousPosition(int pos);
    }

    public void unselectAll() {
        checkedPosition = -1;
        notifyDataSetChanged();
    }
}
