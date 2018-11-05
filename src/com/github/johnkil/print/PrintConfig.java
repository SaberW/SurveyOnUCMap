package com.github.johnkil.print;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class PrintConfig {

    private static PrintConfig sInstance;

    public static void initDefault(AssetManager assets, String defaultFontPath) {
        Typeface defaultFont = TypefaceManager.load(assets, defaultFontPath);
        initDefault(defaultFont);
    }

    public static void initDefault(Typeface defaultFont) {
        sInstance = new PrintConfig(defaultFont);
    }

    static PrintConfig get() {
        if (sInstance == null)
            sInstance = new PrintConfig();
        return sInstance;
    }

    private final Typeface mFont;
    private final boolean mIsFontSet;

    private PrintConfig() {
        this(null);
    }

    private PrintConfig(Typeface defaultFont) {
        mFont = defaultFont;
        mIsFontSet = defaultFont != null;
    }

    Typeface getFont() {
        return mFont;
    }

    boolean isFontSet() {
        return mIsFontSet;
    }
}