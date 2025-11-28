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

    private boolean isHand;

    private boolean isFirst = true;


    public LanguageCustomAdapter(ArrayList<LanguageModel> newData, boolean isHand) {

        this.data = newData;
        this.isHand = isHand;
    }


    public void updateData(ArrayList<LanguageModel> newData) {
        data.clear();
        if (newData != null && !newData.isEmpty()) {
            data.addAll(newData);
        }
        notifyDataSetChanged();
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
            binding.tvTitle.setText(languageModel.getLanguageName());
            if (languageModel.getImage() == 0) {
                binding.ivAvatar.setVisibility(View.GONE);
            } else {
                binding.ivAvatar.setVisibility(View.VISIBLE);
                binding.ivAvatar.setImageResource(languageModel.getImage());
            }
            if (position == 1 && isHand && isFirst) {
                binding.animHand.setVisibility(View.VISIBLE);
            } else {
                binding.animHand.setVisibility(View.GONE);
            }
            if (checkedPosition == -1) {
                binding.imgSelected.setImageResource(R.drawable.ic_unselect_language);
            } else {
                if (checkedPosition == getAdapterPosition()) {
                    binding.imgSelected.setImageResource(R.drawable.ic_checked_language);
                } else {
                    binding.imgSelected.setImageResource(R.drawable.ic_unselect_language);
                }
            }
            binding.getRoot().setOnClickListener(v -> {
                binding.imgSelected.setImageResource(R.drawable.ic_checked_language);
                isFirst = false;
                binding.animHand.setVisibility(View.GONE);
                if (checkedPosition != getAdapterPosition()) {
                    notifyItemChanged(checkedPosition);
                    checkedPosition = getAdapterPosition();
                    onItemClickListener.onItemNewClick(position, languageModel, LanguageCustomAdapter.this);
                }
            });
        }

    }


    public void hideAnimHand() {
        isFirst = false;
        notifyItemChanged(1);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener {
        void onItemNewClick(int pos, LanguageModel itemTabModel, LanguageCustomAdapter adapter);

        void onPreviousPosition(int pos);
    }

    public void unselectAll() {
        checkedPosition = -1;
        notifyDataSetChanged();
    }
}
