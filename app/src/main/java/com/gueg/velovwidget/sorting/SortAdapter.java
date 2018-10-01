package com.gueg.velovwidget.sorting;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gueg.velovwidget.R;
import com.gueg.velovwidget.Item;

import java.util.ArrayList;
import java.util.Collections;

public class SortAdapter extends RecyclerView.Adapter<SortAdapter.ViewHolder> {

    private ArrayList<Item> mList;


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        ImageView mImageView;

        ViewHolder(View v) {
            super(v);
            mTextView = v.findViewById(R.id.row_sort_title);
            mImageView = v.findViewById(R.id.row_sort_type);
        }
    }


    SortAdapter(ArrayList<Item> list) {
        mList = list;
    }

    @NonNull
    @Override
    public SortAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sort, parent, false);
        return new SortAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SortAdapter.ViewHolder holder, final int position) {
        holder.mTextView.setText(mList.get(position).name);
        holder.mImageView.setImageDrawable(mList.get(position).isSeparator()?
                holder.mImageView.getContext().getResources().getDrawable(R.drawable.ic_sort):
                holder.mImageView.getContext().getResources().getDrawable(R.drawable.ic_velo));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }





}