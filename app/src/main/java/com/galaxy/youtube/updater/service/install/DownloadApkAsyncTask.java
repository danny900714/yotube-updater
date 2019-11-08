package com.galaxy.youtube.updater.service.install;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.galaxy.youtube.updater.MainActivity;
import com.galaxy.youtube.updater.NotificationChanelConstants;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.activity.DescriptionActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

import static com.galaxy.youtube.updater.service.install.InstallService.ACTION_CANCEL_DOWNLOAD;
import static com.galaxy.youtube.updater.service.install.InstallService.EXTRA_PACKAGE_NAME;

public class DownloadApkAsyncTask extends AsyncTask<DownloadApkAsyncTask.Request, Integer, Boolean> {

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private Consumer<Integer> onSuccessListener;
    private Consumer<Integer> onCanceledListener;

    private int id;

    public DownloadApkAsyncTask(Context context) {
        mContext = context;
    }

    public DownloadApkAsyncTask setOnSuccessListener(Consumer<Integer> onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
        return this;
    }

    public DownloadApkAsyncTask setOnCanceledListener(Consumer<Integer> onCanceledListener) {
        this.onCanceledListener = onCanceledListener;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected Boolean doInBackground(Request... requests) {
        for (Request request: requests) {
            // build notification
            mNotificationBuilder = new NotificationCompat.Builder(mContext, NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_ID)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentTitle(request.mNotificationTitle == null ? mContext.getString(R.string.downloads) : request.mNotificationTitle)
                    .setProgress(0, 0, true);
            /* Intent it = new Intent(mContext, DescriptionActivity.class);
            it.putExtra(DescriptionActivity.KEY_PACKAGE_NAME, request.mPackageName);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, it, 0);
            mNotificationBuilder.setContentIntent(pendingIntent); */
            // cancel action
            Intent itCancelAction = new Intent(mContext, InstallService.class);
            itCancelAction.setAction(ACTION_CANCEL_DOWNLOAD);
            Log.d(InstallService.class.getSimpleName(), "normal install package name: " + request.mPackageName);
            itCancelAction.putExtra(EXTRA_PACKAGE_NAME, request.mPackageName);
            Log.d(InstallService.class.getSimpleName(), "normal install package name in intent: " + itCancelAction.getStringExtra(EXTRA_PACKAGE_NAME));
            PendingIntent pendingCancelIntent = PendingIntent.getService(mContext, 0, itCancelAction, 0);
            mNotificationBuilder.addAction(R.drawable.ic_close_black_24dp, mContext.getString(android.R.string.cancel), pendingCancelIntent);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_ID, mContext.getString(NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_NAME), NotificationManager.IMPORTANCE_LOW);
                mNotificationManager.createNotificationChannel(channel);
                mNotificationBuilder.setChannelId(NotificationChanelConstants.NOTIFICATION_CHANEL_DOWNLOAD_ID);
            }
            id = request.mPackageName.hashCode();
            mNotificationManager.notify(request.mPackageName.hashCode(), mNotificationBuilder.build());

            // check if destination is safe
            File apkFile = new File(request.mFilePath);
            if (!apkFile.getParentFile().exists()) apkFile.getParentFile().mkdirs();

            // download file
            try {
                URL url = new URL(request.mUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                int size = connection.getContentLength();
                Log.d(DownloadApkAsyncTask.class.getSimpleName(), "Size: " + size);

                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);
                OutputStream outputStream = new FileOutputStream(apkFile);
                byte[] data = new byte[4096];

                int count, total = 0;
                while ((count = inputStream.read(data)) != -1) {
                    if (isCancelled()) {
                        outputStream.flush();
                        inputStream.close();
                        outputStream.close();
                        break;
                    }

                    total += count;
                    if (size != -1) publishProgress((int)(total * 100.0 / size));
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.d(DownloadApkAsyncTask.class.getSimpleName(), "Progress: " + values[0]);
        mNotificationBuilder.setProgress(100, values[0], false);
        mNotificationManager.notify(id, mNotificationBuilder.build());
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        mNotificationManager.cancel(id);
        mNotificationBuilder = null;
        if (onCanceledListener != null) onCanceledListener.accept(id);
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        mNotificationManager.cancel(id);
        mNotificationBuilder = null;
        if (isSuccess && onSuccessListener != null) onSuccessListener.accept(id);
    }

    public static class Request {
        private String mUrl, mFilePath, mPackageName, mNotificationTitle;

        public Request(String url, String filePath, String packageName) {
            mUrl = url;
            mFilePath = filePath;
            mPackageName = packageName;
        }

        public Request setTitle(String title) {
            mNotificationTitle = title;
            return this;
        }
    }
}
