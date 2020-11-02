package com.ringpublishing.gdpr.internal.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.ringpublishing.gdpr.R;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

public class ButtonView extends AppCompatButton
{

    @NonNull
    private final Paint paint;

    @Nullable
    private Rect rect;


    public ButtonView(Context context)
    {
        this(context, null);
    }

    public ButtonView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ButtonView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.ring_publishing_gdpr_button_border_thickness));
    }

    public void setThemeColor(@ColorInt int themeColor)
    {
        paint.setColor(themeColor);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
    {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        rect = new Rect(0, 0, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (rect != null)
        {
            canvas.drawRect(rect, paint);
        }
    }

}
