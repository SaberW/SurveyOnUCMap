package com.github.johnkil.print;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;

interface IPrint {
    void setIconTextRes(@StringRes int resId);

    void setIconCodeRes(@IntegerRes int resId);

    void setIconCode(int code);

    void setIconText(CharSequence text);

    CharSequence getIconText();

    void setIconColorRes(@ColorRes int resId);

    void setIconColor(int color);

    void setIconColor(ColorStateList colors);

    ColorStateList getIconColor();

    void setIconSizeRes(@DimenRes int resId);

    void setIconSizeDp(float size);

    void setIconSize(int unit, float size);

    int getIconSize();

    void setIconFont(String path);

    void setIconFont(Typeface font);

    Typeface getIconFont();
}