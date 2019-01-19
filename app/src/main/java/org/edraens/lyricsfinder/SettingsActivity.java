package org.edraens.lyricsfinder;

import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPrefFragment()).commit();
    }
    public static class MyPrefFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle SavedInstanceState){
            super.onCreate(SavedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
