package com.applidium.candycrushsolver.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.applidium.candycrushsolver.BuildConfig;
import com.applidium.candycrushsolver.R;

import net.hockeyapp.android.metrics.MetricsManager;

import org.opencv.android.OpenCVLoader;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private void initializeOpenCV() {
        if (!OpenCVLoader.initDebug()) {
            Timber.i("open cv initialization failed");
            initializeOpenCV();
        } else {
            Timber.i("open cv initialization successful");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showTutorialIfFirstTime();
        initializeOpenCV();

        if (!(BuildConfig.APP_KEY).equals("")) {
            MetricsManager.register(this, getApplication(), BuildConfig.APP_KEY);
        }

        launchSettingsFragment();
    }

    private void launchSettingsFragment() {
        SettingsFragment settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    private void showTutorialIfFirstTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
            Intent intent = new Intent(this, TutoActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Timber.v("exit app");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
