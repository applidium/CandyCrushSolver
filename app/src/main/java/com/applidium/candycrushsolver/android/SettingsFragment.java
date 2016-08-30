package com.applidium.candycrushsolver.android;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.applidium.candycrushsolver.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final long TIME_LIMIT = 2000;
    private static final int REQUEST_CODE_FOR_SCREENSHOT = 100;
    private static final String KEY_STRING_CHOICE = "KEY_STRING_CHOICE";
    private static String STORE_DIRECTORY;
    private PermissionChecker permissionChecker;
    private Preference headService;
    private Preference buttonScreenshot;
    private Preference buttonAccessibility;
    private Preference goButton;
    private ListPreference choiceButton;
    private boolean currentlyRecording = false;
    private boolean serviceAlreadyStarted = false;
    private static Screenshot screenshot;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        STORE_DIRECTORY = getActivity().getFilesDir().getAbsolutePath();
        Timber.v("Store directory : %s", STORE_DIRECTORY);

        addPreferencesFromResource(R.xml.settings);

        initializeHeadServiceButton();
        initializeAccessibilityButton();
        initializeCandyCrushButton();
        initializeTutorialButton();
        initializeChoiceButton();

        /*** Screenshot part ***/
        screenshot = new Screenshot(STORE_DIRECTORY);
        screenshot.callForTheProjectionManager(getActivity());
        initializeScreenshotButton();
        screenshot.startCaptureHandlingThread();
    }

    private void initializeHeadServiceButton() {
        headService = findPreference(getString(R.string.serviceEnabledKey));
        permissionChecker = new PermissionChecker(getActivity());
        headService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //is Marshmallow or higher
                    Intent intent = permissionChecker.createRequiredPermissionIntent();
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.api_under_23), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }

    private void initializeAccessibilityButton() {
        buttonAccessibility = findPreference(getString(R.string.accessibility_title));
        buttonAccessibility.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                return true;
            }
        });
    }

    private void initializeScreenshotButton() {
        buttonScreenshot = findPreference(getString(R.string.screenshot));
        buttonScreenshot.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!currentlyRecording) {
                    startProjection();
                    currentlyRecording = true;
                } else {
                    stopProjection();
                    currentlyRecording = false;
                    changeIconAccordingToCurrentSettings();
                }
                return true;
            }
        });
    }

    private void initializeCandyCrushButton() {
        goButton = findPreference(getString(R.string.go));
        goButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String candyCrushPackageName = "com.king.candycrushsaga";
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(candyCrushPackageName, "com.king.candycrushsaga.CandyCrushSagaActivity"));

                if (isCandyCrushInstalled(intent)) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.candy_not_installed), Toast.LENGTH_LONG).show();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + candyCrushPackageName)));
                    } catch (android.content.ActivityNotFoundException noPlayStoreOnPhone) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + candyCrushPackageName)));
                    }
                }
                return true;
            }
        });
    }

    private boolean isCandyCrushInstalled(Intent intent) {
        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> info = manager.queryIntentActivities(intent, 0);
        return info.size() > 0;
    }

    private void initializeTutorialButton() {
        Preference tutorialButton = findPreference(getString(R.string.tuto));
        tutorialButton.setIcon(R.drawable.question);
        tutorialButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), TutoActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    private void initializeChoiceButton() {
        choiceButton = (ListPreference) findPreference(getString(R.string.choice_title));
        changeButtonAppearanceAccordingToChoice(getResources().getString(R.string.choice_best));
    }

    private void changeChoiceButton() {
        SharedPreferences settings =  getPreferenceManager().getSharedPreferences();
        String choice = choiceButton.getEntry().toString();
        Timber.v(choice);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(getString(R.string.choice_best), choice);
        editor.apply();

        changeButtonAppearanceAccordingToChoice(choice);
    }

    private void changeButtonAppearanceAccordingToChoice(String choice) {
        if (choice.equals(getResources().getString(R.string.choice_every))) {
            choiceButton.setIcon(R.drawable.infinite);
        } else {
            choiceButton.setIcon(R.drawable.star);
        }
    }

    public void startProjection() {
        Timber.v("Start projection");
        startActivityForResult(screenshot.createCaptureIntent(), REQUEST_CODE_FOR_SCREENSHOT);
    }

    public void stopProjection() {
        Timber.v("Stop projection");
       screenshot.stopHandler();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.v("On activity result");
        switch (requestCode) {
            case REQUEST_CODE_FOR_SCREENSHOT:
                Timber.v("Right request code");
                screenshot.afterActivityResult(getActivity(), resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        serviceAlreadyStarted = false;
        if (isRecentFilePresent()) {
            currentlyRecording = true;
        }
        changeIconAccordingToCurrentSettings();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        changeChoiceButton();
        changeIconAccordingToCurrentSettings();
    }

    private void changeIconAccordingToCurrentSettings() {
        setIconColorStepOne();
        setIconColorStepTwo();
        setIconColorStepThree();

        if (threeStepsOK()) {
            setServiceOnState();
        } else {
            setServiceOffState();
        }
    }

    private void setServiceOffState() {
        goButton.setIcon(R.drawable.ic_launcher_wb);
        goButton.setSelectable(false);
        if (serviceAlreadyStarted) {
            stopHeadService();
            serviceAlreadyStarted = false;
        }
    }

    private void setServiceOnState() {
        goButton.setIcon(R.drawable.ic_launcher);
        goButton.setSelectable(true);
        if (!serviceAlreadyStarted) {
            startHeadService();
            serviceAlreadyStarted = true;
        }
    }

    private boolean threeStepsOK() {
        return permissionChecker.isRequiredPermissionGranted() && isAccessibilityEnabled() && currentlyRecording;
    }

    private void setIconColorStepThree() {
        if (currentlyRecording) {
            buttonScreenshot.setIcon(R.drawable.three);
            buttonScreenshot.setTitle(R.string.screenshot_stop);
        } else {
            buttonScreenshot.setIcon(R.drawable.three_red);
            buttonScreenshot.setTitle(R.string.screenshot);
        }
    }

    private void setIconColorStepTwo() {
        if (isAccessibilityEnabled()) {
            buttonAccessibility.setIcon(R.drawable.two);
        } else {
            buttonAccessibility.setIcon(R.drawable.two_red);
        }
    }

    private void setIconColorStepOne() {
        if (permissionChecker.isRequiredPermissionGranted()) {
            headService.setIcon(R.drawable.one);
        } else {
            headService.setIcon(R.drawable.one_red);
        }
    }

    public boolean isAccessibilityEnabled() {
        final String accessibilityServiceName = getActivity().getPackageName() + "/com.applidium.candycrushsolver.android.HeadService";
        int accessibilityEnabled = getGeneralAccessibilityState();
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(accessibilityServiceName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int getGeneralAccessibilityState() {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(getActivity().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Timber.d("Error finding setting, default accessibility to not found: %s", e.getMessage());
        }
        return accessibilityEnabled;
    }

    private boolean isRecentFilePresent() {
        File file = new File(STORE_DIRECTORY + "/myscreen.png");
        Date lastModDate = new Date(file.lastModified());
        if (file.exists()) {
            Date d = new Date();
            return  Math.abs(lastModDate.getTime() - d.getTime()) < TIME_LIMIT;
        }
        return false;
    }

    private void deleteOldScreenshots() {
        try {
            STORE_DIRECTORY = getActivity().getFilesDir().getAbsolutePath();
            if (STORE_DIRECTORY == null) {
                return;
            }
            FileUtils.deleteDirectory(new File(STORE_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startHeadService() {
        String choice = choiceButton.getEntry().toString();
        Boolean choseBestMove = (choice.equals(getResources().getString(R.string.choice_best)));

        Context context = getActivity();
        Intent intent = new Intent(context, HeadService.class);

        Bundle extras = new Bundle();
        extras.putBoolean(KEY_STRING_CHOICE, choseBestMove);
        intent.putExtras(extras);

        context.startService(intent);
    }

    private void stopHeadService() {
        deleteOldScreenshots();
        AccessibilityManager manager = (AccessibilityManager) getActivity().getSystemService(Context.ACCESSIBILITY_SERVICE);
        manager.interrupt();
    }


}
