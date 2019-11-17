package com.galaxy.youtube.updater.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.galaxy.youtube.updater.R;
import com.galaxy.youtube.updater.data.feedback.FeedbackManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

// TODO: improve by adding support to log and screenshot
public class FeedbackActivity extends AppCompatActivity {

    private AppBarLayout mAppBarLay;
    private FloatingActionButton mFabSend;
    private TextInputLayout mEdtLayFeedback;
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
        mEdtLayFeedback = findViewById(R.id.feedbackEdtLayFeedback);
        mEdtFeedback = findViewById(R.id.feedbackEdtFeedback);
        mRdgType = findViewById(R.id.feedbackRdgType);
        mChkLog = findViewById(R.id.feedbackChkInfo);
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
        mFabSend.setOnClickListener(v -> sendFeedback());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        hideOption(R.id.feedback_send);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feedback_send:
                sendFeedback();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendFeedback() {
        // get data
        String type;
        if (mRdgType.getCheckedRadioButtonId() == R.id.feedbackRdbBug) type = "bug";
        else type = "suggestion";
        String content = Objects.requireNonNull(mEdtFeedback.getText()).toString();
        if (content.equals("")) {
            mEdtLayFeedback.setError(getText(R.string.feedback_edit_text_error));
            mEdtLayFeedback.setErrorIconDrawable(null);
            return;
        }
        boolean isDeviceInfoEnabled = mChkLog.isChecked();

        // send feedback
        FeedbackManager feedbackManager = new FeedbackManager.Builder(FeedbackActivity.this, type, content)
                .setDeviceInfoEnabled(isDeviceInfoEnabled)
                .build();
        feedbackManager.submit();

        // send back result
        setResult(RESULT_OK);
        finish();
    }

    private void hideOption(int resId) {
        MenuItem item = mMenu.findItem(resId);
        item.setVisible(false);
    }

    private void showOption(int resId) {
        MenuItem item = mMenu.findItem(resId);
        item.setVisible(true);
    }
}
