package com.galaxy.youtube.updater.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.service.install.InstallService;

public class UpdateUpdaterDialog extends DialogFragment {

    public static final String TAG = UpdateUpdaterDialog.class.getName() + ".TAG";
    public static final String KEY_VERSION_NAME = UpdateUpdaterDialog.class.getName() + ".VERSION_NAME";

    private String versionName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            versionName = getArguments().getString(KEY_VERSION_NAME);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.new_update_found)
                .setMessage(String.format(getString(R.string.update_updater_dialog_message), versionName))
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> {
                    AppManager.getInstance("com.galaxy.youtube.updater", appManager -> {
                        InstallService.startActionNormalInstall(context, appManager.getPackageName(), appManager.getName(), appManager.getApkFilePath(context), appManager.getApkReference().getPath());
                    });
                }))
                .setNegativeButton(android.R.string.cancel, ((dialog, which) -> {
                    getActivity().finish();
                }));
        return builder.create();
    }
}
