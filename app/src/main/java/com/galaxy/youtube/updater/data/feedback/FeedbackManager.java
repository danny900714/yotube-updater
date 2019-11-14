package com.galaxy.youtube.updater.data.feedback;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;

import com.galaxy.util.device.Device;
import com.galaxy.util.device.DeviceInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

import io.fabric.sdk.android.services.common.CommonUtils;

public class FeedbackManager {
    private static final String[] TYPES = new String[]{"bug", "suggestion"};
    private static final String COLLECTION_NAME = "feedback";

    private Feedback feedback;

    private FeedbackManager(Context context, String type, String content, boolean isDeviceInfoEnabled) {
        feedback = new Feedback();
        feedback.uid = FirebaseAuth.getInstance().getUid();
        feedback.type = type;
        feedback.content = content;

        // get version
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            feedback.versionCode = packageInfo.versionCode;
            feedback.versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (isDeviceInfoEnabled) {
            feedback.brand = Build.BRAND;
            feedback.manufacturer = Build.MANUFACTURER;
            feedback.model = Build.MODEL;
            feedback.supportedAbis = Arrays.asList(Build.SUPPORTED_ABIS);
            feedback.freeMemory = Integer.parseInt(DeviceInfo.getDeviceInfo(context, Device.DEVICE_FREE_MEMORY));
            feedback.networkType = DeviceInfo.getDeviceInfo(context, Device.DEVICE_NETWORK_TYPE);
            feedback.language = DeviceInfo.getDeviceInfo(context, Device.DEVICE_LANGUAGE);
            feedback.sdkVersion = Integer.parseInt(DeviceInfo.getDeviceInfo(context, Device.DEVICE_VERSION));
            feedback.screenSize = Double.parseDouble(DeviceInfo.getDeviceInfo(context, Device.DEVICE_IN_INCH));
            feedback.isRooted = CommonUtils.isRooted(context);
        }
    }

    // TODO: improve by adding listeners
    public void submit() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_NAME).add(feedback);
    }

    public static class Builder {
        private Context context;
        private String type, content;
        private boolean isDeviceInfoEnabled = false;

        public Builder(@NonNull Context context, @NonNull String type, @NonNull String content) {
            boolean isMatch = false;
            for (String declaredType: TYPES) {
                if (type.equals(declaredType)) isMatch = true;
            }
            if (!isMatch) throw new UnsupportedTypeException("Type " + type + " is not supported");

            this.context = context;
            this.type = type;
            this.content = content;
        }

        public Builder setDeviceInfoEnabled(boolean enabled) {
            isDeviceInfoEnabled = enabled;
            return this;
        }

        public FeedbackManager build() {
            return new FeedbackManager(context, type, content, isDeviceInfoEnabled);
        }
    }
}
