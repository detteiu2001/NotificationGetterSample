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

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        inflate(getContext(), R.layout.custom_notification_layout, this);

        titleText = findViewById(R.id.titleText);
        bodyText = findViewById(R.id.bodyText);
        icon = findViewById(R.id.iconImageView);

        if (attrs != null) {
            TypedArray typedArray;

            try {
                typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomNotificationView, defStyleAttr, defStyleRes);
                setTitleText(typedArray.getString(R.styleable.CustomNotificationView_titleText));
                setBodyText(typedArray.getString(R.styleable.CustomNotificationView_bodyText));
                setIcon(typedArray.getDrawable(R.styleable.CustomNotificationView_icon));
                typedArray.recycle();
            } catch (Exception e) {
                Log.e(TAG, "init: TypedArrayの取得に失敗", e);
            }
        }
    }

    public void setTitleText(CharSequence title) {
        titleText.setText(title);
    }

    public void setTitleText(CharSequence apkName, CharSequence title) {
        titleText.setText(String.format("%s - %s", apkName, title));
    }

    public void setBodyText(CharSequence body) {
        bodyText.setText(body);
    }

    @Deprecated
    public void setBodyText(CharSequence ticker, CharSequence body){
        bodyText.setText(String.format("%s\n%s", ticker, body));
    }

    public void setIcon(Icon icon){
        this.icon.setImageIcon(icon);
    }

    public void setIcon(Drawable image) {
        this.icon.setImageDrawable(image);
    }
}
