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

import com.google.android.material.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    PackageManager packageManager;
    // 通知を受け取るクラスからのメッセージを受け取るためのハンドラ
    static Handler handler;
    // 直近の通知を保存する場所
    StatusBarNotification recent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // パッケージ名からアプリ名を取得するためにパッケージマネージャーを取得
        packageManager = getPackageManager();
        // 通知の取得を許可する画面に飛ぶためのボタン
        findViewById(R.id.openSetting).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        // 直近の通知を再表示するボタン
        findViewById(R.id.recentNotification).setOnClickListener(v -> {
            // 直近の通知があるか判別
            if (recent != null) {
                displayNotification(recent);
            } else {
                // 直近の通知が保存されていない場合はSnackBarでユーザーに通告
                Snackbar.make(v, "最近の通知はありません", Snackbar.LENGTH_SHORT).show();
            }
        });

        // メッセージを受け取るためのハンドラの実装
        handler = new Handler(msg -> {
            if (msg.what == NotificationListener.NOTIFICATION_MESSAGE_ID) {
                if (!(msg.obj instanceof StatusBarNotification)) {
                    return false;
                }
                StatusBarNotification sbn = (StatusBarNotification) msg.obj;
                recent = sbn;
                Log.i(TAG, "onCreate: 通知を受信");
                displayNotification(sbn);
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 裏画面から復帰するたびに通知を取得する権限があるか確認
        if(!isNotificationListenerServiceEnabled(this)){
            // ない場合、設定への移動を促すダイアログを表示
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
    }

    // 権限があるかチェックするメソッド
    public static boolean isNotificationListenerServiceEnabled(Context context) {
        ComponentName cn = new ComponentName(context, NotificationListener.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(cn.flattenToString());
    }

    // 通知を表示する(受け取る)メソッド
    void displayNotification(StatusBarNotification sbn){
        // パッケージ名を取得
        String packageName = sbn.getPackageName();
        // Notification型の通知本体を取得
        Notification notification = sbn.getNotification();
        // tickerText(概要文)を取得
        CharSequence tickerText = notification.tickerText;
        // 通知の情報が入ったBundleを取得
        Bundle extras = notification.extras;
        // タイトルを取得
        String title = extras.getString(Notification.EXTRA_TITLE);
        // 本文を取得
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        // subTextを取得(ほぼnull)
        CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
        // infoTextを取得(ほぼnull)
        CharSequence infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
        // bigTextを取得(ほぼnull)
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        // 小さいiconを取得(大体ベクター画像なので拡大に耐えやすい)
        Icon icon = notification.getSmallIcon();
        // 小さいiconが取得できなかったときに大きいiconを取得(ラスター画像なことが結構あるので拡大すると悲惨なことになりやすい)
        if (icon == null) icon = notification.getLargeIcon();

        // アプリ名を取得
        CharSequence appName;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // TIRAMISU以降で推奨のコードが変わったらしい
                appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_UNINSTALLED_PACKAGES)));
            } else {
                appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "onNotificationPosted: パッケージ名からの取得エラー", e);
            appName = null;
        }
        // 取得データチェック
        Log.d(TAG, "PackageName: " + packageName + ", Name: " + appName + ", Title: " + title + ", Text: " + text + ", subText: " + subText + ", infoText: " + infoText + ", tickerText: " + tickerText + ", bigText: " + bigText);

        // ConstraintLayoutを取得
        ConstraintLayout cl = findViewById(R.id.constraintLayout);
        // 通知を表示するカスタムビューを作成
        CustomNotificationView customNotificationView = new CustomNotificationView(this);

        // カスタムビューに画像を登録
        if (icon != null) {
            customNotificationView.setIcon(icon);
        } else {
            // iconがnullの可能性に備えてドロイド君を滑り止めに設定
            customNotificationView.setIcon(AppCompatResources.getDrawable(this, R.drawable.ic_launcher_foreground));
        }

        // タイトルをいい感じに設定
        if (appName == null && title == null) {
            customNotificationView.setTitleText(packageName);
        } else if (appName == null) {
            customNotificationView.setTitleText(title);
        } else if (title == null) {
            customNotificationView.setTitleText(appName);
        } else {
            customNotificationView.setTitleText(appName, title);
        }

        // 本文をいい感じに設定してくれるメソッドを呼び出し
        String body = chooseText(String.valueOf(text), String.valueOf(tickerText));
        // 本文がなかったらそれはおそらく人間が確認するための通知ではないので表示しない
        if (body.equals("NoBody")) return;

        // いい感じに設定してもらった本文をビューに設定
        customNotificationView.setBodyText(body);

        // ConstraintLayoutに通知を表示するカスタムビューを登録
        cl.addView(customNotificationView);
        // 通知を表示するカスタムビューのレイアウトパラメータを作成
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) customNotificationView.getLayoutParams();
        // 通知を中央上に配置するように設定
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        // レイアウトパラメータをカスタムビューに設定
        customNotificationView.setLayoutParams(params);

        // 通知を表示するアニメーションを作成
        Animation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -50,
                Animation.RELATIVE_TO_SELF, 0);
        anim.setFillAfter(true);
        anim.setDuration(500);

        // 表示アニメーションを表示
        customNotificationView.startAnimation(anim);

        // タイマーを作成
        Timer timer = new Timer();
        // タイマーを定義
        // 10秒後にrun()内の処理が実行される
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 通知を消去するアニメーションを作成
                Animation clearAnim = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, 0,
                        Animation.RELATIVE_TO_SELF, -50);
                clearAnim.setFillAfter(true);
                clearAnim.setDuration(500);
                // 通知を消去するアニメーションを再生し終わったあとにConstraintLayoutからカスタムビューを消去したいのでアニメーションリスナーを設定
                clearAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // ConstraintLayoutからカスタムビューを消去
                        cl.removeView(customNotificationView);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                // 消去アニメーションを開始
                customNotificationView.startAnimation(clearAnim);
            }
        }, 10000);
    }

    String chooseText(String text1, String text2){
        // 通知の本文が何もない場合に"NoBody"を返す
        if ((text1 == null || text1.equals("null")) && (text2 == null || text2.equals("null"))) return "NoBody";
        // text1がnullでtext2がnullじゃない場合、そのままtext2を返す
        if (text1 == null || text1.equals("null")) return text2;
        // text2がnullでtext1がnullじゃない場合、そのままtext1を返す
        if (text2 == null || text2.equals("null")) return text1;
        String str1, str2;

        // 長さが長い方をstr1に、短い方をstr2にする
        if(text1.length() >= text2.length()){
            str1 = text1;
            str2 = text2;
        } else {
            str1 = text2;
            str2 = text1;
        }

        Log.i(TAG, "str1: " + str1 + ", str2 : " + str2);

        // 長い方に短い方がそっくりそのまま含まれていた場合、類似性とかどうせもいいので長い方を返す
        if (str1.contains(str2)) {
            Log.i(TAG, "chooseText: 同じ文を含有");
            return str1;
        }

        // 類似性を計算
        double similarity = calcDistance(str1, str2);
        Log.i(TAG, "similarity: " + similarity);

        if(similarity > 0.5){
            // 類似性が高かったら長い方だけを返す
            Log.i(TAG, "chooseText: 類似性高");
            return str1;
        } else {
            // 類似性が低かったら両方を繋げて返す
            Log.i(TAG, "chooseText: 類似性低 / 結合");
            return str2 + "\n" + str1;
        }
    }

    // いい感じに探してきた類似検索メソッド
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