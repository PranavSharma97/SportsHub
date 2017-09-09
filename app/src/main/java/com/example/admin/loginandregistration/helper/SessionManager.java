package com.example.admin.loginandregistration.helper;

/**
 * Created by admin on 4/14/2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;

public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_MOBILE = "mobile";

    private static final String PREF_NAME = "AndroidHiveLogin";

    private static final String KEY_IS_WAITING_FOR_SMS = "IsWaitingForSms";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_IS_SPORT_SET = "isSportSet";

    public SessionManager(Context context,String name) {
        this._context = context;
//        pref = PreferenceManager.getDefaultSharedPreferences(_context);
        pref = _context.getSharedPreferences(name, PRIVATE_MODE);
        editor = pref.edit();
        editor.putBoolean(KEY_IS_SPORT_SET, false);
    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public void setUserChoiceOfSports(boolean isSportSet)
    {
        editor.putBoolean(KEY_IS_SPORT_SET, isSportSet);

        // commit changes
        editor.commit();

        Log.d(TAG, "User sport session modified!");
    }

    public boolean isSportSet()
    {
        return pref.getBoolean(KEY_IS_SPORT_SET, true);
    }

    public void setSports(String mainSport, String secondarySport, String tertiarySport)
    {
        editor.putString("mainsport", mainSport);
        editor.putString("secondarysport", secondarySport);
        editor.putString("tertiarysport", tertiarySport);

        // commit changes
        editor.commit();

        Log.d(TAG, "User sport session modified!");
    }

    public String[] getSports()
    {
        String sports[] = new String[10];

        sports[0] = pref.getString("mainsport",null);
        sports[1] = pref.getString("secondarysport",null);
        sports[2] = pref.getString("tertiarysport",null);

        return sports;
    }

    public void storeUserSelection(int checked)
    {
        editor.putInt("userSelection", checked);

        editor.commit();
    }

    public int getUserSelection()
    {
        return pref.getInt("userSelection", -1);
    }



    public void storeUserSecondarySportsSelection(String index, Boolean state)
    {
        editor.putBoolean(index, state);

        editor.commit();
    }


    public boolean getUserSecondarySportSelection(String index)
    {
        return pref.getBoolean(index, false);
    }

    public void setIsWaitingForSms(boolean isWaiting) {
        editor.putBoolean(KEY_IS_WAITING_FOR_SMS, isWaiting);
        editor.commit();
    }

    public boolean isWaitingForSms() {
        return pref.getBoolean(KEY_IS_WAITING_FOR_SMS, false);
    }

    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.commit();
    }

    public String getMobileNumber() {
        return pref.getString(KEY_MOBILE_NUMBER, null);
    }

    public void createLogin(String name, String email, String mobile) {
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_MOBILE, mobile);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> profile = new HashMap<>();
        profile.put("name", pref.getString(KEY_NAME, null));
        profile.put("email", pref.getString(KEY_EMAIL, null));
        profile.put("mobile", pref.getString(KEY_MOBILE, null));
        return profile;
    }

}









