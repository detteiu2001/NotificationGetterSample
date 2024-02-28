package ga.gi.choju.myapplication;

import android.os.Message;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    // メッセージのマジックナンバーをシンボリック定数化
    static final int NOTIFICATION_MESSAGE_ID = 1;

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

        // 今回はアクティビティ起動中以外はメッセージを受信するメリットがないので判定
        // Logcatやらなんやらをキレイにする
        if (NotificationGetterSample.isMainActivityRunning()) {
            Log.d(TAG, "onNotificationPosted: 通知を検知");

            Message message = Message.obtain();
            message.what = NOTIFICATION_MESSAGE_ID;
            // StatusBarNotificationをそのままMainActivityで処理する
            message.obj = sbn;
            MainActivity.handler.sendMessage(message);
        }
    }
}
