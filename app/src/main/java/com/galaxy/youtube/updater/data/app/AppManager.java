package com.galaxy.youtube.updater.data.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

public class AppManager {
    private static FirebaseFirestore mDb;
    private static FirebaseStorage mFirebaseStorage;

    // info
    private String packageName;
    private AppInfo appInfo;
    private AppName appName;
    private AppScore appScore;
    private AppNewFeature appNewFeature;

    private File apkFile;

    // state
    private FileDownloadTask mDownloadTask;

    static {
        mDb = FirebaseFirestore.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
    }

    private AppManager(){}

    public static void getInstance(String packageName, final OnAppDataRetrieveListener listener) {
        final AppManager manager = new AppManager();
        final int[] finishedCount = {0};

        mDb.collection("apps").document(packageName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    listener.onAppDataRetrieve(null);
                    return;
                }

                manager.packageName = documentSnapshot.getId();
                manager.appInfo = documentSnapshot.toObject(AppInfo.class);

                // get App name
                manager.appInfo.name.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists())
                            manager.appName = documentSnapshot.toObject(AppName.class);

                        finishedCount[0]++;
                        if (finishedCount[0] == 3)
                            listener.onAppDataRetrieve(manager);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                // get app score
                manager.appInfo.score.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists())
                            manager.appScore = documentSnapshot.toObject(AppScore.class);

                        finishedCount[0]++;
                        if (finishedCount[0] == 3)
                            listener.onAppDataRetrieve(manager);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

                // get app new feature
                manager.appInfo.newFeature.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists())
                            manager.appNewFeature = documentSnapshot.toObject(AppNewFeature.class);

                        finishedCount[0]++;
                        if (finishedCount[0] == 3)
                            listener.onAppDataRetrieve(manager);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    public static void getUpdatableApps(final Context context, final OnAppDataListRetrieveListener listener) {
        final List<AppManager> appManagerList = new ArrayList<>();
        final List<String> installedPackagesName = getInstalledPackages(context);

        // get server supported packages
        getServerSupportedPackages(new OnStringListRetrieveListener() {
            @Override
            public void onDataRetrieve(ArrayList<String> data) {
                // check which package is occurred both in server packages and installed packages
                // choose the smaller list as the outer loop operation
                final List<String> updatablePackages = new ArrayList<>();
                if (data.size() > installedPackagesName.size()) {
                    for (String serverSupportedPackageName: data) {
                        for (String installedPackageName: installedPackagesName) {
                            if (serverSupportedPackageName.equals(installedPackageName)) {
                                updatablePackages.add(serverSupportedPackageName);
                                break;
                            }
                        }
                    }
                } else {
                    for (String installedPackageName: installedPackagesName) {
                        for (String serverSupportedPackageName: data) {
                            if (serverSupportedPackageName.equals(installedPackageName)) {
                                updatablePackages.add(serverSupportedPackageName);
                                break;
                            }
                        }
                    }
                }

                // parse app manager from updatable package
                final int[] finishedCount = {0};
                for (String packageName: updatablePackages) {
                    getInstance(packageName, new OnAppDataRetrieveListener() {
                        @Override
                        public void onAppDataRetrieve(AppManager appManager) {
                            finishedCount[0]++;
                            if (!isLatestVersion(context, appManager.packageName, appManager.appInfo.versionCode))
                                appManagerList.add(appManager);

                            if (finishedCount[0] == updatablePackages.size())
                                listener.onAppDataListRetrieve(appManagerList);
                        }
                    });
                }
            }
        });

        // get app info
        /*final int[] finishedCount = {0};
        for (String packageName: installedPackagesName) {
            mDb.collection("apps").document(packageName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        getInstance(documentSnapshot.getId(), new OnAppDataRetrieveListener() {
                            @Override
                            public void onAppDataRetrieve(AppManager appManager) {
                                appManagerList.add(appManager);

                                finishedCount[0]++;
                                if (finishedCount[0] == installedPackagesName.size())
                                    listener.onAppDataListRetrieve(appManagerList);
                            }
                        });
                    } else
                        finishedCount[0]++;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }*/
    }

    public static void addListenerToInstance(String packageName, final OnAppDataRetrieveListener listener) {
        final AppManager manager = new AppManager();

        mDb.collection("apps").document(packageName).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e!= null) {
                    Log.w(AppManager.class.getSimpleName(), e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    manager.packageName = documentSnapshot.getId();
                    manager.appInfo = documentSnapshot.toObject(AppInfo.class);

                    final boolean[] isAppInfoChanged = {true};
                    final int[] finishedCount = {0};

                    // get app name
                    manager.appInfo.name.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(AppManager.class.getSimpleName(), e);
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                                manager.appName = documentSnapshot.toObject(AppName.class);

                            if (isAppInfoChanged[0]) {
                                finishedCount[0]++;
                                if (finishedCount[0] == 2) {
                                    listener.onAppDataRetrieve(manager);
                                    isAppInfoChanged[0] = false;
                                }
                            } else
                                listener.onAppDataRetrieve(manager);
                        }
                    });

                    // get app score
                    manager.appInfo.score.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(AppManager.class.getSimpleName(), e);
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists())
                                manager.appScore = documentSnapshot.toObject(AppScore.class);

                            if (isAppInfoChanged[0]) {
                                finishedCount[0]++;
                                if (finishedCount[0] == 2) {
                                    listener.onAppDataRetrieve(manager);
                                    isAppInfoChanged[0] = false;
                                }
                            } else
                                listener.onAppDataRetrieve(manager);
                        }
                    });
                } else
                    listener.onAppDataRetrieve(null);
            }
        });
    }

    // TODO: Logic should be changed
    public static void addListenerToUpdatableApps(final Context context, final OnAppDataListRetrieveListener listener) {
        final List<AppManager> appManagerList = new ArrayList<>();
        final List<String> installedPackagesName = getInstalledPackages(context);

        // get app info
        final boolean[] isFirstLoad = {true};
        final int[] finishedCount = {0};
        for (String packageName: installedPackagesName) {
            mDb.collection("apps").document(packageName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        addListenerToInstance(documentSnapshot.getId(), new OnAppDataRetrieveListener() {
                            @Override
                            public void onAppDataRetrieve(AppManager appManager) {
                                if (appManager == null)
                                    return;

                                if (isFirstLoad[0]) {
                                    if (!isLatestVersion(context, appManager.packageName, appManager.appInfo.versionCode))
                                        appManagerList.add(appManager);

                                    finishedCount[0]++;
                                    if (finishedCount[0] == installedPackagesName.size()) {
                                        listener.onAppDataListRetrieve(appManagerList);
                                        isFirstLoad[0] = false;
                                    }
                                } else {
                                    if (!appManagerList.contains(appManager))
                                        appManagerList.add(appManager);

                                    listener.onAppDataListRetrieve(appManagerList);
                                }
                            }
                        });
                    } else
                        finishedCount[0]++;
                }
            });
        }
    }

    public String getName() {
        if (!appName.hasTranslation)
            return appName.defaultName;

        switch (Locale.getDefault().getLanguage()) {
            case "en": return appName.en;
            case "zh": return appName.zh;
            default: return null;
        }
    }

    public String getDefaultName() {
        return appName.defaultName;
    }

    // TODO: add support to multiline message
    public String getNewFeature() {
        if (!appNewFeature.hasTranslation)
            return appNewFeature.defaultName;

        switch (Locale.getDefault().getLanguage()) {
            case "en": return appNewFeature.en;
            case "zh": return appNewFeature.zh;
            default: return null;
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public StorageReference getIconReference() {
        return mFirebaseStorage.getReferenceFromUrl(appInfo.icon);
    }

    public StorageReference getApkReference() {
        return mFirebaseStorage.getReferenceFromUrl(appInfo.apk);
    }

    public void downloadApk(Context context, final OnApkDownloadFinishListener finishListener, @Nullable final OnApkDownloadProgressListener progressListener, @Nullable OnCanceledListener canceledListener) {
        final String apkName = getDefaultName() + " " + getVersionName();

        if (context != null) {
            File internalApkDir = new File(context.getFilesDir().getAbsolutePath() + File.separator + "apk");
            if (!internalApkDir.exists())
                internalApkDir.mkdirs();
            apkFile = new File(internalApkDir.getAbsolutePath() + File.separator + apkName);

            if (apkFile.exists()) {
                finishListener.onApkDownloadFinish(apkFile, apkFile.getTotalSpace());
                return;
            }

            // download apk
            mDownloadTask = getApkReference().getFile(apkFile);
            mDownloadTask.addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    if (progressListener != null)
                        progressListener.onApkDownloadProgress(taskSnapshot.getBytesTransferred(), taskSnapshot.getTotalByteCount());
                }
            }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    finishListener.onApkDownloadFinish(apkFile, taskSnapshot.getTotalByteCount());
                }
            });
            if (canceledListener != null)
                mDownloadTask.addOnCanceledListener(canceledListener);
        }
    }

    public boolean cancelDownloadApk() {
        if (mDownloadTask == null || mDownloadTask.isComplete())
            return false;

        mDownloadTask.addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                if (apkFile != null && apkFile.exists())
                    apkFile.delete();
            }
        });
        return mDownloadTask.cancel();
    }

    public void installApk(Context context) {
        if (apkFile == null || !apkFile.exists())
            throw new IllegalStateException("Apk file not found. Please download apk before install it.");

        Intent installIntent = new Intent(Intent.ACTION_VIEW)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(FileProvider.getUriForFile(context, context.getPackageName(), apkFile), "application/vnd.android.package-archive");
        context.startActivity(installIntent);
    }

    public String getVersionName() {
        return appInfo.versionName;
    }

    public double getScore() {
        double totalCount = appScore.count1 + appScore.count2 + appScore.count3 + appScore.count4 + appScore.count5;
        double average = (appScore.count1 + appScore.count2 * 2 + appScore.count3 * 3 + appScore.count4 * 4 + appScore.count5 * 5) / totalCount;
        return Math.round(average * 10) / 10.0;
    }

    public String getApkFilePath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + "apk" + File.separator + getDefaultName() + " " + getVersionName();
    }

    private static List<String> getInstalledPackages(Context context) {
        List<String> installedPackagesName = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        for (ApplicationInfo info: packages) {
            if ((info.flags & mask) == 0)
                installedPackagesName.add(info.packageName);
        }

        return installedPackagesName;
    }

    private static void getServerSupportedPackages(final OnStringListRetrieveListener listener) {
        mDb.collection("server").document("app list").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ArrayList<String> packages = (ArrayList<String>) documentSnapshot.get("packages");
                listener.onDataRetrieve(packages);
            }
        });
    }

    private static boolean isLatestVersion(Context context, String packageName, long versionCode) {
        try {
            PackageInfo pm = context.getPackageManager().getPackageInfo(packageName, 0);
            return pm.versionCode >= versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }

    public interface OnAppDataRetrieveListener {
        void onAppDataRetrieve(AppManager appManager);
    }

    public interface OnAppDataListRetrieveListener {
        void onAppDataListRetrieve(List<AppManager> appManagers);
    }

    public interface OnApkDownloadProgressListener {
        void onApkDownloadProgress(long bytesTransferred, long totalByteCount);
    }

    public interface OnApkDownloadFinishListener {
        void onApkDownloadFinish(File apkFile, long size);
    }

    private interface OnStringListRetrieveListener {
        void onDataRetrieve(ArrayList<String> data);
    }
}
