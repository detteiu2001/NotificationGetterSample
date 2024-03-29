package ga.gi.choju.myapplication;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificationGetterSample extends Application {
    private static boolean isMainActivityRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity instanceof MainActivity) {
                    // MainActivityが作られたときだけフラグを立てる
                    isMainActivityRunning = true;
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {}

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (activity instanceof MainActivity) {
                    // MainActivityが完全に終了されたタイミングでフラグを寝かせる
                    isMainActivityRunning = false;
                }
            }
        });
    }

    public static boolean isMainActivityRunning() {
        return isMainActivityRunning;
    }
}
