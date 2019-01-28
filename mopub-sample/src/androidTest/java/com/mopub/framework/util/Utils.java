// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.framework.util;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.internal.deps.guava.collect.Iterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public class Utils {

    private static int BUFFER_SIZE = 4*1024;
    /**
     * When running an instrumentation test, this method will return the currently resumed activity.
     *
     * @return the resumed activity
     */
    public static Activity getCurrentActivity() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.waitForIdleSync();
        final Activity[] activity = new Activity[1];
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Collection<Activity> activities = ActivityLifecycleMonitorRegistry
                        .getInstance().getActivitiesInStage(Stage.RESUMED);
                activity[0] = Iterables.getOnlyElement(activities);
            }
        });
        return activity[0];
    }

    public static List<String> getLogs() {
        Process logcat;
        final List<String> log = new ArrayList<>();
        try {
            logcat = Runtime.getRuntime().exec(new String[]{"logcat", "-d"});
            BufferedReader br =
                    new BufferedReader(
                            new InputStreamReader(logcat.getInputStream()),
                            BUFFER_SIZE
                    );
            String line;
            while ((line = br.readLine()) != null) {
                log.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return log;
    }
}