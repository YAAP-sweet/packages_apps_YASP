/*
 * Copyright (C) 2021 Yet Another AOSP Project
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

import com.android.internal.logging.nano.MetricsProto;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.search.SearchIndexable;

import com.yasp.settings.preferences.CustomSeekBarPreference;
import com.yasp.settings.preferences.SystemSettingListPreference;

@SearchIndexable
public class GamingMode extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String GAMING_MODE_MEDIA_KEY = "gaming_mode_media";
    private static final String GAMING_MODE_BRIGHTNESS_KEY = "gaming_mode_brightness";
    private static final String GAMING_MODE_RINGER_KEY = "gaming_mode_ringer";

    CustomSeekBarPreference mMediaVolume;
    CustomSeekBarPreference mBrightnessLevel;
    SystemSettingListPreference mRingerMode;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.gaming_mode);

        final ContentResolver resolver = getActivity().getContentResolver();

        mMediaVolume = findPreference(GAMING_MODE_MEDIA_KEY);
        int value = Settings.System.getInt(resolver, GAMING_MODE_MEDIA_KEY, 80);
        mMediaVolume.setValue(value);
        mMediaVolume.setOnPreferenceChangeListener(this);

        mBrightnessLevel = findPreference(GAMING_MODE_BRIGHTNESS_KEY);
        value = Settings.System.getInt(resolver, GAMING_MODE_BRIGHTNESS_KEY, 80);
        mBrightnessLevel.setValue(value);
        mBrightnessLevel.setOnPreferenceChangeListener(this);

        mRingerMode = findPreference(GAMING_MODE_RINGER_KEY);
        value = Settings.System.getInt(resolver, GAMING_MODE_RINGER_KEY, 0);
        mRingerMode.setValue(Integer.toString(value));
        mRingerMode.setSummary(mRingerMode.getEntry());
        mRingerMode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mMediaVolume) {
            int value = (Integer) objValue;
            Settings.System.putInt(resolver, GAMING_MODE_MEDIA_KEY, value);
            return true;
        } else if (preference == mBrightnessLevel) {
            int value = (Integer) objValue;
            Settings.System.putInt(resolver, GAMING_MODE_BRIGHTNESS_KEY, value);
            return true;
        } else if (preference == mRingerMode) {
            int value = Integer.valueOf((String) objValue);
            int index = mRingerMode.findIndexOfValue((String) objValue);
            mRingerMode.setSummary(mRingerMode.getEntries()[index]);
            Settings.System.putInt(resolver, GAMING_MODE_RINGER_KEY, value);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.YASP;
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.gaming_mode);
}
