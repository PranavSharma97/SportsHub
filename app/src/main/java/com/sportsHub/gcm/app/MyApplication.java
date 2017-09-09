package com.sportsHub.gcm.app;

/**
 * Created by Admin on 11/05/16.
 */

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.admin.loginandregistration.activity.LoginActivity;
import com.example.admin.loginandregistration.helper.SQLiteHandler;
import com.example.admin.loginandregistration.helper.SessionManager;
import com.sportsHub.gcm.helper.MyPreferenceManager;


/**
 * Created by Ravi on 13/05/15.
 */

public class MyApplication extends Application {

    public static final String TAG = MyApplication.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static MyApplication mInstance;

    private MyPreferenceManager pref;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext(),"Login");
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public MyPreferenceManager getPrefManager() {
        if (pref == null) {
            pref = new MyPreferenceManager(this);
        }

        return pref;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void logout() {
        pref.clear();
        logoutUser();
        //Intent intent = new Intent(this, LoginActivity.class);
        //startActivity(intent);
    }
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(this, com.example.admin.loginandregistration.activity.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}