package com.galaxy.youtube.updater.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.galaxy.recyclerview.OnRecyclerViewItemClickListener;
import com.galaxy.util.ViewUtils;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.app.AppManager;

import java.util.List;
import java.util.Locale;

public class HomeAppListRecyclerAdapter extends RecyclerView.Adapter<HomeAppListRecyclerAdapter.ViewHolder> {

    private OnRecyclerViewItemClickListener<AppManager> mListener;
    private List<AppManager> mAppManagerList;

    public HomeAppListRecyclerAdapter(List<AppManager> managerList, OnRecyclerViewItemClickListener<AppManager> listener) {
        mAppManagerList = managerList;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rcl_item_home_app_list, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.mItemView.setOnClickListener(v -> {
            mListener.onRecyclerViewItemClick(holder.mItemView, holder.mManager);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppManager manager = mAppManagerList.get(position);
        holder.mManager = manager;
        View layout = holder.mItemView;

        if (position == 0) {
            layout.setPaddingRelative((int) ViewUtils.convertDpToPixel(16, layout.getContext()), layout.getPaddingTop(), layout.getPaddingEnd(), layout.getPaddingBottom());
        }

        Glide.with(holder.mItemView).load(manager.getIconReference()).into(holder.mImgIcon);
        holder.mTxtName.setText(manager.getName());
        holder.mTxtStar.setText(String.format(Locale.getDefault(), "%2.1f★", manager.getScore()));
    }

    @Override
    public int getItemCount() {
        if (mAppManagerList == null) return 0;
        return mAppManagerList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private AppManager mManager;

        private View mItemView;
        private ImageView mImgIcon;
        private TextView mTxtName, mTxtStar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
            mImgIcon = mItemView.findViewById(R.id.homeAppListImgIcon);
            mTxtName = mItemView.findViewById(R.id.homeAppListTxtName);
            mTxtStar = mItemView.findViewById(R.id.homeAppListTxtStar);
        }
    }
}
