package com.galaxy.youtube.updater.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.firebase.ui.auth.AuthUI;
import com.galaxy.youtube.updater.MainActivity;
import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.ServerStatus;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.dialog.ServerRepairingDialog;
import com.galaxy.youtube.updater.dialog.UpdateUpdaterDialog;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class LauncherActivity extends AppCompatActivity implements ServerRepairingDialog.ServerRepairingDialogListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseFirestore db;

    private static final int DELAYED_TIME = 1000;
    private static final int RC_SIGN_IN = 1;
    private static final List<AuthUI.IdpConfig> AUTH_PROVIDERS = Arrays.asList(
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.EmailBuilder().build()
    );
    private static final String SERVER_REPAIRING_DIALOG_TAG = "ServerRepairingDialog";
    private boolean isServerRepaired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_acctivity);

        // initialize firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        db = FirebaseFirestore.getInstance();

        // initialize AdMob
        MobileAds.initialize(this);

        // wait 1 second and start task
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // check if server is repaired and handle repairing UI
                db.collection("server").document("status").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                ServerStatus status = document.toObject(ServerStatus.class);
                                if (status != null)
                                    isServerRepaired = status.isRepaired;

                                if (isServerRepaired) {
                                    // TODO: set to shared preferences


                                    // show server is repairing dialog
                                    ServerRepairingDialog dialog = new ServerRepairingDialog();
                                    dialog.setCancelable(false);
                                    dialog.show(getSupportFragmentManager(), SERVER_REPAIRING_DIALOG_TAG);
                                } else {
                                    // check if user has login
                                    // if not, start sign in activity
                                    FirebaseUser user = mFirebaseAuth.getCurrentUser();
                                    if (user == null) {
                                        // start sign-in activity
                                        startActivityForResult(
                                                AuthUI.getInstance()
                                                        .createSignInIntentBuilder()
                                                        .setAvailableProviders(AUTH_PROVIDERS)
                                                        .setLogo(R.drawable.ic_launcher)
                                                        .build(),
                                                RC_SIGN_IN
                                        );
                                    } else {
                                        // check if app should update
                                        AppManager.getInstance("com.galaxy.youtube.updater", appManager -> {
                                            if (appManager.hasNewUpdate(LauncherActivity.this)) {
                                                UpdateUpdaterDialog dialog = new UpdateUpdaterDialog();
                                                Bundle args = new Bundle();
                                                args.putString(UpdateUpdaterDialog.KEY_VERSION_NAME, appManager.getVersionName());
                                                dialog.setArguments(args);
                                                dialog.show(getSupportFragmentManager(), UpdateUpdaterDialog.TAG);
                                            } else startMainActivity();
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

            }
        }, DELAYED_TIME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK)
                startMainActivity();
            else {
                //TODO: show sign in failed dialog
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof  ServerRepairingDialog) {
            finish();
        }
    }

    private void startMainActivity() {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
        finish();
    }
}
