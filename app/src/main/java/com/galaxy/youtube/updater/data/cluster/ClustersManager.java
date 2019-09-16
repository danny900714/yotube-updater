package com.galaxy.youtube.updater.data.cluster;

import com.galaxy.youtube.updater.data.app.AppManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.util.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClustersManager {
    private static FirebaseFirestore mDb;

    private List<Cluster> clusterList = new ArrayList<>();

    static {
        mDb = FirebaseFirestore.getInstance();
    }

    private ClustersManager() {}

    public static void getInstance(Consumer<ClustersManager> listener) {
        mDb.collection("clusters").orderBy("order").get().addOnSuccessListener(snapshot -> {
            ClustersManager manager = new ClustersManager();
            for (DocumentSnapshot documentSnapshot: snapshot.getDocuments()) {
                Cluster cluster = documentSnapshot.toObject(Cluster.class);
                manager.clusterList.add(cluster);
            }
            listener.accept(manager);
        });
    }

    public int getClusterCount() {
        return clusterList.size();
    }

    public String getClusterTitle(int position) {
        Map<String, String> titleMap = clusterList.get(position).title;
        return titleMap.getOrDefault(Locale.getDefault().getLanguage(), titleMap.get("en"));
    }

    public void getClusterApps(int position, Consumer<List<AppManager>> listener) {
        List<AppManager> appManagers = new ArrayList<>();
        Cluster cluster = clusterList.get(position);

        AtomicInteger count = new AtomicInteger();
        for (String packageName: cluster.apps) {
            AppManager.getInstance(packageName, manager -> {
                appManagers.add(manager);
                count.getAndIncrement();
                if (count.get() == cluster.apps.size())
                    listener.accept(appManagers);
            });
        }
    }
}
