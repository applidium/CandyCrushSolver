package com.applidium.candycrushsolver.android;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.applidium.candycrushsolver.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import timber.log.Timber;

public class HeadService extends AccessibilityService {

    private final static int FOREGROUND_ID = 999;
    private static int DELAY = 5000;
    private static final long TIME_LIMIT = 60000;
    private static final String KEY_STRING_CHOICE = "KEY_STRING_CHOICE";
    private static final String PENDING_INTENT = "PENDING_INTENT";
    private String STORE_DIRECTORY;
    final Handler handler = new Handler();
    private Context context;
    private Intent intent;
    private boolean choseBestMove;
    private boolean beenInCandyCrush;
    private boolean isStopped;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //your attention space rangers, this is starCommand !
        choseBestMove = intent.getExtras().getBoolean(KEY_STRING_CHOICE);
        toastServiceStarted();
        STORE_DIRECTORY = getFilesDir().getAbsolutePath();
        PendingIntent pendingIntent = createPendingIntent();
        Notification notification = createNotification(pendingIntent);
        if (!choseBestMove) {
            DELAY = 10000;
        }
        initializeBooleans();

        startForeground(FOREGROUND_ID, notification);

        return START_NOT_STICKY;
    }

    private void initializeBooleans() {
        isStopped = false;
        beenInCandyCrush = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyHeadService();
        deleteFiles();
        stopForeground(true);
        toastServiceEnded();
    }

    private void initService() {
        Timber.v("Life cycle : service initialisation");
        beenInCandyCrush = true;
        context = this;
    }

    private void destroyHeadService() {
        if (intent != null) {
            context.stopService(intent);
        }
    }

    private void deleteFiles() {
        try {
            if (STORE_DIRECTORY == null) {
                return;
            }
            FileUtils.deleteDirectory(new File(STORE_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private Notification createNotification(PendingIntent intent) {
        NotificationCompat.Builder mBuilder = buildNotification(intent);

        PendingIntent quitPendingIntent = createPendingIntentToQuit();

        NotificationCompat.Action actionOpenApp =
                new NotificationCompat.Action.Builder(R.drawable.notif_yes, getText(R.string.notificationGo), intent).build();
        NotificationCompat.Action actionQuit =
                new NotificationCompat.Action.Builder(R.drawable.notif_no, getText(R.string.notificationQuit), quitPendingIntent).build();

        mBuilder.addAction(actionOpenApp);
        mBuilder.addAction(actionQuit);

        return mBuilder.build();
    }

    private NotificationCompat.Builder buildNotification(PendingIntent intent) {
        return new NotificationCompat.Builder(this)
                    .setContentTitle(getText(R.string.notificationTitle))
                    .setContentText(getText(R.string.notificationText))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(intent);
    }

    private PendingIntent createPendingIntentToQuit() {
        Intent quitIntent = new Intent(PENDING_INTENT);
        PendingIntent quitPendingIntent = PendingIntent.getBroadcast(this, 0, quitIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PENDING_INTENT);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopBusinessService();
                deleteFiles();
                isStopped = true;
                stopForeground(true);
            }
        };
        registerReceiver(receiver, filter);
        return quitPendingIntent;
    }

    private void toastServiceStarted() {
        String message;
        if (choseBestMove) {
            message = getResources().getString(R.string.start_service_best);
        } else {
            message = getResources().getString(R.string.start_service_every);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void toastServiceEnded() {
        Toast.makeText(this, getResources().getString(R.string.stop_service), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        setServiceInfo(config);
    }

    private void startImageListener() {
        handler.postDelayed(getRunnable(), 2);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getSource() == null || isStopped) {
            return;
        }
        Timber.v("Still on");
        File file = new File(STORE_DIRECTORY + "/myscreen.png");
        Date lastModDate = new Date(file.lastModified());
        if (file.exists()) {
            launchBusinessServiceIfOnCandyCrush(event, lastModDate);
        }
    }

    private void launchBusinessServiceIfOnCandyCrush(AccessibilityEvent event, Date lastModDate) {
        Date d = new Date();
        //check if screenshot not so old : way to know if screenshots working
        if (event.getPackageName().toString().equals("com.king.candycrushsaga") && Math.abs(lastModDate.getTime() - d.getTime()) < TIME_LIMIT) {
            launchBusinessService();
        } else {
            stopBusinessService();
        }
    }

    private void launchBusinessService() {
        initService();
        Timber.v("App cycle : Candy Crush activity");
        startImageListener();
    }

    private void stopBusinessService() {
        Timber.v("App cycle : activity other than Candy Crush");
        if (beenInCandyCrush) {
            handler.removeCallbacksAndMessages(null);
            Intent businessIntent = new Intent(this, BusinessService.class);
            stopService(businessIntent);
        }
    }

    @Override
    public void onInterrupt() {
        super.onDestroy();
        destroyHeadService();
        deleteFiles();
        stopForeground(true);
        toastServiceEnded();
    }

    private Runnable getRunnable() {
        return new Runnable() {
            public void run() {
                Timber.v("App cycle : just before starting intent");
                intent = new Intent(context, BusinessService.class);

                Bundle extras = new Bundle();
                extras.putBoolean(KEY_STRING_CHOICE, choseBestMove);
                intent.putExtras(extras);

                context.startService(intent);
                Timber.v("App cycle : just after starting intent");
                handler.postDelayed(getRunnable(), DELAY);
            }
        };
    }
}
