package com.galaxy.youtube.updater.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.icu.text.NumberFormat;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.data.user.UserManager;
import com.galaxy.youtube.updater.dialog.EarnMoneyDialog;
import com.galaxy.youtube.updater.service.install.InstallService;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.galaxy.youtube.updater.R;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DescriptionActivity extends AppCompatActivity {

    public static final String KEY_PACKAGE_NAME = DescriptionActivity.class.getName() + ".PACKAGE_NAME";

    private InstallService mService;
    private boolean isBound = false;

    private TextView mTxtName, mTxtDeveloper, mTxtExtra, mTxtStar, mTxtDownloads, mTxtAge, mTxtAbout;
    private ImageView mImgIcon;
    private MaterialButton mBtnInstall, mBtnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get correspond app manager and app info
        Intent it = getIntent();
        String packageName = it.getStringExtra(KEY_PACKAGE_NAME);
        AppManager.getInstance(packageName, appManager -> {
            // set view
            setContentView(R.layout.activity_description);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

            // init views
            initViews();

            Glide.with(this).load(appManager.getIconReference()).into(mImgIcon);
            mTxtName.setText(appManager.getName());
            mTxtDeveloper.setText(appManager.getDeveloperName());
            mTxtStar.setText(getString(R.string.score_star, appManager.getScore()));
            mTxtDownloads.setText(getString(R.string.download_over, NumberFormat.getInstance().format(appManager.getDisplayDownloads())));
            mTxtAge.setText(getString(R.string.years_old_above, appManager.getRequiredAge()));
            mTxtAbout.setText(appManager.getShortDescription());

            // set price
            // if (appManager.getPrice() != 0) mBtnInstall.setText("$" + appManager.getPrice());

            // set on click listener
            mBtnInstall.setOnClickListener(view -> {
                mBtnCancel.setOnClickListener(v -> InstallService.startActionCancelDownload(this, appManager.getPackageName()));
                mBtnCancel.setVisibility(View.VISIBLE);
                mBtnInstall.setVisibility(View.INVISIBLE);

                // install apk
                UserManager.getInstance(FirebaseAuth.getInstance().getUid(), userManager -> {
                    /* if (userManager.getMoney() - appManager.getPrice() < 0) {
                        mBtnCancel.setVisibility(View.INVISIBLE);
                        mBtnInstall.setVisibility(View.VISIBLE);
                        EarnMoneyDialog dialog = new EarnMoneyDialog();
                        dialog.show(getSupportFragmentManager(), EarnMoneyDialog.TAG);
                    } else {
                    userManager.setMoney(userManager.getMoney() - appManager.getPrice());
                    userManager.updateChanges(false); */

                    if (appManager.getApkUrl().startsWith("gs://")) InstallService.startActionNormalInstall(this, appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(this), appManager.getApkReference().getPath());
                    else InstallService.startActionNormalUrlInstall(this, appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(this), appManager.getApkUrl());
                    // }
                });
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // bind service
        Intent service = new Intent(this, InstallService.class);
        bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            InstallService.LocalBinder binder = (InstallService.LocalBinder) service;
            mService = binder.getService();
            isBound = true;

            // listen service event
            String packageName = getIntent().getStringExtra(KEY_PACKAGE_NAME);
            mService.addDownloadCancelledListener(DescriptionActivity.class.getName(), cancelledPackageName -> {
                if (cancelledPackageName.equals(packageName)) {
                    mBtnInstall.setVisibility(View.VISIBLE);
                    mBtnCancel.setVisibility(View.INVISIBLE);
                    mService.stopForeground(true);
                }
            });
            mService.addDownloadFinishedListener(DescriptionActivity.class.getName(), finishedPackageName -> {
                mBtnCancel.setOnClickListener(v -> InstallService.startActionSkipInstall(DescriptionActivity.this, packageName));
            });
            mService.addInstallCanceledListener(DescriptionActivity.class.getName(), canceledPackageName -> {
                if (canceledPackageName.equals(packageName)) {
                    mBtnCancel.setVisibility(View.INVISIBLE);
                    mBtnInstall.setVisibility(View.VISIBLE);
                    mService.stopForeground(true);
                }
            });
            mService.addPackageInstalledListener(DescriptionActivity.class.getName(), installedPackageName -> {
                if (installedPackageName.equals(packageName)) {
                    mBtnCancel.setVisibility(View.INVISIBLE);
                    mBtnInstall.setVisibility(View.VISIBLE);
                    mService.stopForeground(true);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            mService.removePackageInstalledListener(DescriptionActivity.class.getName());
            mService.removeDownloadCancelledListener(DescriptionActivity.class.getName());
            mService.removeDownloadFinishedListener(DescriptionActivity.class.getName());
        }
    };

    private void initViews() {
        mTxtName = findViewById(R.id.descriptionTxtName);
        mTxtDeveloper = findViewById(R.id.descriptionTxtDeveloper);
        mTxtExtra = findViewById(R.id.descriptionTxtExtra);
        mTxtStar = findViewById(R.id.descriptionTxtStar);
        mTxtDownloads = findViewById(R.id.descriptionTxtDownloads);
        mTxtAge = findViewById(R.id.descriptionTxtYear);
        mTxtAbout = findViewById(R.id.descriptionTxtAbout);
        mImgIcon = findViewById(R.id.descriptionImgIcon);
        mBtnInstall = findViewById(R.id.descriptionBtnInstall);
        mBtnCancel = findViewById(R.id.descriptionBtnCancel);
    }
}
