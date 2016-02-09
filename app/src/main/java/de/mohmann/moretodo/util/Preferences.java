package de.mohmann.moretodo.util;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by mohmann on 2/9/16.
 */
public class Preferences {

    /* general prefs */
    final public static String PREF_AUTOREMOVE_DONE_TODOS = "autoremove_done_todos";
    final public static String PREF_MARKDOWN_ENABLE = "markdown_enable";

    /* notification prefs */
    final public static String PREF_NOTIFICATIONS_ENABLE = "notifications_enable";
    final public static String PREF_NOTIFICATIONS_VIBRATE = "notifications_vibrate";

    /* general prefs getters and setters */
    public static void setAutoremoveDoneTodos(Context context, int value) {
        setIntegerPreference(context, PREF_AUTOREMOVE_DONE_TODOS, value);
    }

    public static int getAutoremoveDoneTodos(Context context) {
        return getIntegerPreference(context, PREF_AUTOREMOVE_DONE_TODOS, -1);
    }

    public static void setMarkdownEnabled(Context context, boolean value) {
        setBooleanPreference(context, PREF_MARKDOWN_ENABLE, value);
    }

    public static boolean isMarkdownEnabled(Context context) {
        return getBooleanPreference(context, PREF_MARKDOWN_ENABLE, false);
    }

    /* notification prefs getters and setters */
    public static void setNotificationsEnabled(Context context, boolean value) {
        setBooleanPreference(context, PREF_NOTIFICATIONS_ENABLE, value);
    }

    public static boolean isNotificationsEnabled(Context context) {
        return getBooleanPreference(context, PREF_NOTIFICATIONS_ENABLE, true);
    }

    public static void setVibrateEnabled(Context context, boolean value) {
        setBooleanPreference(context, PREF_NOTIFICATIONS_VIBRATE, value);
    }

    public static boolean isVibrateEnabled(Context context) {
        return getBooleanPreference(context, PREF_NOTIFICATIONS_VIBRATE, true);
    }

    /* generic getters and setters */
    public static void setBooleanPreference(Context context, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void setStringPreference(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
    }

    public static String getStringPreference(Context context, String key, String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defaultValue);
    }

    private static int getIntegerPreference(Context context, String key, int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    private static void setIntegerPreference(Context context, String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply();
    }

    private static boolean setIntegerPrefrenceBlocking(Context context, String key, int value) {
        return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).commit();
    }

    private static long getLongPreference(Context context, String key, long defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defaultValue);
    }

    private static void setLongPreference(Context context, String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply();
    }
}
