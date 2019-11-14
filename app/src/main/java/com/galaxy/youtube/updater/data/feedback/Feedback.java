package com.galaxy.youtube.updater.data.feedback;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

@IgnoreExtraProperties
public class Feedback {
    public String uid;
    public String content;
    public String type;

    // device info
    public String brand;
    public String manufacturer;
    public String model;
    @PropertyName("supported abis")
    public List<String> supportedAbis;
    @PropertyName("free memory")
    public int freeMemory;
    @PropertyName("network type")
    public String networkType;
    public String language;
    @PropertyName("sdk version")
    public int sdkVersion;
    @PropertyName("screen size")
    public double screenSize;
    @PropertyName("is rooted")
    public boolean isRooted;

    // app info
    @PropertyName("version code")
    public int versionCode;
    @PropertyName("version name")
    public String versionName;

    public String log;
    @PropertyName("submit time")
    @ServerTimestamp
    public Timestamp submitTime;
}
