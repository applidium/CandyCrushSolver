package com.applidium.candycrushsolver.monitoring.utils.crashes;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;

import com.applidium.candycrushsolver.Settings;
import com.applidium.candycrushsolver.android.MainActivity;
import com.applidium.candycrushsolver.monitoring.utils.logging.Logger;
import com.applidium.candycrushsolver.monitoring.utils.trace.Tracer;

import net.hockeyapp.android.CrashManagerListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HockeyAppCrashManagerListener extends CrashManagerListener implements
        Application.ActivityLifecycleCallbacks,
        Tracer,
        ComponentCallbacks2
{
    private final static int MAX_TRACE_LOGS = Settings.crashes.max_trace;
    private final static int MAX_COMPONENT_LOGS = Settings.crashes.max_component;

    private static final boolean ENABLED = Settings.crashes.enabled;
    private static final boolean ADDITIONAL_DATA = Settings.crashes.additional_data;

    private LinkedList<String> activityStack = new LinkedList<>();
    private WeakReference<Activity> mostRecentActivity;

    private LinkedList<String> networkLogs = new LinkedList<>();

    private LinkedList<String> traceLogs = new LinkedList<>();

    private LinkedList<String> componentLogs = new LinkedList<>();

    @Inject
    HockeyAppCrashManagerListener() {}

    @Override
    public boolean shouldAutoUploadCrashes() {
        return ENABLED;
    }

    @Override
    public String getDescription() {
        if (!ADDITIONAL_DATA) {
            return null;
        }

        String stack = getStackDescription();
        String view = getViewDescription();
        String network = getNetworkDescription();
        String trace = getTraceDescription();
        String component = getComponentDescription();

        return stack + view + network + trace + component;
    }

    private String getComponentDescription() {
        return formatLogs(componentLogs, "COMPONENT LOGS");
    }

    @NonNull
    private String formatLogs(LinkedList<String> logs, String title) {
        StringBuilder sb = new StringBuilder();

        sb.append(title);
        sb.append("\n");

        String underline = new String(new char[title.length()]).replace('\0', '=');
        sb.append(underline);
        sb.append("\n\n");

        for (String s : logs) {
            sb.append(s).append("\n");
        }

        sb.append("\n\n");
        return sb.toString();
    }

    private String getTraceDescription() {
        return formatLogs(traceLogs, "TRACE LOGS");
    }

    private String getStackDescription() {
        return formatLogs(activityStack, "ACTIVITY STACK");
    }

    private String getViewDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("VIEW DUMP\n");
        sb.append("=========\n\n");

        if (mostRecentActivity != null) {
            Activity activity = mostRecentActivity.get();
            if (activity != null) {
                View content = activity.findViewById(android.R.id.content);
                appendViewHierarchy(sb, "", content);
            } else {
                sb.append("Unable to dump view hierarchy\n");
            }
        }

        sb.append("\n\n");
        return sb.toString();
    }

    private static void appendViewHierarchy(StringBuilder sb, String prefix, View view) {
        String desc = getPrintableView(prefix, view);
        sb.append(desc).append("\n");
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            prefix = deepenPrefix(prefix);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View v = viewGroup.getChildAt(i);
                appendViewHierarchy(sb, prefix, v);
            }
        }
    }

    @NonNull
    private static String deepenPrefix(String prefix) {
        if (prefix.length() == 0) {
            prefix = "| " + prefix;
        } else {
            prefix = "  " + prefix;
        }
        return prefix;
    }

    @NonNull
    private static String getPrintableView(String prefix, View view) {
        return String.format(Locale.getDefault(),
                "%s%s (%.0f, %.0f, %d, %d) %s",
                prefix,
                view.getClass().getSimpleName(),
                view.getX(),
                view.getY(),
                view.getWidth(),
                view.getHeight(),
                getPrintableId(view));
    }

    private static String getPrintableId(View view) {
        return view.getId() != -1 ? view.getResources().getResourceName(view.getId()) : "";
    }


    private String getNetworkDescription() {
        return formatLogs(networkLogs, "NETWORK LOGS");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        String desc = getActivityDesc(activity);
        activityStack.add(desc);
    }

    private String getActivityDesc(Activity activity) {
        String desc = getObjectDescription(activity);

        if (activity instanceof MainActivity) {
            FragmentManager fragmentManager = ((MainActivity) activity).getSupportFragmentManager();
            int backstackCount = fragmentManager.getBackStackEntryCount();
            for (int i = 0; i < backstackCount; i++) {
                desc += "\n " + fragmentManager.getBackStackEntryAt(i).getName();
            }
        }

        return desc;
    }

    private static String getObjectDescription(Object object) {
        int hash = System.identityHashCode(object);
        return String.format(
                Locale.getDefault(),
                "%s@%X",
                object.getClass().getSimpleName(),
                hash
        );
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // no-op
    }

    @Override
    public void onActivityResumed(Activity activity) {
        mostRecentActivity = new WeakReference<>(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // no-op
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // no-op
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // no-op
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activityStack.pop();
    }

    @Override
    public void trace(Object target, String message, Object[] parameterValues) {
        if (traceLogs.size() == MAX_TRACE_LOGS) {
            traceLogs.removeFirst();
        }
        String trace = getTraceMessage(target, message, parameterValues);
        traceLogs.add(trace);
    }

    @NonNull
    private String getTraceMessage(Object target, String message, Object[] parameterValues) {
        StringBuilder traceBuilder = new StringBuilder();
        if (target != null) {
            traceBuilder.append(getObjectDescription(target));
            traceBuilder.append(" | ");
        }
        if (parameterValues != null && parameterValues.length > 0) {
            traceBuilder.append(String.format(message, parameterValues));
        } else {
            traceBuilder.append(message);
        }
        return traceBuilder.toString();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        String message = "onConfigurationChanged " + newConfig;
        addComponentMessage(message);
    }

    @Override
    public void onLowMemory() {
        addComponentMessage("onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        addComponentMessage("onTrimMemory " + level);
    }

    private void addComponentMessage(String message) {
        if (componentLogs.size() == MAX_COMPONENT_LOGS) {
            componentLogs.removeFirst();
        }
        componentLogs.add(message);
    }

    public static class LoggerTracer implements Tracer {

        private final Logger logger;

        @Inject
        LoggerTracer(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void trace(Object target, String message, Object[] parameterValues) {
            logger.i(target, message, parameterValues);
        }
    }

    public static class NoOpTracer implements Tracer {

        @Inject
        NoOpTracer() {}

        @Override
        public void trace(Object target, String message, Object[] parameterValues) {
        }
    }

    @Retention(RetentionPolicy.CLASS)
    @Target({ ElementType.METHOD })
    public static @interface Trace {
    }

    public static interface Tracer {
        void trace(Object target, String message, Object[] parameterValues);
    }
}
