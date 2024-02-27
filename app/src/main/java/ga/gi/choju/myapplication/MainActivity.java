package ga.gi.choju.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    static boolean isForeground = false;

    static PackageManager packageManager;

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

}