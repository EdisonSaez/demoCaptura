package cl.autentia.barcode;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by iroman on 05-01-2017.
 */

public class PreferencesUtils {

    private static final String SHARED_FILE = "Barcode.config";
    public static final String KEY_TYPE = QRActivity.Extras.In.PARAMETER_TYPE;
    public static final String KEY_PORTRAIT = QRActivity.Extras.In.PORTRAIT;

    private Context mContext;

    public PreferencesUtils(Context mContext) {
        this.mContext = mContext;
    }

    private SharedPreferences getSettings() {
        return mContext.getSharedPreferences(SHARED_FILE, Context.MODE_PRIVATE);
    }

    public String getString(String sKey) {
        return getSettings().getString(sKey, null);
    }

    public void setString(String sKey, String sValue) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putString(sKey, sValue);
        editor.commit();
    }

    public boolean getBoolean(String sKey) {
        return getSettings().getBoolean(sKey, false);
    }

    public void setBoolean(String sKey, boolean sValue) {
        SharedPreferences.Editor editor = getSettings().edit();
        editor.putBoolean(sKey, sValue);
        editor.commit();
    }
}
