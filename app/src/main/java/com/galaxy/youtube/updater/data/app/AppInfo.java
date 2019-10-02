package com.galaxy.youtube.updater.data.app;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.util.Map;

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
    public int downloads;
    public int age;
    @PropertyName("short description")
    public Map<String, String> shortDescription;
    @PropertyName("long description")
    public Map<String, String> longDescription;
    public int price;
}
