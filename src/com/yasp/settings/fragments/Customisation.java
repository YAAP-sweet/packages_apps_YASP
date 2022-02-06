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

import static android.os.UserHandle.USER_SYSTEM;

import android.app.ActivityManagerNative;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContentResolver;
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

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Customisation extends DashboardFragment implements OnPreferenceChangeListener {

    private static final String TAG = "Customisation";

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen screen = getPreferenceScreen();

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
        return false;
    }
}
