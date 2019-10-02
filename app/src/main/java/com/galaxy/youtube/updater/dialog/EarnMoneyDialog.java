package com.galaxy.youtube.updater.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.user.UserManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class EarnMoneyDialog extends DialogFragment {

    public static final String TAG = EarnMoneyDialog.class.getName() + ".TAG";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private RewardedAd mRewardedAd;
    private int userMoney = 0;

    private ImageButton mImgBtnClose;
    private TextView mTxtMoneyDivision;
    private ProgressBar mProgress, mProgressBar;
    private Button mBtnWatch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_earn_money, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        // init views
        mImgBtnClose = getView().findViewById(R.id.earnMoneyBtnClose);
        mTxtMoneyDivision = getView().findViewById(R.id.earnMoneyTxtMoneyDivision);
        mProgress = getView().findViewById(R.id.earnMoneyProgress);
        mProgressBar = getView().findViewById(R.id.earnMoneyProgressBar);
        mBtnWatch = getView().findViewById(R.id.earnMoneyBtnWatch);

        // TODO: pass uid by fragment argument
        // get user money
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        UserManager.getListenableInstance(uid, userManager -> {
            if (isVisible()) {
                int money = userManager.getMoney();
                userMoney = money;
                mTxtMoneyDivision.setText(String.format(getString(R.string.money_division), money, 100));
                mProgressBar.setProgress(money);

                if (money >= 100) mBtnWatch.setEnabled(false);

                mProgress.setVisibility(View.GONE);
                mTxtMoneyDivision.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mBtnWatch.setVisibility(View.VISIBLE);
            }
        });

        // init ads
        MobileAds.initialize(getContext(), initializationStatus -> {

        });
        loadNewAd();

        mImgBtnClose.setOnClickListener(v -> this.dismiss());
        mBtnWatch.setOnClickListener(v -> {
            if (mRewardedAd.isLoaded()) {
                RewardedAdCallback adCallback = new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        mBtnWatch.setEnabled(false);
                        loadNewAd();

                        // add money
                        UserManager.getInstance(mAuth.getUid(), userManager -> {
                            userManager.setMoney(userManager.getMoney() + 10);
                            userManager.updateChanges(false);
                        });
                    }

                    @Override
                    public void onRewardedAdFailedToShow(int i) {
                        super.onRewardedAdFailedToShow(i);
                        mBtnWatch.setEnabled(false);
                        loadNewAd();
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        super.onRewardedAdClosed();
                        mBtnWatch.setEnabled(false);
                        loadNewAd();
                    }
                };
                mRewardedAd.show(getActivity(), adCallback);
            }
        });
    }

    private void loadNewAd() {
        mRewardedAd = new RewardedAd(getContext(), "ca-app-pub-9369855966075970/2011572909");
        mRewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    private void notifyBtnWatchAdLoaded() {
        if (userMoney < 100) mBtnWatch.setEnabled(true);
    }

    private RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onRewardedAdLoaded() {
            super.onRewardedAdLoaded();
            notifyBtnWatchAdLoaded();
        }

        @Override
        public void onRewardedAdFailedToLoad(int i) {
            super.onRewardedAdFailedToLoad(i);
            Log.w(EarnMoneyDialog.class.getSimpleName(), "Ads cannot load, return code: " + i);
            switch (i) {
                case 3:
                    Toast.makeText(getContext(), R.string.toast_no_ads, Toast.LENGTH_LONG).show();
            }
        }
    };
}
