package com.manhdd.nuce.khoamanhinhnhandangkhuanmat.chung;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.manhdd.nuce.khoamanhinhnhandangkhuanmat.R;

/**
 * Created by Simi on 9/14/2017.
 */

public class CustomButton extends AppCompatButton {
    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void changeBackgroundColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            LayerDrawable bgDrawable = (LayerDrawable) getBackground();
            final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.ripple_2);
            if(shape != null) {
                shape.setColor(color);
            }
        } else {
            GradientDrawable gradientDrawable = (GradientDrawable) getBackground().mutate();
            if(gradientDrawable != null) {
                gradientDrawable.setColor(color);
            }
        }
    }
}
