package com.galaxy.youtube.updater;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.galaxy.youtube.updater.activity.DescriptionActivity;
import com.galaxy.youtube.updater.apps.AppsFragment;
import com.galaxy.youtube.updater.data.app.AppManager;
import com.galaxy.youtube.updater.data.cluster.ClustersManager;
import com.galaxy.youtube.updater.data.user.UserManager;
import com.galaxy.youtube.updater.dialog.EarnMoneyDialog;
import com.galaxy.youtube.updater.home.HomeFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                AppsFragment.OnFragmentInteractionListener,
                HomeFragment.OnFragmentInteractionListener {

    private FirebaseAuth mAuth;
    private RewardedAd mRewardedAd;

    private TextView mTxtUserName, mTxtUserEmail;
    private ImageView mImgUserProfile;
    private FrameLayout mFrmLay;
    private AppsFragment mAppsFragment;
    private HomeFragment mHomeFragment = HomeFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init views
        mFrmLay = findViewById(R.id.mainFrameLay);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // show home fragment
        replaceFragment(mHomeFragment);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // init firebase
        mAuth = FirebaseAuth.getInstance();

        // get header view and its content
        View header = navigationView.getHeaderView(0);
        mTxtUserName = header.findViewById(R.id.navHeaderTxtUserName);
        mTxtUserEmail = header.findViewById(R.id.navHeaderTxtUserEmail);
        mImgUserProfile = header.findViewById(R.id.navHeaderImgUserProfile);

        // get user data and set to header
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    mTxtUserName.setText(user.getDisplayName());
                    mTxtUserEmail.setText(user.getEmail());
                    Glide.with(MainActivity.this).load(user.getPhotoUrl()).into(mImgUserProfile);
                } else {
                    //TODO: show user not login dialog and stop app
                }
            }
        });

        // create apk directory
        File internalApkDir = new File(getFilesDir().getAbsolutePath() + File.separator + "apk");
        if (!internalApkDir.exists())
            internalApkDir.mkdirs();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentTransaction frmTrans = getSupportFragmentManager().beginTransaction();

        int id = item.getItemId();
        switch (id) {
            case R.id.nav_apps:
                if (mAppsFragment == null) mAppsFragment = AppsFragment.newInstance();
                frmTrans.replace(R.id.mainFrameLay, mAppsFragment);
                break;
            case R.id.nav_home:
                if (mHomeFragment == null) mHomeFragment = HomeFragment.newInstance();
                frmTrans.replace(R.id.mainFrameLay, mHomeFragment);
                break;
            /* case R.id.nav_earn:
                EarnMoneyDialog dialog = new EarnMoneyDialog();
                dialog.show(getSupportFragmentManager(), EarnMoneyDialog.TAG);
                break; */
        }
        frmTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        frmTrans.commit();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void OnClusterClick(View view, ClustersManager manager) {

    }

    @Override
    public void OnAppInClusterClick(View view, AppManager manager) {
        String packageName = manager.getPackageName();
        Intent it = new Intent(this, DescriptionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(DescriptionActivity.KEY_PACKAGE_NAME, manager.getPackageName());
        it.putExtras(bundle);
        startActivity(it);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction frmTrans = getSupportFragmentManager().beginTransaction();
        frmTrans.replace(R.id.mainFrameLay, fragment);
        frmTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        frmTrans.commit();
    }
}
