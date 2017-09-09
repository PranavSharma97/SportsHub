package com.sportsHub.gcm.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.R;
import com.example.admin.loginandregistration.app.AppConfig;
import com.example.admin.loginandregistration.helper.CustomPriorityRequest;
import com.example.admin.loginandregistration.helper.SQLiteHandler;
import com.example.admin.loginandregistration.helper.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.sportsHub.gcm.adapter.ChatRoomsAdapter;
import com.sportsHub.gcm.app.EndPoints;
import com.sportsHub.gcm.app.MyApplication;
import com.sportsHub.gcm.gcm.GcmIntentService;
import com.sportsHub.gcm.gcm.NotificationUtils;
import com.sportsHub.gcm.helper.Config;
import com.sportsHub.gcm.helper.SimpleDividerItemDecoration;
import com.sportsHub.gcm.model.ChatRoom;
import com.sportsHub.gcm.model.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<ChatRoom> chatRoomArrayList;
    private ChatRoomsAdapter mAdapter;
    private RecyclerView recyclerView;
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Check for login session. If not logged in launch
         * login activity
         * */
        if (MyApplication.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /**
         * Broadcast receiver calls in two scenarios
         * 1. gcm registration is completed
         * 2. when new push notification is received
         * */
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    subscribeToGlobalTopic();

                } else if (intent.getAction().equals(Config.SENT_TOKEN_TO_SERVER)) {
                    // gcm registration id is stored in our server's MySQL
                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    handlePushNotification(intent);
                }
            }
        };

        chatRoomArrayList = new ArrayList<>();
        mAdapter = new ChatRoomsAdapter(this, chatRoomArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                // when chat is clicked, launch full chat thread activity
                ChatRoom chatRoom = chatRoomArrayList.get(position);
                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                intent.putExtra("chat_room_id", chatRoom.getId());
                intent.putExtra("name", chatRoom.getName());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        /**
         * Always check for google play services availability before
         * proceeding further with GCM
         * */
        if (checkPlayServices()) {
            registerGCM();
            fetchChatRooms();
        }
    }

    /**
     * Handles new push notification
     */
    private void handlePushNotification(Intent intent) {
        int type = intent.getIntExtra("type", -1);

        // if the push is of chat room message
        // simply update the UI unread messages count
        if (type == Config.PUSH_TYPE_CHATROOM) {
            Message message = (Message) intent.getSerializableExtra("message");
            String chatRoomId = intent.getStringExtra("chat_room_id");

            if (message != null && chatRoomId != null) {
                updateRow(chatRoomId, message);
            }
        } else if (type == Config.PUSH_TYPE_USER) {
            // push belongs to user alone
            // just showing the message in a toast
            Message message = (Message) intent.getSerializableExtra("message");
            Toast.makeText(getApplicationContext(), "New push: " + message.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    /**
     * Updates the chat list unread count and the last message
     */
    private void updateRow(String chatRoomId, Message message) {
        for (ChatRoom cr : chatRoomArrayList) {
            if (cr.getId().equals(chatRoomId)) {
                int index = chatRoomArrayList.indexOf(cr);
                cr.setLastMessage(message.getMessage());
                cr.setUnreadCount(cr.getUnreadCount() + 1);
                chatRoomArrayList.remove(index);
                chatRoomArrayList.add(index, cr);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }


    /**
     * fetching the chat rooms by making http call
     */
    private void fetchChatRooms() {
        StringRequest strReq = new StringRequest(Request.Method.GET,
                EndPoints.CHAT_ROOMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);

                    // check for error flag
                    if (obj.getBoolean("error") == false) {
                        JSONArray chatRoomsArray = obj.getJSONArray("chat_rooms");
                        for (int i = 0; i < chatRoomsArray.length(); i++) {
                            JSONObject chatRoomsObj = (JSONObject) chatRoomsArray.get(i);
                            ChatRoom cr = new ChatRoom();
                            cr.setId(chatRoomsObj.getString("chat_room_id"));
                            cr.setName(chatRoomsObj.getString("name"));
                            cr.setLastMessage("");
                            cr.setUnreadCount(0);
                            cr.setTimestamp(chatRoomsObj.getString("created_at"));

                            chatRoomArrayList.add(cr);
                        }

                    } else {
                        // error in fetching chat rooms
                        Toast.makeText(getApplicationContext(), "" + obj.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                mAdapter.notifyDataSetChanged();

                // subscribing to all chat room topics
                subscribeToAllTopics();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
                Toast.makeText(getApplicationContext(), "Volley error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    // subscribing to global topic
    private void subscribeToGlobalTopic() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
        intent.putExtra(GcmIntentService.TOPIC, Config.TOPIC_GLOBAL);
        startService(intent);
    }

    // Subscribing to all chat room topics
    // each topic name starts with `topic_` followed by the ID of the chat room
    // Ex: topic_1, topic_2
    private void subscribeToAllTopics() {
        for (ChatRoom cr : chatRoomArrayList) {

            Intent intent = new Intent(this, GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
            intent.putExtra(GcmIntentService.TOPIC, "topic_" + cr.getId());
            startService(intent);
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clearing the notification tray
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gcm, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_logout:
                MyApplication.getInstance().logout();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onBackPressed() {

        update_location("Offline");
        MainActivity.super.onBackPressed();

//        new AlertDialog.Builder(this)
//                .setTitle("Really Exit?")
//                .setMessage("Are you sure you want to exit?")
//                .setNegativeButton(android.R.string.no, null)
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        update_location("Offline");
//                        MainActivity.super.onBackPressed();
//                    }
//                }).create().show();
    }

    private void update_location(final String status) {

        String tag_string_req = "update_location";

        //Request.Priority priority = Request.Priority.HIGH;

        CustomPriorityRequest strReq = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_LOCATION ,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Location update Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");
                    if (error==1) {

                        Toast.makeText(getApplicationContext(), "Location successfully updated", Toast.LENGTH_LONG).show();
                    } else {

                        // Error occurred in updation of location. Get the error
                        // message
                        //String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                "Error", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Location", "Location Updation Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {

                db = new SQLiteHandler(getApplicationContext());
                session = new SessionManager(getApplicationContext(),"Login");

                if (!session.isLoggedIn()) {
                    logoutUser();
                }

                // Fetching user details from sqlite
                HashMap<String, String> user = db.getUserDetails();

                String name = user.get("name");
                String email = user.get("email");

                // Posting params to register url
//                Location currentLocation = LocationServices.FusedLocationApi
//                        .getLastLocation(mLocationClient);

//                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("status", status);

                return params;
            }

        };

        strReq.setPriority(Request.Priority.IMMEDIATE);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void logoutUser() {

        update_location("Offline");

        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, com.example.admin.loginandregistration.activity.LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}