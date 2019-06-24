package com.galaxy.notification;

import android.content.Context;

import androidx.core.app.NotificationCompat;

// TODO: finish it
public class DownloadNotification {
    public static class Builder {
        private NotificationCompat.Builder builder;

        public Builder(Context context, String channelId) {
            builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.stat_sys_download);
        }

        public Builder setContentTitle(CharSequence title) {
            builder.setContentTitle(title);
            return this;
        }
    }
}
