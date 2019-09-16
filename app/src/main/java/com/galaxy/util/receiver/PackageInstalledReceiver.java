package com.galaxy.util.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.galaxy.youtube.updater.service.install.InstallService;

public class PackageInstalledReceiver extends BroadcastReceiver {

    private OnPackageInstalledListener listener;

    public PackageInstalledReceiver(OnPackageInstalledListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().getEncodedSchemeSpecificPart();
        listener.onPackageInstalled(packageName);
    }

    public interface OnPackageInstalledListener {
        void onPackageInstalled(String packageName);
    }
}
