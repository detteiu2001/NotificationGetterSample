package ga.gi.choju.myapplication;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    static boolean isForeground = false;

    static PackageManager packageManager;
    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        packageManager = getPackageManager();
        findViewById(R.id.openSetting).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });



        handler = new Handler(msg -> {
            if (msg.what == NotificationListener.NOTIFICATION_MESSAGE_ID) {
                if (!(msg.obj instanceof StatusBarNotification)) {
                    return false;
                }
                StatusBarNotification sbn = (StatusBarNotification) msg.obj;
                Log.i(TAG, "onCreate: 通知を受信");
                String packageName = sbn.getPackageName();

                Notification notification = sbn.getNotification();

                CharSequence tickerText = notification.tickerText;
                Bundle extras = notification.extras;
                String title = extras.getString(Notification.EXTRA_TITLE);
                CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
                CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
                CharSequence infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
                CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
                Icon icon = notification.getSmallIcon();
                if (icon == null) icon = notification.getLargeIcon();
                CharSequence appName;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_UNINSTALLED_PACKAGES)));
                    } else {
                        appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES));
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "onNotificationPosted: パッケージ名からの取得エラー", e);
                    appName = null;
                }
                Log.d(TAG, "PackageName: " + packageName + ", Name: " + appName + ", Title: " + title + ", Text: " + text + ", subText: " + subText + ", infoText: " + infoText + ", tickerText: " + tickerText + ", bigText: " + bigText);

                ConstraintLayout cl = findViewById(R.id.constraintLayout);
                CustomNotificationView customNotificationView = new CustomNotificationView(this);
                if (icon != null) {
                    customNotificationView.setIcon(icon);
                } else {
                    customNotificationView.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_launcher_foreground));
                }
                if (appName == null && title == null) {
                    customNotificationView.setTitleText(packageName);
                } else if (appName == null) {
                    customNotificationView.setTitleText(title);
                } else if (title == null) {
                    customNotificationView.setTitleText(appName);
                } else {
                    customNotificationView.setTitleText(appName, title);
                }

                String body = chooseText(String.valueOf(text), String.valueOf(tickerText));
                if (body == null) return false;

                customNotificationView.setBodyText(body);

                cl.addView(customNotificationView);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) customNotificationView.getLayoutParams();
//                    params.topMargin = -200;
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                customNotificationView.setLayoutParams(params);

                Animation anim = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, -50,
                        Animation.RELATIVE_TO_SELF, 0);
                anim.setFillAfter(true);
                anim.setDuration(500);
                customNotificationView.startAnimation(anim);

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Animation clearAnim = new TranslateAnimation(
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, -50);
                        clearAnim.setFillAfter(true);
                        clearAnim.setDuration(500);
                        clearAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                cl.removeView(customNotificationView);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        customNotificationView.startAnimation(clearAnim);
                    }
                }, 10000);
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isNotificationListenerServiceEnabled(this)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("通知の許可がありません")
                    .setMessage("通知の設定を開きますか?")
                    .setPositiveButton("開く", (dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);

                        startActivity(intent);
                    })
                    .setNegativeButton("なにもしない", null)
                    .show();
        }
        isForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isForeground = false;
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        ComponentName cn = new ComponentName(context, NotificationListener.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    String chooseText(String text1, String text2){
        if (text1 == null) return text2;
        if (text2 == null) return text1;
        String str1, str2;

        if(text1.length() >= text2.length()){
            str1 = text1;
            str2 = text2;
        } else {
            str1 = text2;
            str2 = text1;
        }

        Log.i(TAG, "str1: " + str1 + ", str2 : " + str2);

        if (str1.contains(str2)) {
            Log.i(TAG, "chooseText: 同じ文を含有");
            return str1;
        }

        double similarity = calcDistance(str1, str2);

        Log.i(TAG, "similarity: " + similarity);

        if(similarity > 0.5){
            Log.i(TAG, "chooseText: 類似性高");
            return str1;
        } else {
            Log.i(TAG, "chooseText: 類似性低 / 結合");
            return str2 + "\n" + str1;
        }
    }

    double calcDistance(String text1, String text2){
        // ジャロ・ウィンクラー距離のパラメータ
        final int prefixLength = 4;

        // ジャロ距離を計算
        double jaroDistance = calculateJaroDistance(text1, text2);

        // 共通の接頭辞の長さを計算
        int commonPrefixLength = 0;
        for (int i = 0; i < Math.min(prefixLength, Math.min(text1.length(), text2.length())); i++) {
            if (text1.charAt(i) == text2.charAt(i)) {
                commonPrefixLength++;
            } else {
                break;
            }
        }

        // ジャロ・ウィンクラー距離を計算
        double jaroWinklerDistance = jaroDistance + (commonPrefixLength * 0.1 * (1 - jaroDistance));

        // 閾値を超える場合は1.0にクリップ
        if (jaroWinklerDistance > 1.0) {
            jaroWinklerDistance = 1.0;
        }

        return jaroWinklerDistance;
    }

    // ジャロ距離を計算するメソッド
    private static double calculateJaroDistance(String str1, String str2) {
        int matchWindow = Math.max(0, Math.max(str1.length(), str2.length()) / 2 - 1);

        boolean[] str1Matches = new boolean[str1.length()];
        boolean[] str2Matches = new boolean[str2.length()];

        int matches = 0;
        for (int i = 0; i < str1.length(); i++) {
            int start = Math.max(0, i - matchWindow);
            int end = Math.min(i + matchWindow + 1, str2.length());

            for (int j = start; j < end; j++) {
                if (!str2Matches[j] && str1.charAt(i) == str2.charAt(j)) {
                    str1Matches[i] = true;
                    str2Matches[j] = true;
                    matches++;
                    break;
                }
            }
        }

        if (matches == 0) {
            return 0.0;
        }

        double transpositions = 0;
        int k = 0;
        for (int i = 0; i < str1.length(); i++) {
            if (str1Matches[i]) {
                while (!str2Matches[k]) {
                    k++;
                }
                if (str1.charAt(i) != str2.charAt(k)) {
                    transpositions++;
                }
                k++;
            }
        }

        return ((double) matches / str1.length()
                + (double) matches / str2.length()
                + ((double) matches - transpositions / 2) / matches) / 3.0;
    }
}