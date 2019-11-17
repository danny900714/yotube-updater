package com.galaxy.youtube.updater.dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.galaxy.youtube.updater.R;

public class ShareDialog extends DialogFragment {

    public static final String TAG = ShareDialog.class.getName() + ".TAG";

    private ImageView mImgClose;
    private TextView mTxtCopy;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_fragment_share, null);
        mImgClose = view.findViewById(R.id.shareDialogImgClose);
        mTxtCopy = view.findViewById(R.id.shareDialogTxtCopy);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set on click listener
        mImgClose.setOnClickListener(v -> dismiss());
        mTxtCopy.setOnClickListener(v -> {
            // set url to clipboard
            ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("URL", getString(R.string.app_share_url));
            manager.setPrimaryClip(clipData);

            // show Toast
            Toast.makeText(getContext(), R.string.url_successfully_copied, Toast.LENGTH_LONG).show();
        });
    }
}
