/*
 * Copyright (C) 2018 YAAP-OS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yasp.settings.fragments;

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;
import android.app.ActivityManagerNative;
import android.app.UiModeManager;
import android.content.Context;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.os.Handler;
import android.net.Uri;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.settings.development.OverlayCategoryPreferenceController;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;
import android.util.Log;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.android.settings.dashboard.DashboardFragment;
import java.util.Locale;
import android.text.TextUtils;

import android.view.View;

import com.yasp.settings.preferences.SystemSettingListPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Customisation extends DashboardFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Customisation";
    private static final String QS_CLOCK_PICKER = "qs_clock_picker";

    private SystemSettingListPreference mQsClockPicker;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private Handler mHandler;
    private Context mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen screen = getPreferenceScreen();

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mQsClockPicker = (SystemSettingListPreference) findPreference(QS_CLOCK_PICKER);
        boolean isAospClock = Settings.System.getIntForUser(resolver,
                QS_CLOCK_PICKER, 0, UserHandle.USER_CURRENT) == 4;
        mQsClockPicker.setOnPreferenceChangeListener(this);
        mCustomSettingsObserver.observe();
    }

    private CustomSettingsObserver mCustomSettingsObserver = new CustomSettingsObserver(mHandler);
    private class CustomSettingsObserver extends ContentObserver {

        CustomSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            Context mContext = getContext();
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.QS_CLOCK_PICKER ),
                    false, this, UserHandle.USER_ALL);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_CLOCK_PICKER ))) {
                updateQsClock();
            }
        }
    }

    private void updateQsClock() {
        ContentResolver resolver = getActivity().getContentResolver();

        boolean AospClock = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_CLOCK_PICKER , 0, UserHandle.USER_CURRENT) == 4;
        boolean ColorOsClock = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_CLOCK_PICKER , 0, UserHandle.USER_CURRENT) == 5;

        if (AospClock) {
            updateQsClockPicker(mOverlayManager, "com.spark.qsclockoverlays.aosp");
        } else if (ColorOsClock) {
            updateQsClockPicker(mOverlayManager, "com.spark.qsclockoverlays.coloros");
        } else {
        	setDefaultClock(mOverlayManager);
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
    	return R.xml.customisation;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.YASP;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle(), this);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle, Fragment fragment) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.font"));
        controllers.add(new OverlayCategoryPreferenceController(context,
                "android.theme.customization.icon_pack"));
        return controllers;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
    	if (preference == mQsClockPicker) {
            int SelectedClock = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_CLOCK_PICKER, SelectedClock);
            mCustomSettingsObserver.observe();
            return true;
        }
        return false;
    }

    public static void setDefaultClock(IOverlayManager overlayManager) {
        for (int i = 0; i < CLOCKS.length; i++) {
            String clocks = CLOCKS[i];
            try {
                overlayManager.setEnabled(clocks, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateQsClockPicker(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < CLOCKS.length; i++) {
                String clocks = CLOCKS[i];
                try {
                    overlayManager.setEnabled(clocks, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void handleOverlays(String packagename, Boolean state, IOverlayManager mOverlayManager) {
        try {
            mOverlayManager.setEnabled(packagename,
                    state, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] CLOCKS = {
        "com.spark.qsclockoverlays.aosp",
        "com.spark.qsclockoverlays.coloros",
    };
}
