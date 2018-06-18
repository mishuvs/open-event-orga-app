package org.fossasia.openevent.app.core.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import org.fossasia.openevent.app.R;
import org.fossasia.openevent.app.common.Constants;
import org.fossasia.openevent.app.ui.ViewUtils;

public class ScanSettings extends PreferenceFragmentCompat {

    public static ScanSettings newInstance() {
        return new ScanSettings();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.color_top_surface));
        return view;
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle bundle, String rootKey) {
        PreferenceManager manager = getPreferenceManager();
        manager.setSharedPreferencesName(Constants.FOSS_PREFS);

        setPreferencesFromResource(R.xml.scan_settings, rootKey);

        OnPreferenceChangeListener listener = (preference, newValue) -> {
            String key = preference.getKey();

            CheckBoxPreference checkOut = (CheckBoxPreference) findPreference("check_out");
            CheckBoxPreference validate = (CheckBoxPreference) findPreference("validate");
            CheckBoxPreference checkIn = (CheckBoxPreference) findPreference("check_in");

            switch (key) {
                case "check_in":
                    //Reset other items
                    checkOut.setChecked(false);
                    validate.setChecked(false);
                    break;
                case "check_out":
                    //Reset other items
                    checkIn.setChecked(false);
                    validate.setChecked(false);
                    break;
                case "validate":
                    //Reset other items
                    checkOut.setChecked(false);
                    checkIn.setChecked(false);
                    break;
            }

            //Force the current focused checkbox to always stay checked when pressed
            //i.e confirms value when newValue is checked (true) and discards newValue
            //when newValue is unchecked (false)
            return (Boolean) newValue;
        };

        findPreference("check_in").setOnPreferenceChangeListener(listener);
        findPreference("check_out").setOnPreferenceChangeListener(listener);
        findPreference("validate").setOnPreferenceChangeListener(listener);

    }

    @Override
    public void onResume() {
        super.onResume();
        ViewUtils.setTitle(this, getString(R.string.scan_settings));
    }

}
