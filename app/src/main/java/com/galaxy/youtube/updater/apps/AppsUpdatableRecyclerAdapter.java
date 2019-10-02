package com.galaxy.youtube.updater.apps;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageMetadata;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AppsUpdatableRecyclerAdapter extends RecyclerView.Adapter<AppsUpdatableRecyclerAdapter.ViewHolder>{

    private OnRecyclerViewItemClickListener listener;
    private Context context;

    private List<AppManager> appManagerList;

    public AppsUpdatableRecyclerAdapter(Context context, OnRecyclerViewItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rcl_item_apps_updatable, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRecyclerViewItemClick(holder.mItemView, holder.mItem);
            }
        });
        holder.mBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUpdateButtonClick(holder);
            }
        });
        holder.mImgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancelButtonClick(holder);
            }
        });
        holder.mImgExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.isNewFeatureExpand = !holder.isNewFeatureExpand;
                listener.onExpandNewFeatureButtonClick(holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        AppManager manager = appManagerList.get(position);
        holder.mItem = manager;

        holder.mTxtName.setText(manager.getName());
        holder.mImgIcon.setContentDescription(manager.getName());
        holder.mTxtNewFeatures.setText(manager.getNewFeature());
        if (manager.getPrice() > 0) holder.mBtnUpdate.setText("$" + manager.getPrice());

        // set icon
        Glide.with(context).load(manager.getIconReference()).into(holder.mImgIcon);

        // get app size
        manager.getApkReference().getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                holder.mTxtSize.setText(Formatter.formatShortFileSize(context, storageMetadata.getSizeBytes()));
            }
        });
    }

    public void setAppManagerList(List<AppManager> appManagerList) {
        this.appManagerList = appManagerList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (appManagerList == null)
            return 0;
        else
            return appManagerList.size();
    }

    public List<AppManager> getAllItems() {
        return appManagerList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private AppManager mItem;

        private View mItemView;
        private ImageView mImgIcon, mImgExpand, mImgCancel;
        private TextView mTxtName, mTxtSize, mTxtNewFeatures;
        private Button mBtnUpdate;
        private ConstraintLayout mLayNewFeature;

        private boolean isNewFeatureExpand = false;

        private ViewHolder(@NonNull View mItemView) {
            super(mItemView);
            this.mItemView = mItemView;
            mImgIcon = mItemView.findViewById(R.id.appsUpdatableIcon);
            mImgExpand = mItemView.findViewById(R.id.appsUpdatableImgExpand);
            mImgCancel = mItemView.findViewById(R.id.appsUpdatableImgCancel);
            mTxtName = mItemView.findViewById(R.id.appsUpdatableTxtName);
            mTxtSize = mItemView.findViewById(R.id.appsUpdatableTxtSize);
            mTxtNewFeatures = mItemView.findViewById(R.id.appsUpdatableTxtNewFeature);
            mBtnUpdate = mItemView.findViewById(R.id.appsUpdatableBtnUpdate);
            mLayNewFeature = mItemView.findViewById(R.id.appsUpdatableLayNewFeature);
        }

        public void showStartDownloadView() {
            mBtnUpdate.setVisibility(View.INVISIBLE);
            mImgCancel.setVisibility(View.VISIBLE);
        }

        public void showDefaultView() {
            mImgIcon.setVisibility(View.VISIBLE);
            mImgExpand.setVisibility(View.VISIBLE);
            mImgCancel.setVisibility(View.INVISIBLE);
            mTxtName.setVisibility(View.VISIBLE);
            mTxtSize.setVisibility(View.VISIBLE);
            mBtnUpdate.setVisibility(View.VISIBLE);
        }

        public void toggleNewFeature() {
            if (isNewFeatureExpand) {
                mLayNewFeature.setVisibility(View.GONE);
                mImgExpand.setImageResource(R.drawable.ic_expand_more_black_18dp);
            } else {
                mLayNewFeature.setVisibility(View.VISIBLE);
                mImgExpand.setImageResource(R.drawable.ic_expand_less_black_18dp);
            }
        }

        public AppManager getAppManager() {
            return mItem;
        }

        public boolean isNewFeatureExpand() {
            return isNewFeatureExpand;
        }
    }

    public interface OnRecyclerViewItemClickListener extends com.galaxy.recyclerview.OnRecyclerViewItemClickListener<AppManager> {
        void onUpdateButtonClick(ViewHolder viewHolder);
        void onCancelButtonClick(ViewHolder viewHolder);
        void onExpandNewFeatureButtonClick(ViewHolder viewHolder);
    }
}
