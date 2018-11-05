package com.github.johnkil.print;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;

class TypefaceManager {
    private static final HashMap<String, Typeface> sTypefaces = new HashMap<String, Typeface>();

    static Typeface load(AssetManager assets, String path) {
        synchronized (sTypefaces) {
            Typeface typeface;
            if (sTypefaces.containsKey(path)) {
                typeface = sTypefaces.get(path);
            } else {
                typeface = Typeface.createFromAsset(assets, path);
                sTypefaces.put(path, typeface);
            }
            return typeface;
        }
    }

    private TypefaceManager() {
    }
}