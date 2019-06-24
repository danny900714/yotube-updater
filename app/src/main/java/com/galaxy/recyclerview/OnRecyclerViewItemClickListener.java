package com.galaxy.recyclerview;

import android.view.View;

public interface OnRecyclerViewItemClickListener<T> {
    void onRecyclerViewItemClick(View itemView, T item);
}
