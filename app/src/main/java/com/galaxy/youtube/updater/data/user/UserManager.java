package com.galaxy.youtube.updater.data.user;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserManager {

    private UserInfo userInfo;
    private String uid;
    private Map<String, Object> pendingChanges = new HashMap<>();

    private UserManager(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static void getInstance(String uid, Consumer<UserManager> listener) {
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mDb.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            UserManager manager = new UserManager(documentSnapshot.toObject(UserInfo.class));
            manager.uid = uid;
            listener.accept(manager);
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    public static void getListenableInstance(String uid, Consumer<UserManager> listener) {
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mDb.collection("users").document(uid).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            if (documentSnapshot != null && documentSnapshot.exists()) {
                UserManager manager = new UserManager(documentSnapshot.toObject(UserInfo.class));
                manager.uid = uid;
                listener.accept(manager);
            }
        });
    }

    public int getMoney() {
        return userInfo.money;
    }

    public void setMoney(int money) {
        if (money > 100) money = 100;
        pendingChanges.put("money", money);
    }

    public void updateChanges(boolean updateLocal) {
        FirebaseFirestore mDb = FirebaseFirestore.getInstance();
        mDb.collection("users").document(uid).update(pendingChanges).addOnSuccessListener(v -> {
            if (updateLocal) userInfo.money = (int) pendingChanges.get("money");
            pendingChanges.clear();
        });
    }
}
