package ga.gi.choju.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

// 通知を表示するカスタムビューの制御クラス
public class CustomNotificationView extends LinearLayout {
    private static final String TAG = "CustomNotificationView";

    TextView titleText;
    TextView bodyText;
    ImageView icon;

    public CustomNotificationView(Context context) {
        this(context, null);
    }

    public CustomNotificationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomNotificationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomNotificationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleAttr);
    }

    // 初期化処理
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        // このクラスとcustom_notification_layout.xmlを紐づけ
        inflate(getContext(), R.layout.custom_notification_layout, this);

        titleText = findViewById(R.id.titleText);
        bodyText = findViewById(R.id.bodyText);
        icon = findViewById(R.id.iconImageView);

        // layoutファイルでAttributeを設定しているか判別
        if (attrs != null) {
            TypedArray typedArray;

            try {
                // Attributeを取得
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomNotificationView, defStyleAttr, defStyleRes);
                // それぞれあったら　初期化中に設定
                setTitleText(typedArray.getString(R.styleable.CustomNotificationView_titleText));
                setBodyText(typedArray.getString(R.styleable.CustomNotificationView_bodyText));
                setIcon(typedArray.getDrawable(R.styleable.CustomNotificationView_icon));
                typedArray.recycle();
            } catch (Exception e) {
                Log.e(TAG, "init: TypedArrayの取得に失敗", e);
            }
        }
    }

    // カスタムビューを操作するためのメソッド郡
    public void setTitleText(CharSequence title) {
        titleText.setText(title);
    }

    public void setTitleText(CharSequence apkName, CharSequence title) {
        titleText.setText(String.format("%s - %s", apkName, title));
    }

    public void setBodyText(CharSequence body) {
        bodyText.setText(body);
    }

    public void setIcon(Icon icon){
        this.icon.setImageIcon(icon);
    }

    public void setIcon(Drawable image) {
        this.icon.setImageDrawable(image);
    }
}
