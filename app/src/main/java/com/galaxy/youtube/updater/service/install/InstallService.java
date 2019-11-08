package com.galaxy.youtube.updater.service.install;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.galaxy.util.receiver.PackageInstalledReceiver;
import com.galaxy.youtube.updater.MainActivity;
import com.galaxy.youtube.updater.NotificationChanelConstants;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.activity.DescriptionActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class InstallService extends Service {

    // broadcast related actions and extra
    /* public static final String ACTION_PACKAGE_INSTALLED = InstallService.class.getName() + ".PACKAGE_INSTALLED";
    public static final String ACTION_DOWNLOAD_CANCELLED = InstallService.class.getName() + ".DOWNLOAD_CANCELLED";
    public static final String EXTRA_BROADCAST_PACKAGE_NAME  = InstallService.class.getName() + ".BROADCAST_PACKAGE_NAME"; */

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_NORMAL_INSTALL = "com.galaxy.youtube.updater.service.install.action.NORMAL_INSTALL";
    public static final String ACTION_SILENT_INSTALL = "com.galaxy.youtube.updater.service.install.action.SILENT_INSTALL";
    public static final String ACTION_SKIP_INSTALL = "com.galaxy.youtube.updater.service.install.action.SKIP_INSTALL";
    public static final String ACTION_CANCEL_DOWNLOAD = "com.galaxy.youtube.updater.service.install.action.CANCEL_DOWNLOAD";

    public static final String EXTRA_PACKAGE_NAME = "com.galaxy.youtube.updater.service.install.extra.PACKAGE_NAME";
    public static final String EXTRA_APP_NAME = "com.galaxy.youtube.updater.service.install.extra.APP_NAME";
    public static final String EXTRA_APK_FILE_PATH = "com.galaxy.youtube.updater.service.install.extra.APK_FILE_PATH";
    public static final String EXTRA_APK_STORAGE_PATH = "com.galaxy.youtube.updater.service.install.extra.APK_STORAGE_PATH";
    public static final String EXTRA_APK_URL = "com.galaxy.youtube.updater.service.install.extra.APK_URL";

    private final IBinder mBinder = new LocalBinder();
    private Map<String, Consumer<String>> packageInstalledListeners = new HashMap<>();
    private Map<String, Consumer<String>> downloadCancelledListeners = new HashMap<>();
    private Map<String, Consumer<String>> downloadFinishedListeners = new HashMap<>();
    private Map<String, Consumer<String>> installCanceledListeners = new HashMap<>();

    private PackageInstalledReceiver receiver;
    private NotificationManager notificationManager;

    private StorageReference mStorage;
    private FileDownloadTask downloadTask;
    private DownloadApkAsyncTask downloadApkAsyncTask;

    private Queue<Intent> taskQueue = new ArrayDeque<>();
    private boolean isHandlingTask = false;
    private Intent handlingIntent;

    public InstallService() {
        super();
    }

    public static void startActionNormalInstall(Context context, String packageName, String appName, String apkFilePath, String apkStoragePath) {
        Intent intent = new Intent(context, InstallService.class);
        intent.setAction(ACTION_NORMAL_INSTALL);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(EXTRA_APP_NAME, appName);
        intent.putExtra(EXTRA_APK_FILE_PATH, apkFilePath);
        intent.putExtra(EXTRA_APK_STORAGE_PATH, apkStoragePath);
        context.startService(intent);
    }

    public static void startActionNormalUrlInstall(Context context, String packageName, String appName, String apkFilePath, String apkUrl) {
        Intent intent = new Intent(context, InstallService.class);
        intent.setAction(ACTION_NORMAL_INSTALL);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(EXTRA_APP_NAME, appName);
        intent.putExtra(EXTRA_APK_FILE_PATH, apkFilePath);
        intent.putExtra(EXTRA_APK_URL, apkUrl);
        context.startService(intent);
    }

    public static void startActionSilentInstall(Context context, String packageName, String appName, String apkFilePath, String apkStoragePath) {
        Intent intent = new Intent(context, InstallService.class);
        intent.setAction(ACTION_SILENT_INSTALL);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(EXTRA_APP_NAME, appName);
        intent.putExtra(EXTRA_APK_FILE_PATH, apkFilePath);
        intent.putExtra(EXTRA_APK_STORAGE_PATH, apkStoragePath);
        context.startService(intent);
    }

    public static void startActionSkipInstall(Context context, String packageName) {
        Intent intent = new Intent(context, InstallService.class);
        intent.setAction(ACTION_SKIP_INSTALL);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        context.startService(intent);
    }

    public static void startActionCancelDownload(Context context, String packageName) {
        Intent intent = new Intent(context, InstallService.class);
        intent.setAction(ACTION_CANCEL_DOWNLOAD);
        intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStorage = FirebaseStorage.getInstance().getReference();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // register broadcast receiver
        receiver = new PackageInstalledReceiver(onPackageInstalledListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        registerReceiver(receiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(InstallService.class.getSimpleName(), "action = " + action);
            switch (action) {
                case ACTION_NORMAL_INSTALL:
                    taskQueue.add(intent);
                    if (!isHandlingTask)
                        handleNextTask();
                    break;
                case ACTION_SILENT_INSTALL:
                    final String param1 = intent.getStringExtra(EXTRA_APK_FILE_PATH);
                    final String param2 = intent.getStringExtra(EXTRA_APK_STORAGE_PATH);
                    handleActionSilentInstall(param1, param2);
                    break;
                case ACTION_SKIP_INSTALL:
                    if (handlingIntent.getStringExtra(EXTRA_PACKAGE_NAME).equals(intent.getStringExtra(EXTRA_PACKAGE_NAME)) && isHandlingTask) {
                        notificationManager.cancel(handlingIntent.getStringExtra(EXTRA_PACKAGE_NAME).hashCode());
                        handleActionSkipInstall(new File(handlingIntent.getStringExtra(EXTRA_APK_FILE_PATH)));
                    }
                    break;
                case ACTION_CANCEL_DOWNLOAD:
                    Log.d(InstallService.class.getSimpleName(), "cancel package name = " + intent.getStringExtra(EXTRA_PACKAGE_NAME));
                    if (handlingIntent.getStringExtra(EXTRA_PACKAGE_NAME).equals(intent.getStringExtra(EXTRA_PACKAGE_NAME)) && isHandlingTask && (downloadTask == null ? downloadApkAsyncTask.getStatus() == AsyncTask.Status.RUNNING : downloadTask.isInProgress())) {
                        handleActionCancelDownload();
                    }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(InstallService.class.getSimpleName(), "onDestroy()");
        unregisterReceiver(receiver);
    }

    public void addPackageInstalledListener(String className, Consumer<String> listener) {
        packageInstalledListeners.put(className, listener);
    }

    public void addDownloadCancelledListener(String className, Consumer<String> listener) {
        downloadCancelledListeners.put(className, listener);
    }

    public void addDownloadFinishedListener(String className, Consumer<String> listener) {
        downloadFinishedListeners.put(className, listener);
    }

    public void addInstallCanceledListener(String className, Consumer<String> listener) {
        installCanceledListeners.put(className, listener);
    }

    public void removePackageInstalledListener(String className) {
        packageInstalledListeners.remove(className);
    }

    public void removeDownloadCancelledListener(String className) {
        downloadCancelledListeners.remove(className);
    }

    public void removeDownloadFinishedListener(String className) {
        downloadFinishedListeners.remove(className);
    }

    public void removeInstallCanceledListener(String className) {
        installCanceledListeners.remove(className);
    }

    private void handleNextTask() {
        if (taskQueue.size() == 0) {
            isHandlingTask = false;
            stopSelf();
            return;
        }
        isHandlingTask = true;

        handlingIntent = taskQueue.poll();
        switch (handlingIntent.getAction()) {
            case ACTION_NORMAL_INSTALL:
                final String apkFilePath = handlingIntent.getStringExtra(EXTRA_APK_FILE_PATH);
                final String packageName = handlingIntent.getStringExtra(EXTRA_PACKAGE_NAME);
                final String appName = handlingIntent.getStringExtra(EXTRA_APP_NAME);

                if (handlingIntent.getStringExtra(EXTRA_APK_STORAGE_PATH) == null) {
                    final String apkUrl = handlingIntent.getStringExtra(EXTRA_APK_URL);
                    handleActionNormalUrlInstall(apkFilePath, apkUrl, packageName, appName);
                } else {
                    final String apkStoragePath = handlingIntent.getStringExtra(EXTRA_APK_STORAGE_PATH);
                    handleActionNormalInstall(apkFilePath, apkStoragePath, packageName, appName);
                }
                break;
            case ACTION_SILENT_INSTALL:
                break;
        }
    }

    private void handleActionNormalInstall(final String apkFIlePath, String apkStoragePath, final String packageName, final String appName) {
        // TODO: improve notification
        // load ad

        // show indeterminate notification
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(appName)
                .setProgress(0, 0, true);
        /* Intent it = new Intent(this, DescriptionActivity.class);
        it.putExtra(DescriptionActivity.KEY_PACKAGE_NAME, packageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, it, 0);
        builder.setContentIntent(pendingIntent); */
        // cancel action
        Intent itCancelAction = new Intent(this, InstallService.class);
        itCancelAction.setAction(ACTION_CANCEL_DOWNLOAD);
        Log.d(InstallService.class.getSimpleName(), "normal install package name: " + packageName);
        itCancelAction.putExtra(EXTRA_PACKAGE_NAME, packageName);
        Log.d(InstallService.class.getSimpleName(), "normal install package name in intent: " + itCancelAction.getStringExtra(EXTRA_PACKAGE_NAME));
        PendingIntent pendingCancelIntent = PendingIntent.getService(this, 0, itCancelAction, 0);
        builder.addAction(R.drawable.ic_close_black_24dp, getString(android.R.string.cancel), pendingCancelIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_ID, getString(NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_NAME), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_ID);
        }
        notificationManager.notify(packageName.hashCode(), builder.build());
        startForeground(packageName.hashCode(), builder.build());

        // download apk
        downloadApk(apkFIlePath, apkStoragePath, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // fire listener
                for (Map.Entry<String, Consumer<String>> entry: downloadFinishedListeners.entrySet()) entry.getValue().accept(packageName);

                // show install notification
                showInstallNotification(packageName, appName);
                /* NotificationCompat.Builder installNotificationBuilder = new NotificationCompat.Builder(InstallService.this, NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_ID)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setColor(getColor(R.color.colorPrimary))
                        .setContentTitle(String.format(getString(R.string.installing_app), appName));
                Intent skipInstallIntent = new Intent(InstallService.this, InstallService.class)
                        .setAction(ACTION_SKIP_INSTALL)
                        .putExtra(EXTRA_PACKAGE_NAME, packageName);
                PendingIntent skipPendingIntent = PendingIntent.getService(InstallService.this, 0, skipInstallIntent, 0);
                installNotificationBuilder.addAction(R.drawable.ic_close_black_24dp, getString(android.R.string.cancel), skipPendingIntent);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_ID, getString(NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_NAME), NotificationManager.IMPORTANCE_LOW);
                    notificationManager.createNotificationChannel(channel);
                    installNotificationBuilder.setChannelId(NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_ID);
                }
                notificationManager.notify(packageName.hashCode(), installNotificationBuilder.build()); */

                // free downloadTask
                downloadTask = null;

                File apkFile = new File(apkFIlePath);
                normalInstallApk(apkFile);
            }
        }, new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                builder.setProgress((int) (taskSnapshot.getTotalByteCount() / 1000), (int) (taskSnapshot.getBytesTransferred()) / 1000, false);
                notificationManager.notify(packageName.hashCode(), builder.build());
            }
        }, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.w(InstallService.class.getSimpleName(), "download cancel");
                notificationManager.cancel(packageName.hashCode());
                for (Map.Entry<String, Consumer<String>> entry: downloadCancelledListeners.entrySet()) entry.getValue().accept(packageName);
            }
        });

    }

    private void handleActionNormalUrlInstall(final String apkFIlePath, final String apkUrl, final String packageName, final String appName) {
        Log.d(InstallService.class.getSimpleName(), "Url install start!");
        Log.d(InstallService.class.getSimpleName(), "apkFilePath: " + apkFIlePath);

        downloadApkAsyncTask = new DownloadApkAsyncTask(this)
                .setOnSuccessListener(id -> {
                    // fire listeners
                    for (Map.Entry<String, Consumer<String>> entry: downloadFinishedListeners.entrySet()) entry.getValue().accept(packageName);

                    showInstallNotification(packageName, appName);

                    File apkFile = new File(apkFIlePath);
                    normalInstallApk(apkFile);
                }).setOnCanceledListener(id -> {
                    // fire listeners
                    for (Map.Entry<String, Consumer<String>> entry: downloadCancelledListeners.entrySet()) entry.getValue().accept(packageName);
                });
        downloadApkAsyncTask.execute(new DownloadApkAsyncTask.Request(apkUrl, apkFIlePath, packageName).setTitle(appName));
        // register broadcast receiver
        /* registerReceiver(onApkDownloadCompletedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir()
                .setTitle(appName);
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request); */
    }

    private void handleActionSilentInstall(String param1, String param2) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void handleActionSkipInstall(final File apkFile) {
        for (Map.Entry<String, Consumer<String>> entry: installCanceledListeners.entrySet()) entry.getValue().accept(handlingIntent.getStringExtra(EXTRA_PACKAGE_NAME));

        new Thread(new Runnable() {
            @Override
            public void run() {
                apkFile.delete();

                isHandlingTask = false;
                handlingIntent = null;
                handleNextTask();
            }
        }).start();
    }

    private void handleActionCancelDownload() {
        if (handlingIntent.getStringExtra(EXTRA_APK_STORAGE_PATH) == null) downloadApkAsyncTask.cancel(true);
        else downloadTask.cancel();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(InstallService.class.getSimpleName(), "in cancel background thread");
                // delete apk file
                File apkFile = new File(handlingIntent.getStringExtra(EXTRA_APK_FILE_PATH));
                apkFile.delete();

                isHandlingTask = false;
                handlingIntent = null;
                handleNextTask();
            }
        }).start();
    }

    private void downloadApk(String apkFilePath, String apkStoragePath, @Nullable final OnSuccessListener<FileDownloadTask.TaskSnapshot> onSuccessListener, @Nullable OnProgressListener<FileDownloadTask.TaskSnapshot> onProgressListener, @Nullable OnCanceledListener onCanceledListener) {
        File apkFile = new File(apkFilePath);
        if (apkFile.exists()) {
            if (onSuccessListener != null)
                onSuccessListener.onSuccess(null);
            return;
        }

        StorageReference apkStorageRef = mStorage.child(apkStoragePath);
        downloadTask = apkStorageRef.getFile(apkFile);
        if (onSuccessListener != null)
            downloadTask.addOnSuccessListener(onSuccessListener);
        if (onProgressListener != null)
            downloadTask.addOnProgressListener(onProgressListener);
        if (onCanceledListener != null)
            downloadTask.addOnCanceledListener(onCanceledListener);
    }

    private void normalInstallApk(File apkFile) {
        if (apkFile == null || !apkFile.exists())
            throw new IllegalStateException("Apk file not found. Please download apk before install it.");

        Log.d(InstallService.class.getSimpleName(), "install triggered");
        Intent installIntent = new Intent(Intent.ACTION_VIEW)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK)
                .setDataAndType(FileProvider.getUriForFile(this, getPackageName(), apkFile), "application/vnd.android.package-archive");
        startActivity(installIntent);
    }

    private void showInstallNotification(String packageName, String appName) {
        NotificationCompat.Builder installNotificationBuilder = new NotificationCompat.Builder(InstallService.this, NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(String.format(getString(R.string.installing_app), appName));
        Intent skipInstallIntent = new Intent(InstallService.this, InstallService.class)
                .setAction(ACTION_SKIP_INSTALL)
                .putExtra(EXTRA_PACKAGE_NAME, packageName);
        PendingIntent skipPendingIntent = PendingIntent.getService(InstallService.this, 0, skipInstallIntent, 0);
        installNotificationBuilder.addAction(R.drawable.ic_close_black_24dp, getString(android.R.string.cancel), skipPendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_ID, getString(NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_NAME), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            installNotificationBuilder.setChannelId(NotificationChanelConstants.NOTIFICATION_CHANEL_INSTALL_ID);
        }
        notificationManager.notify(packageName.hashCode(), installNotificationBuilder.build());
    }

    private PackageInstalledReceiver.OnPackageInstalledListener onPackageInstalledListener = new PackageInstalledReceiver.OnPackageInstalledListener() {
        @Override
        public void onPackageInstalled(final String packageName) {
            Log.d(InstallService.class.getSimpleName(), "Package installed: " + packageName);
            if (packageName.equals(handlingIntent.getStringExtra(EXTRA_PACKAGE_NAME))) {
                for (Map.Entry<String, Consumer<String>> entry: packageInstalledListeners.entrySet()) entry.getValue().accept(packageName);

                new Thread(() -> {
                    notificationManager.cancel(packageName.hashCode());

                    // delete apk file
                    File apkFile = new File(handlingIntent.getStringExtra(EXTRA_APK_FILE_PATH));
                    apkFile.delete();

                    isHandlingTask = false;
                    handlingIntent = null;
                    handleNextTask();
                }).start();
            }
        }
    };

    private BroadcastReceiver onApkDownloadCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(InstallService.class.getSimpleName(), "Download complete, and intent = " + intent.toString());
        }
    };

    public class LocalBinder extends Binder {
        public InstallService getService() {
            return InstallService.this;
        }
    }
}
