/*
 * Copyright (C) 2022 SuperiorOS
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

package com.superior.lab.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;

import java.util.Locale;
import android.text.TextUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import com.superior.lab.utils.ResourceUtils;
import com.superior.support.preferences.CustomSeekBarPreference;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
        
    private static final String KEY_STATUSBAR_TOP_PADDING = "statusbar_top_padding";
    private static final String KEY_STATUSBAR_LEFT_PADDING = "statusbar_left_padding";
    private static final String KEY_STATUSBAR_RIGHT_PADDING = "statusbar_right_padding";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.superior_lab_statusbar);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context mContext = getActivity().getApplicationContext();
        final Resources res = getResources();
        
        final int defaultLeftPadding = ResourceUtils.getIntDimensionDp(res,
                com.android.internal.R.dimen.status_bar_padding_start);
        CustomSeekBarPreference seekBar = findPreference(KEY_STATUSBAR_LEFT_PADDING);
        seekBar.setDefaultValue(defaultLeftPadding, true);

        final int defaultRightPadding = ResourceUtils.getIntDimensionDp(res,
                com.android.internal.R.dimen.status_bar_padding_end);
        seekBar = findPreference(KEY_STATUSBAR_RIGHT_PADDING);
        seekBar.setDefaultValue(defaultRightPadding, true);

        final int defaultTopPadding = ResourceUtils.getIntDimensionDp(res,
                com.android.internal.R.dimen.status_bar_padding_top);
        seekBar = findPreference(KEY_STATUSBAR_TOP_PADDING);
        seekBar.setDefaultValue(defaultTopPadding, true);

    }
    
    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        final Resources res = mContext.getResources();

        final int defaultLeftPadding = ResourceUtils.getIntDimensionDp(res,
                com.android.internal.R.dimen.status_bar_padding_start);
        final int defaultRightPadding = ResourceUtils.getIntDimensionDp(res,
                com.android.internal.R.dimen.status_bar_padding_end);
        final int defaultTopPadding = ResourceUtils.getIntDimensionDp(res,
                com.android.internal.R.dimen.status_bar_padding_top);
                
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUSBAR_LEFT_PADDING, defaultLeftPadding, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUSBAR_RIGHT_PADDING, defaultRightPadding, UserHandle.USER_CURRENT);
        Settings.System.putIntForUser(resolver,
                Settings.System.STATUSBAR_TOP_PADDING, defaultTopPadding, UserHandle.USER_CURRENT);
    }            

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SUPERIOR;
    }

}
