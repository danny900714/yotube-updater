package com.galaxy.youtube.updater.apps;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.galaxy.youtube.updater.BuildConfig;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.data.user.UserManager;
import com.galaxy.youtube.updater.dialog.EarnMoneyDialog;
import com.galaxy.youtube.updater.service.install.InstallService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AppsFragment extends Fragment implements AppsUpdatableRecyclerAdapter.OnRecyclerViewItemClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // private static final String ARG_PARAM1 = "param1";
    // private static final String ARG_PARAM2 = "param2";


    // private String mParam1;
    // private String mParam2;

    private OnFragmentInteractionListener mListener;
    private FirebaseFirestore mDb;
    private AppsUpdatableRecyclerAdapter adapter;
    private RewardedAd mRewardedAd;

    private TextView mTxtCheckingUpdate, mTxtUpdateInfo, mTxtUpdateSubInfo, mTxtNoUpdate;
    private ProgressBar mPrgBar;
    private View mDivider;
    private RecyclerView mRlcUpdatable;
    private Button mBtnUpdateAll;
    private ImageView mImgAllLatest;

    private List<String> installedPackagesName = new ArrayList<>();

    public AppsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppsFragment.
     */
    public static AppsFragment newInstance(/*String param1, String param2*/) {
        AppsFragment fragment = new AppsFragment();
        Bundle args = new Bundle();
        // args.putString(ARG_PARAM1, param1);
        // args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           //  mParam1 = getArguments().getString(ARG_PARAM1);
            // mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initViews();

        // init firebase
        mDb = FirebaseFirestore.getInstance();

        // init recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRlcUpdatable.setLayoutManager(layoutManager);
        mRlcUpdatable.setNestedScrollingEnabled(false);
        adapter = new AppsUpdatableRecyclerAdapter(getContext(), this);
        mRlcUpdatable.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        mRlcUpdatable.addItemDecoration(dividerItemDecoration);

        // create and load ad
        // mRewardedAd = createAndLoadRewardedAd();

        // get updatable apps
        AppManager.getUpdatableApps(getContext(), new AppManager.OnAppDataListRetrieveListener() {
            @Override
            public void onAppDataListRetrieve(List<AppManager> appManagers) {
                mTxtCheckingUpdate.setVisibility(View.INVISIBLE);
                mPrgBar.setVisibility(View.INVISIBLE);

                if (appManagers.size() == 0) {
                    mTxtUpdateInfo.setVisibility(View.INVISIBLE);
                    mTxtUpdateSubInfo.setVisibility(View.INVISIBLE);
                    mBtnUpdateAll.setVisibility(View.INVISIBLE);
                    mTxtNoUpdate.setVisibility(View.VISIBLE);
                    mImgAllLatest.setVisibility(View.VISIBLE);
                } else {
                    mTxtNoUpdate.setVisibility(View.INVISIBLE);
                    mImgAllLatest.setVisibility(View.INVISIBLE);
                    mTxtUpdateInfo.setVisibility(View.VISIBLE);
                    mTxtUpdateSubInfo.setVisibility(View.VISIBLE);
                    mBtnUpdateAll.setVisibility(View.VISIBLE);

                    // set updatable count
                    mTxtUpdateInfo.setText(String.format(getString(R.string.available_updates), appManagers.size()));
                }

                adapter.setAppManagerList(appManagers);
                adapter.notifyDataSetChanged();
            }
        });

        // set listeners
        mBtnUpdateAll.setOnClickListener(onBtnUpdateAllClick);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRecyclerViewItemClick(View itemView, AppManager item) {

    }

    @Override
    public void onUpdateButtonClick(final AppsUpdatableRecyclerAdapter.ViewHolder viewHolder) {
        // show ad
        /*if (mRewardedAd.isLoaded()) {
            RewardedAdCallback adCallback = new RewardedAdCallback() {
                public void onRewardedAdOpened() {
                    // Ad opened.
                }

                public void onRewardedAdClosed() {
                    // Ad closed.
                }

                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    // User earned reward.

                }

                public void onRewardedAdFailedToShow(int errorCode) {
                    // Ad failed to display
                }
            };
            mRewardedAd.show(getActivity(), adCallback);
        }*/

        UserManager.getInstance(FirebaseAuth.getInstance().getUid(), userManager -> {
            AppManager appManager = viewHolder.getAppManager();

            /* if (userManager.getMoney() - appManager.getPrice() < 0) {
                EarnMoneyDialog dialog = new EarnMoneyDialog();
                dialog.show(getActivity().getSupportFragmentManager(), EarnMoneyDialog.TAG);
            } else {
                userManager.setMoney(userManager.getMoney() - appManager.getPrice());
                userManager.updateChanges(false); */
            if (appManager.getApkUrl().startsWith("gs://")) InstallService.startActionNormalInstall(getContext(), appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(getContext()), appManager.getApkReference().getPath());
            else InstallService.startActionNormalUrlInstall(getContext(), appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(getContext()), appManager.getApkUrl());
            // }
        });
    }

    @Override
    public void onCancelButtonClick(AppsUpdatableRecyclerAdapter.ViewHolder viewHolder) {
        viewHolder.getAppManager().cancelDownloadApk();
    }

    @Override
    public void onExpandNewFeatureButtonClick(AppsUpdatableRecyclerAdapter.ViewHolder viewHolder) {
        viewHolder.toggleNewFeature();
    }

    private View.OnClickListener onBtnUpdateAllClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<AppManager> appManagerList = adapter.getAllItems();
            if (appManagerList == null || appManagerList.size() == 0)
                return;

            for (AppManager appManager: appManagerList) {
                if (appManager.getApkUrl().startsWith("gs://")) InstallService.startActionNormalInstall(getContext(), appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(getContext()), appManager.getApkReference().getPath());
                else InstallService.startActionNormalUrlInstall(getContext(), appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(getContext()), appManager.getApkUrl());
            }
        }
    };

    private RewardedAd createAndLoadRewardedAd() {
        RewardedAd rewardedAd;
        if (BuildConfig.DEBUG)
            rewardedAd = new RewardedAd(getContext(), "ca-app-pub-3940256099942544/5224354917");
        else
            rewardedAd = new RewardedAd(getContext(), "ca-app-pub-9369855966075970/2011572909");

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
                Log.d(AppsFragment.class.getSimpleName(), "ad loaded");
            }

            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
                Log.w(AppsFragment.class.getSimpleName(), "Ad load failed");
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
        return rewardedAd;
    }

    private void initViews() {
        mTxtCheckingUpdate = getView().findViewById(R.id.appsTxtCheckUpdate);
        mTxtUpdateInfo = getView().findViewById(R.id.appsTxtUpdateInfo);
        mTxtUpdateSubInfo = getView().findViewById(R.id.appsTxtUpdateSubinfo);
        mPrgBar = getView().findViewById(R.id.appsPrgBar);
        mDivider = getView().findViewById(R.id.appsDiv);
        mRlcUpdatable = getView().findViewById(R.id.appsUpdatableRcl);
        mBtnUpdateAll = getView().findViewById(R.id.appsBtnUpdateAll);
        mTxtNoUpdate = getView().findViewById(R.id.appsUpdatableTxtNoUpdate);
        mImgAllLatest = getView().findViewById(R.id.appsUpdatableImgAllLatest);
    }

    public interface OnFragmentInteractionListener {

    }
}
