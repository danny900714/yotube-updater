package com.galaxy.youtube.updater.data.cluster;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Cluster {
    public List<String> apps;
    public int order;
    public Map<String, String> title;
}
