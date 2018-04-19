package com.example.jakub.floristsinventory;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import org.w3c.dom.Text;

/**
 * This activity allows user to choose his/her own settings for the app
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class CatalogPreferenceFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference orderBy = findPreference(getResources().getString(R.string.order_by_key));
            preferenceSummaryToValue(orderBy);

            Preference quantityDecrement = findPreference(getResources().getString(R.string.sale_price_key));
            preferenceSummaryToValue(quantityDecrement);

            Preference currency = findPreference(getResources().getString(R.string.currency_key));
            preferenceSummaryToValue(currency);


        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) findPreference(preference.getKey());
                int prefIndex = listPreference.findIndexOfValue(value.toString());
                CharSequence[] labels = listPreference.getEntries();
                preference.setSummary(labels[prefIndex]);
            } else {
                if (!TextUtils.isEmpty(value.toString()) && Integer.parseInt(value.toString()) > 100) {
                    preference.setSummary("-100%");
                } else if (TextUtils.isEmpty(value.toString()) || (!TextUtils.isEmpty(value.toString()) && Integer.valueOf(value.toString()) < 0)) {
                    preference.setSummary("-0%");
                } else {
                    preference.setSummary("-" + value.toString() + "%");
                }
            }
            return true;
        }

        private void preferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String values = sharedPreferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, values);
        }
    }
}