package com.android.launcher3.qsb;

import static com.android.launcher3.settings.SettingsActivity.SEARCH_PACKAGE;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.android.launcher3.BaseActivity;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherPrefs;
import com.android.launcher3.R;
import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.util.Themes;
import com.android.launcher3.views.ActivityContext;

public class QsbLayout extends FrameLayout implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LENS_ACTIVITY = "com.google.android.apps.lens.MainActivity";
    public static final String LENS_URI = "google://lens";

    private ImageView mAssistantIcon;
    private ImageView mGoogleIcon;
    private ImageView mLensIcon;
    private Context mContext;

    private String mSearchPackage;

    public QsbLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public QsbLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        configureIcons();
        LauncherPrefs.getPrefs(mContext).registerOnSharedPreferenceChangeListener(this);

        mSearchPackage = QsbContainerView.getSearchWidgetPackageName(mContext);

        if (mAssistantIcon == null) {
            mAssistantIcon.setScaleType(ImageView.ScaleType.CENTER);
            mAssistantIcon.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VOICE_COMMAND)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(mSearchPackage);
                mContext.startActivity(intent);
            });
        }

        if (mSearchPackage.equals(SEARCH_PACKAGE)) {
            mLensIcon.setVisibility(View.VISIBLE);
            mLensIcon.setOnClickListener(view -> {
                final Intent lensIntent = new Intent();
                final Bundle bundle = new Bundle();
                bundle.putString("caller_package", mSearchPackage);
                bundle.putLong("start_activity_time_nanos", SystemClock.elapsedRealtimeNanos());
                lensIntent.setComponent(new ComponentName(mSearchPackage, LENS_ACTIVITY))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setPackage(mSearchPackage)
                        .setData(Uri.parse(LENS_URI))
                        .putExtra("lens_activity_params", bundle);
                mContext.startActivity(lensIntent);
            });
        }

        setOnClickListener(view -> {
            final Intent searchIntent = new Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .setPackage(mSearchPackage);
            mContext.startActivity(searchIntent);
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int requestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        DeviceProfile dp = ActivityContext.lookupContext(mContext).getDeviceProfile();
        int cellWidth = DeviceProfile.calculateCellWidth(requestedWidth, dp.cellLayoutBorderSpacePx.x, dp.numShownHotseatIcons);
        int iconSize = (int)(Math.round((dp.iconSizePx * 0.92f)));
        int width = requestedWidth;
        setMeasuredDimension(width, height);

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child != null) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(Themes.KEY_THEMED_ICONS)) {
            configureIcons();
        }
    }

    private void configureIcons() {
        if (mAssistantIcon == null) {
            mAssistantIcon = findViewById(R.id.mic_icon);
        }

        if (mGoogleIcon == null) {
            mGoogleIcon = findViewById(R.id.g_icon);
        }

        if (mLensIcon == null) {
            mLensIcon = findViewById(R.id.lens_icon);
        }

        if (Themes.isThemedIconEnabled(mContext)) {
            mAssistantIcon.setImageResource(R.drawable.ic_mic_themed);
            mGoogleIcon.setImageResource(R.drawable.ic_super_g_themed);
            mLensIcon.setImageResource(R.drawable.ic_lens_themed);
        } else {
            mAssistantIcon.setImageResource(R.drawable.ic_mic_color);
            mGoogleIcon.setImageResource(R.drawable.ic_super_g_color);
            mLensIcon.setImageResource(R.drawable.ic_lens_color);
        }
    }

}
