package ga.gi.choju.myapplication;

import static ga.gi.choju.myapplication.MainActivity.packageManager;

import android.app.Notification;
import android.app.Service;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.icu.text.IDNA;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: 通知リスナーを開始");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onCreate: 通知リスナーを破棄");
        this.stopSelf();
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.d(TAG, "onNotificationPosted: 通知を検知");

        if (NotificationGetterSample.isMainActivityRunning()) {
            String packageName = sbn.getPackageName();

            Notification notification = sbn.getNotification();

            CharSequence tickerText = notification.tickerText;
            Bundle extras = notification.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            CharSequence infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
//            PackageManager packageManager = getPackageManager();
            CharSequence appName = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_UNINSTALLED_PACKAGES)));
                } else {
                    appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES));
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "onNotificationPosted: パッケージ名からの取得エラー", e);
                appName = packageName;
            }
            Log.d(TAG, "PackageName: " + packageName + ", Name: " + appName +", Title: " + title + ", Text: " + text + ", subText: " + subText + ", infoText: " + infoText + ", tickerText: " + tickerText + ", bigText: " + bigText);
        }

    }
}
