/*
 *  Copyright (C) 2022 SuperiorOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.superior.lab.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.superior.OmniJawsClient;

import com.superior.support.preferences.SecureSettingSwitchPreference;

public class LockScreenSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
        
    private static final String CATEGORY_AMBIENT = "ambient_display";
    private static final String KEY_WEATHER = "lockscreen_weather_enabled";
    private static final String LOCKSCREEN_DOUBLE_LINE_CLOCK = "lockscreen_double_line_clock_switch";

    private Preference mWeather;
    private OmniJawsClient mWeatherClient;
    private SecureSettingSwitchPreference mDoubleLineClock;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.superior_lab_lockscreen);

        ContentResolver resolver = getActivity().getContentResolver();
        Resources resources = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final PreferenceScreen prefSet = getPreferenceScreen();
        final PreferenceCategory ambientCat = (PreferenceCategory) prefScreen.findPreference(CATEGORY_AMBIENT);
        if (TextUtils.isEmpty(getResources().getString(com.android.internal.R.string.config_dozeDoubleTapSensorType)) &&
                TextUtils.isEmpty(getResources().getString(com.android.internal.R.string.config_dozeTapSensorType))) {
            prefScreen.removePreference(ambientCat);
        }

        mWeather = (Preference) findPreference(KEY_WEATHER);
        mWeatherClient = new OmniJawsClient(getContext());
        updateWeatherSettings();

        mDoubleLineClock = (SecureSettingSwitchPreference ) findPreference(LOCKSCREEN_DOUBLE_LINE_CLOCK);
        mDoubleLineClock.setChecked((Settings.Secure.getInt(getContentResolver(),
             Settings.Secure.LOCKSCREEN_USE_DOUBLE_LINE_CLOCK, 1) != 0));
        mDoubleLineClock.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mDoubleLineClock) {
        	boolean value = (Boolean) newValue;
            Settings.Secure.putInt(resolver,
                    Settings.Secure.LOCKSCREEN_USE_DOUBLE_LINE_CLOCK, value ? 1 : 0);
            return true; 
        }
        return false;
    }

    private void updateWeatherSettings() {
        if (mWeatherClient == null || mWeather == null) return;

        boolean weatherEnabled = mWeatherClient.isOmniJawsEnabled();
        mWeather.setEnabled(weatherEnabled);
        mWeather.setSummary(weatherEnabled ? R.string.lockscreen_weather_summary :
            R.string.lockscreen_weather_enabled_info);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherSettings();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SUPERIOR;
    }

}
