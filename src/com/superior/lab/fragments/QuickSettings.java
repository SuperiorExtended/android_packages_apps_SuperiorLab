/*
 * Copyright (C) 2022-2023 The SuperiorOS Project
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

import static android.os.UserHandle.USER_CURRENT;
import static android.os.UserHandle.USER_SYSTEM;

import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.content.om.IOverlayManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import com.superior.support.preferences.SystemSettingListPreference;
import com.superior.support.preferences.SystemSettingSeekBarPreference;

import java.util.List;
import java.util.ArrayList;

public class QuickSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String QS_PAGE_TRANSITIONS = "custom_transitions_page_tile";
    private static final String KEY_QS_PANEL_STYLE = "qs_panel_style";
    private static final String KEY_PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String KEY_PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
    private static final String KEY_PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";

    private Handler mHandler;
    private IOverlayManager mOverlayManager;
    private IOverlayManager mOverlayService;
    private SystemSettingListPreference mQsStyle;
    private ListPreference mQuickPulldown;
    private SystemSettingListPreference mPageTransitions;
    private ListPreference mTileAnimationStyle;
    private SystemSettingSeekBarPreference mTileAnimationDuration;
    private ListPreference mTileAnimationInterpolator;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.superior_lab_quicksettings);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        int qpmode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown = (ListPreference) findPreference("qs_quick_pulldown");
        mQuickPulldown.setValue(String.valueOf(qpmode));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());
        mQuickPulldown.setOnPreferenceChangeListener(this);
        mPageTransitions = (SystemSettingListPreference) findPreference(QS_PAGE_TRANSITIONS);
        mPageTransitions.setOnPreferenceChangeListener(this);
        int customTransitions = Settings.System.getIntForUser(resolver,
                Settings.System.CUSTOM_TRANSITIONS_KEY,
                0, UserHandle.USER_CURRENT);
        mPageTransitions.setValue(String.valueOf(customTransitions));
        mPageTransitions.setSummary(mPageTransitions.getEntry());
        
        mOverlayService = IOverlayManager.Stub
        .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));

        mQsStyle = (SystemSettingListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mCustomSettingsObserver.observe();
        
        mTileAnimationStyle = (ListPreference) findPreference(KEY_PREF_TILE_ANIM_STYLE);
        mTileAnimationDuration = (SystemSettingSeekBarPreference) findPreference(KEY_PREF_TILE_ANIM_DURATION);
        mTileAnimationInterpolator = (ListPreference) findPreference(KEY_PREF_TILE_ANIM_INTERPOLATOR);

        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_TILE_ANIMATION_STYLE, 0, UserHandle.USER_CURRENT);
        updateAnimTileStyle(tileAnimationStyle);

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
                    Settings.System.QS_PANEL_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(Settings.System.getUriFor(Settings.System.QS_PANEL_STYLE))) {
                updateQsStyle();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, value,
                    UserHandle.USER_CURRENT);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            mQuickPulldown.setSummary(
                    mQuickPulldown.getEntries()[index]);
            return true;
        } else if (preference.equals(mPageTransitions)) {
            int customTransitions = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(resolver,
                    Settings.System.CUSTOM_TRANSITIONS_KEY, customTransitions, UserHandle.USER_CURRENT);
            int index = mPageTransitions.findIndexOfValue((String) newValue);
            mPageTransitions.setSummary(
                    mPageTransitions.getEntries()[index]);
            return true;
              } else if (preference == mQsStyle) {
               mCustomSettingsObserver.observe();
            return true;
            } else if (preference == mTileAnimationStyle) {
             int value = Integer.parseInt((String) newValue);
             updateAnimTileStyle(value);
             return true;
          } 
        return false;
    }
    
    private void updateAnimTileStyle(int tileAnimationStyle) {
        mTileAnimationDuration.setEnabled(tileAnimationStyle != 0);
        mTileAnimationInterpolator.setEnabled(tileAnimationStyle != 0);
    }
    
    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);
    }

    private void updateQsStyle() {
        ContentResolver resolver = getActivity().getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        if (qsPanelStyle == 0) {
            setDefaultStyle(mOverlayService);
        } else if (qsPanelStyle == 1) {
            setQsStyle(mOverlayService, "com.android.system.qs.outline");
        } else if (qsPanelStyle == 2 || qsPanelStyle == 3) {
            setQsStyle(mOverlayService, "com.android.system.qs.twotoneaccent");
        } else if (qsPanelStyle == 4) {
            setQsStyle(mOverlayService, "com.android.system.qs.shaded");
        } else if (qsPanelStyle == 5) {
            setQsStyle(mOverlayService, "com.android.system.qs.cyberpunk");
        } else if (qsPanelStyle == 6) {
            setQsStyle(mOverlayService, "com.android.system.qs.neumorph");
        } else if (qsPanelStyle == 7) {
            setQsStyle(mOverlayService, "com.android.system.qs.reflected");
        } else if (qsPanelStyle == 8) {
            setQsStyle(mOverlayService, "com.android.system.qs.surround");
        } else if (qsPanelStyle == 9) {
            setQsStyle(mOverlayService, "com.android.system.qs.thin");
        }    
    }

    public static void setDefaultStyle(IOverlayManager overlayManager) {
        for (int i = 0; i < QS_STYLES.length; i++) {
            String qsStyles = QS_STYLES[i];
            try {
                overlayManager.setEnabled(qsStyles, false, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setQsStyle(IOverlayManager overlayManager, String overlayName) {
        try {
            for (int i = 0; i < QS_STYLES.length; i++) {
                String qsStyles = QS_STYLES[i];
                try {
                    overlayManager.setEnabled(qsStyles, false, USER_SYSTEM);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            overlayManager.setEnabled(overlayName, true, USER_SYSTEM);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static final String[] QS_STYLES = {
        "com.android.system.qs.outline",
        "com.android.system.qs.twotoneaccent",
        "com.android.system.qs.twotoneaccent",
        "com.android.system.qs.shaded",
        "com.android.system.qs.cyberpunk",
        "com.android.system.qs.neumorph",
        "com.android.system.qs.reflected",
        "com.android.system.qs.surround",
        "com.android.system.qs.thin"
    };

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SUPERIOR;
    }

}
