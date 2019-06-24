package com.galaxy.youtube.updater.data.app;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class AppInfo {
    public DocumentReference name;
    public String icon;
    @PropertyName("developer name")
    public String developerName;
    public DocumentReference score;
    @PropertyName("version code")
    public long versionCode;
    @PropertyName("version name")
    public String versionName;
    public String apk;
    @PropertyName("new feature")
    public DocumentReference newFeature;
}
