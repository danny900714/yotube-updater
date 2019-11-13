package com.galaxy.youtube.updater.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.galaxy.youtube.updater.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Optional;

public class FeedbackActivity extends AppCompatActivity {

    private AppBarLayout mAppBarLay;
    private FloatingActionButton mFabSend;
    private TextInputEditText mEdtFeedback;
    private RadioGroup mRdgType;
    private CheckBox mChkLog, mChkSuggestion;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setCollapseIcon(R.drawable.ic_menu_send);
        setSupportActionBar(toolbar);

        // init views
        mAppBarLay = findViewById(R.id.app_bar);
        mFabSend = findViewById(R.id.fab);
        mEdtFeedback = findViewById(R.id.feedbackEdtFeedback);
        mRdgType = findViewById(R.id.feedbackRdgType);
        mChkLog = findViewById(R.id.feedbackChkLog);
        mChkSuggestion = findViewById(R.id.feedbackChkScreenshot);

        // init feedback edit text
        mEdtFeedback.requestFocus();

        // hide option menu icon when app bar is extended
        mAppBarLay.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private boolean isShown = false;
            private int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) scrollRange = mAppBarLay.getTotalScrollRange();
                if (scrollRange + verticalOffset == 0) {
                    showOption(R.id.feedback_send);
                    isShown = true;
                } else if (isShown) {
                    hideOption(R.id.feedback_send);
                    isShown = false;
                }
            }
        });

        // set send on Click listener

        mFabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        hideOption(R.id.feedback_send);
        return true;
    }

    private View.OnClickListener onSendClick = v -> {
        // get data

    };

    private void hideOption(int resId) {
        MenuItem item = mMenu.findItem(resId);
        item.setVisible(false);
    }

    private void showOption(int resId) {
        MenuItem item = mMenu.findItem(resId);
        item.setVisible(true);
    }
}
