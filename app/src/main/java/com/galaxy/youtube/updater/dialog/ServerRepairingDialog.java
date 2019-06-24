package com.galaxy.youtube.updater.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.galaxy.youtube.updater.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ServerRepairingDialog extends DialogFragment {

    private ServerRepairingDialogListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener = (ServerRepairingDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ServerRepairingDialog");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_server_repairing_title)
                .setMessage(R.string.dialog_server_repairing_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(ServerRepairingDialog.this);
                    }
                });
        return builder.create();
    }

    public interface ServerRepairingDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }
}
