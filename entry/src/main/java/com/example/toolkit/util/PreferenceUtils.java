package com.example.toolkit.util;

import ohos.app.Context;
import ohos.data.DatabaseHelper;
import ohos.data.preferences.Preferences;

import java.util.Set;

public class PreferenceUtils {

    public static PreferenceUtils preferenceUtils = null;
    private static String name = "user_info";

    public static PreferenceUtils getInstance() {
        if(preferenceUtils == null) {
            preferenceUtils = new PreferenceUtils();
        }
        return preferenceUtils;
    }

    public Preferences getApplicationPref(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        Utils.log(databaseHelper.toString());
        return databaseHelper.getPreferences(name);
    }

    public void putString(Context context, String key, String value) {
        preferenceUtils.getApplicationPref(context).putString(key, value).flushSync();
    }

    public String getString(Context context, String key, String defValue) {
        return preferenceUtils.getApplicationPref(context).getString(key, defValue);
    }

    public Set<String> getList(Context context, String key){
        return preferenceUtils.getApplicationPref(context).getStringSet(key, null);
    }

    public void putList(Context context, String key, Set<String> value) {
        preferenceUtils.getApplicationPref(context).putStringSet(key, value).flushSync();
    }
}


