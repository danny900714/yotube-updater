package com.galaxy.youtube.updater.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.galaxy.youtube.updater.R;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.button.MaterialButton;

public class AboutDialog extends DialogFragment {

    public static final String TAG = AboutDialog.class.getName() + ".TAG";

    private ImageView mImgClose;
    private TextView mTxtVersion;
    private MaterialButton mBtnLicense, mBtnChangelog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_about, null);
        mImgClose = view.findViewById(R.id.aboutDialogImgClose);
        mTxtVersion = view.findViewById(R.id.aboutDialogTxtVersion);
        mBtnChangelog = view.findViewById(R.id.aboutDialogBtnChangelog);
        mBtnLicense = view.findViewById(R.id.aboutDialogBtnLicense);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // set version
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            mTxtVersion.setText(String.format(getString(R.string.version), versionName));
        } catch (PackageManager.NameNotFoundException e) {
            mTxtVersion.setVisibility(View.GONE);
            e.printStackTrace();
        }

        // set click listener
        mImgClose.setOnClickListener(v -> dismiss());
        mBtnChangelog.setOnClickListener(v -> {
            DialogFragment dialog = new ChangelogDialog();
            dialog.show(getActivity().getSupportFragmentManager(), ChangelogDialog.TAG);
        });
        mBtnLicense.setOnClickListener(v -> startActivity(new Intent(getContext(), OssLicensesMenuActivity.class)));
        super.onActivityCreated(savedInstanceState);
    }
}
