package com.galaxy.youtube.updater.service.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.galaxy.youtube.updater.data.app.AppManager;

import java.util.ArrayList;

public class InstallTaskManager {
    private static InstallTaskManager instance;

    private Context mContext;

    private ArrayList<AppManager> taskList = new ArrayList<>();

    private InstallTaskManager(Context context){
        this.mContext = context;

        // register package added receiver
        PackageInstalledReceiver receiver = new PackageInstalledReceiver(onPackageAddedListener);
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        context.registerReceiver(receiver, filter);
    }

    public static synchronized InstallTaskManager getInstance(Context context) {
        if (instance == null)
            instance = new InstallTaskManager(context.getApplicationContext());
        return instance;
    }

    public void registerInstallTask(AppManager appManager) {
        taskList.add(appManager);
    }

    private PackageInstalledReceiver.OnPackageAddedListener onPackageAddedListener = new PackageInstalledReceiver.OnPackageAddedListener() {
        @Override
        public void onPackageAdded(String packageName) {

        }
    };

    private static class PackageInstalledReceiver extends BroadcastReceiver {

        private OnPackageAddedListener listener;

        private PackageInstalledReceiver(OnPackageAddedListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            listener.onPackageAdded(packageName);
        }

        private interface OnPackageAddedListener {
            void onPackageAdded(String packageName);
        }
    }
}
