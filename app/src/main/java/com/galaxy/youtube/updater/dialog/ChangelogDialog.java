package com.galaxy.youtube.updater.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.galaxy.youtube.updater.R;

public class ChangelogDialog extends DialogFragment {

    public static final String TAG = ChangelogDialog.class.getName() + ".TAG";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.changelog)
                .setMessage(R.string.changelog_dialog_message)
                .setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}
