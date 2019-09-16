package com.galaxy.youtube.updater.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.galaxy.recyclerview.OnRecyclerViewItemClickListener;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.data.cluster.ClustersManager;

public class HomeAppsRecyclerAdapter extends RecyclerView.Adapter<HomeAppsRecyclerAdapter.ViewHolder> {
    private OnInteractionListener mListener;
    private ClustersManager mClustersManager;
    private Context mContext;

    public HomeAppsRecyclerAdapter(Context context, ClustersManager manager, OnInteractionListener listener) {
        mContext = context;
        mClustersManager = manager;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rcl_item_home_apps, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTxtTitle.setText(mClustersManager.getClusterTitle(position));

        // init recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        holder.mRclAppList.setLayoutManager(layoutManager);
        mClustersManager.getClusterApps(position, managerList -> {
            HomeAppListRecyclerAdapter mAdapter = new HomeAppListRecyclerAdapter(managerList, (view, manager) -> mListener.onAppLayoutClick(view, manager));
            holder.mRclAppList.setAdapter(mAdapter);
        });
    }

    @Override
    public int getItemCount() {
        return mClustersManager.getClusterCount();
    }

    public void setClustersManager(ClustersManager clustersManager) {
        this.mClustersManager = clustersManager;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View mItemView;
        private TextView mTxtTitle;
        private RecyclerView mRclAppList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
            mTxtTitle = mItemView.findViewById(R.id.homeAppsTxtTitle);
            mRclAppList = mItemView.findViewById(R.id.homeAppsRclAppList);
        }
    }

    public interface OnInteractionListener extends OnRecyclerViewItemClickListener<ClustersManager> {
        void onAppLayoutClick(View view, AppManager manager);
    }
}
