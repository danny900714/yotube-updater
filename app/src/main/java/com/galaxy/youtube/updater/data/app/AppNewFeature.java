package com.galaxy.youtube.updater.data.app;

import com.google.firebase.firestore.PropertyName;

// TODO: add more languages support
public class AppNewFeature {
    @PropertyName("has translation")
    public boolean hasTranslation;
    @PropertyName("default")
    public String defaultName;
    public String en;
    public String zh;
}
