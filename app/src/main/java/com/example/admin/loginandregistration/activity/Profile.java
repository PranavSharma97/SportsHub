package com.example.admin.loginandregistration.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.R;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.admin.loginandregistration.app.AppConfig;
import com.example.admin.loginandregistration.helper.CustomPriorityRequest;
import com.example.admin.loginandregistration.helper.GetAllImages;
import com.example.admin.loginandregistration.helper.RequestHandler;
import com.example.admin.loginandregistration.helper.RoundImage;
import com.example.admin.loginandregistration.helper.SQLiteHandler;
import com.example.admin.loginandregistration.helper.SessionManager;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.sportsHub.gcm.app.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Profile extends AppCompatActivity{

    public static String GET_IMAGE_URL;

    String dataFromSms;

    public GetAllImages getAllImages;

    private String email;

    private String contact_no;

    private String sport;

    private String user1;
    private String user2;
    private String interaction_as_sender;
    private String interaction_as_receiver;

    private String message;

    TextView tvName;
    TextView tvUsername;
    TextView tvGender;
    TextView tvAge;
    TextView tvMainSport;
    TextView tvSecondarySports;
    TextView tvStatus;
    TextView tvContactNo;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_LOCATIONS = "locations";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    private static final String TAG_USER = "user";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_SPORT = "sport";
    private static final String TAG_SPORT2 = "sport2";
    private static final String TAG_SPORT3 = "sport3";
    private static final String TAG_STATUS = "status";

    private static final String TAG_SENDER = "sender";
    private static final String TAG_RECEIVER = "receiver";
    private static final String TAG_INTERACTION = "interaction";

    private SQLiteHandler db;

    int createOrUpdate;

    public static final String BITMAP_ID = "BITMAP_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.material_design_profile_screen_xml_ui_design);

        Uri data = getIntent().getData();
        if(data!=null)
        {
            dataFromSms = data.toString()
                    .replaceAll("http://yourdomain.com/", "");

            email=dataFromSms;

            Toast.makeText(getApplicationContext(), dataFromSms, Toast.LENGTH_LONG).show();

        }
        else {
            Bundle extras = getIntent().getExtras();

            email = extras.getString("email");
        }

        if(!isNetworkAvailable())
        {
            Toast.makeText(Profile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        tvName = (TextView) findViewById(R.id.name);
        tvUsername = (TextView) findViewById(R.id.username);
        tvGender = (TextView) findViewById(R.id.gender);
        tvAge = (TextView) findViewById(R.id.age);
        tvMainSport = (TextView) findViewById(R.id.mainSport);
        tvSecondarySports = (TextView) findViewById(R.id.secondarySports);
        tvStatus = (TextView) findViewById(R.id.status);
        tvContactNo = (TextView) findViewById(R.id.contact_no);

        fetch_user_profile(email);

        fetch_user_location(email);

        fetch_interaction_as_sender();

        fetch_interaction_as_receiver();

        GET_IMAGE_URL=AppConfig.GET_IMAGE_URL + email;

//        listView = (ListView) findViewById(R.id.listView);
//        listView.setOnItemClickListener(this);
        getURLs();
    }

    private void getImages(){
        class GetImages extends AsyncTask<Void,Void,Void>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(Profile.this,"Loading...","Please wait...",false,false);
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                loading.dismiss();
                Toast.makeText(Profile.this,"Success",Toast.LENGTH_LONG).show();
                ImageView image = (ImageView) findViewById(R.id.imageDownloaded);

                RoundImage roundedImage;
                Bitmap bm;
//                bm = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                        R.drawable.man);

                bm = Bitmap.createScaledBitmap(GetAllImages.bitmaps[0], 500, 500, false);

                roundedImage = new RoundImage(bm);
                image.setImageDrawable(roundedImage);

//                image.setImageBitmap(Bitmap.createScaledBitmap(GetAllImages.bitmaps[0], 500, 500, false));

//                CustomList customList = new CustomList(ImageListView.this,GetAlImages.imageURLs,GetAlImages.bitmaps);
//                listView.setAdapter(customList);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    getAllImages.getAlImages();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        GetImages getImages = new GetImages();
        getImages.execute();
    }

    private void getURLs() {
        class GetURLs extends AsyncTask<String,Void,String>{

            ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(Profile.this,"Loading...","Please Wait...",true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                getAllImages = new GetAllImages(s);
                getImages();
            }

            @Override
            protected String doInBackground(String... strings) {
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(strings[0]);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }
            }
        }
        GetURLs gu = new GetURLs();
        gu.execute(GET_IMAGE_URL);
    }

//    @Override
//    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Intent intent = new Intent(this, ViewFullImage.class);
//        intent.putExtra(BITMAP_ID,i);
//        startActivity(intent);
//    }


    private void fetch_user_profile(final String email) {
        String tag_string_req_fetch = "req_user_profile";

        CustomPriorityRequest strReq2 = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_GET_USER_PROFILE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Fetching User Profile Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");

                    if (error == 1) {

                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String created_at = user
                                .getString("created_at");
                        String username = user.getString("username");
                        String gender = user.getString("gender");
                        contact_no = user.getString("contact_no");
                        int age = user.getInt("age");

                        db = new SQLiteHandler(getApplicationContext());

                        HashMap<String, String> tempUser = db.getUserDetails();

                        String tempEmail = tempUser.get("email");

                        if(tempEmail.equals(email))
                        {
                            Button btnAcceptInvitation = (Button) findViewById(R.id.btnAccept);
                            Button btnDeclineInvitation = (Button) findViewById(R.id.btnDecline);
                            Button btnInvite = (Button) findViewById(R.id.btnInvite);

                            btnAcceptInvitation.setVisibility(View.GONE);
                            btnDeclineInvitation.setVisibility(View.GONE);
                            btnInvite.setVisibility(View.GONE);
                        }

                        tvName.setText("Name : " + name);
                        tvUsername.setText("Username : " + username);
                        tvGender.setText("Gender : " + gender);
                        tvAge.setText("Age : " + age);
                        tvContactNo.setText("Contact No : " + contact_no);

                    } else {
                        // user profile not found
                        // Launch Main Activity
                        Intent i = new Intent(getApplicationContext(),
                                MainActivity.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Location", "Fetching Location Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

            // Posting params to register url

            Map<String, String> params = new HashMap<String, String>();
            params.put("email", email);

            return params;
        }

        };

        strReq2.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq2, tag_string_req_fetch);
    }

    private void fetch_user_location(final String email) {
        String tag_string_req_fetch = "req_user_location";

        CustomPriorityRequest strReq2 = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_GET_USER_LOCATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Fetching User location Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");

                    if (error == 1) {

                        JSONObject c = jObj.getJSONObject("location");

                        String user = c.getString(TAG_USER);
                        String email = c.getString(TAG_EMAIL);
                        double lat = c.getDouble(TAG_LAT);
                        double lng = c.getDouble(TAG_LNG);
                        sport = c.getString(TAG_SPORT);
                        String sport2 = c.getString(TAG_SPORT2);
                        String sport3 = c.getString(TAG_SPORT3);
                        String status = c.getString(TAG_STATUS);

                        tvMainSport.setText("Main Sport : " + sport);
                        tvSecondarySports.setText("Secondary Sports : " + sport2 + "" +
                                ", " + sport3);
                        tvStatus.setText("Status : " + status);

                    } else {
                        // user profile not found
                        // Launch Main Activity
                        Intent i = new Intent(getApplicationContext(),
                                MainActivity.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Location", "Fetching Location Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }

        };

        strReq2.setPriority(Request.Priority.NORMAL);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq2, tag_string_req_fetch);
    }

    private void fetch_interaction_as_sender() {

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        user1 = user.get("email");

        user2 = email;

        String tag_string_req_fetch = "req_interaction_as_sender";


        CustomPriorityRequest strReq2 = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_GET_USER_INTERACTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Fetching Interaction as sender Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");

                    createOrUpdate = error;

                    if (error == 1) {

                        JSONObject c = jObj.getJSONObject("interaction");

                        String sender = c.getString(TAG_SENDER);
                        String receiver = c.getString(TAG_RECEIVER);
                        interaction_as_sender = c.getString(TAG_INTERACTION);
                        int status = c.getInt(TAG_STATUS);

                        if(Integer.parseInt(interaction_as_sender)==1)
                        {
                            tvContactNo.setVisibility(View.VISIBLE);
                        }

                        if(status==0)
                        {

                            Button buttonInvite = (Button) findViewById(R.id.btnInvite);

                            buttonInvite.setEnabled(false);

                            TextView alreadyInvited = (TextView) findViewById(R.id.alreadyInvited);

                            alreadyInvited.setVisibility(View.VISIBLE);

//                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.acceptDeclineLayout);
//
//                            linearLayout.setVisibility(View.VISIBLE);

                        }

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Location", "Fetching Interaction as sender Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url

                Map<String, String> params = new HashMap<String, String>();
                params.put("sender", user1);
                params.put("receiver", user2);

                return params;
            }

        };

        strReq2.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq2, tag_string_req_fetch);
    }

    private void fetch_interaction_as_receiver() {

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        user1 = user.get("email");

        user2 = email;

        String tag_string_req_fetch = "req_interaction_as_receiver";


        CustomPriorityRequest strReq2 = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_GET_USER_INTERACTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Fetching Interaction as receiver Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");

                    if (error == 1) {

                        JSONObject c = jObj.getJSONObject("interaction");

                        String sender = c.getString(TAG_SENDER);
                        String receiver = c.getString(TAG_RECEIVER);
                        interaction_as_receiver = c.getString(TAG_INTERACTION);
                        int status = c.getInt(TAG_STATUS);

                        if(Integer.parseInt(interaction_as_receiver)==1)
                        {
                            tvContactNo.setVisibility(View.VISIBLE);
                        }

                        if(status==0)
                        {

                            Button buttonInvite = (Button) findViewById(R.id.btnInvite);

                            buttonInvite.setVisibility(View.GONE);

                            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.acceptDeclineLayout);

                            linearLayout.setVisibility(View.VISIBLE);

                        }

                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Location", "Fetching Interaction as receiver Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url

                Map<String, String> params = new HashMap<String, String>();
                params.put("sender", user2);
                params.put("receiver", user1);

                return params;
            }

        };

        strReq2.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq2, tag_string_req_fetch);
    }

    private void insert_interaction() {

        String tag_string_req = "create_interaction";

        CustomPriorityRequest strReq = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_CREATE_INTERACTION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Interaction Create Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");
                    if (error==1) {

                        Toast.makeText(getApplicationContext(), "Interaction successfully created", Toast.LENGTH_LONG).show();
                    } else {

                        // Error occurred in addition of location. Get the error
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
                Log.e("Interaction", "Interaction creation Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("sender", user1);
                params.put("receiver", user2);
                params.put("interaction", "0");
                params.put("status", "0");


                return params;
            }
        };

        strReq.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void update_interaction(final int status, final int interaction, final String sender, final String receiver) {

        String tag_string_req = "update_interaction";

        //Request.Priority priority = Request.Priority.HIGH;

        CustomPriorityRequest strReq = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_INTERACTION ,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Interaction update Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");
                    if (error==1) {

                        Toast.makeText(getApplicationContext(), "Interaction successfully updated", Toast.LENGTH_LONG).show();
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
                Log.e("Location", "Interaction Updation Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("sender", sender);
                params.put("receiver", receiver);
                params.put("interaction", String.valueOf(interaction));
                params.put("status", String.valueOf(status));

                return params;
            }

        };

        strReq.setPriority(Request.Priority.IMMEDIATE);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    public void inviteUser(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(Profile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        if(createOrUpdate==0)
        {
            insert_interaction();
        }
        else if(createOrUpdate==1)
        {
            if(Integer.parseInt(interaction_as_sender)==1)
            {
                update_interaction(0,1,user1,user2);
            }
            else
            {
                update_interaction(0,0,user1,user2);
            }
        }

        Button buttonInvite = (Button) findViewById(R.id.btnInvite);

        buttonInvite.setEnabled(false);

        send_sms_as_sender();
    }

    public void acceptInvitation(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(Profile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        if(Integer.parseInt(interaction_as_receiver)==0)
        {
            update_interaction(1, 1, user2, user1);
        }
        else if(Integer.parseInt(interaction_as_receiver)==1)
        {
            update_interaction(1,1,user2,user1);
        }

        TextView invitationAccepted = (TextView) findViewById(R.id.invitationAccepted);

        invitationAccepted.setVisibility(View.VISIBLE);

        send_sms_as_receiver(true);

        Button buttonAccept = (Button) findViewById(R.id.btnAccept);

        buttonAccept.setEnabled(false);

        Button buttonDecline = (Button) findViewById(R.id.btnDecline);

        buttonDecline.setEnabled(false);

    }

    public void declineInvitation(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(Profile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        if(Integer.parseInt(interaction_as_receiver)==0)
        {
            update_interaction(-1, 0, user2, user1);
        }
        else if(Integer.parseInt(interaction_as_receiver)==1)
        {
            update_interaction(-1,1,user2,user1);
        }

        TextView invitationDeclined = (TextView) findViewById(R.id.invitationDeclined);

        invitationDeclined.setVisibility(View.VISIBLE);

//        send_sms_as_receiver(false);

        Button buttonAccept = (Button) findViewById(R.id.btnAccept);

        buttonAccept.setEnabled(false);

        Button buttonDecline = (Button) findViewById(R.id.btnDecline);

        buttonDecline.setEnabled(false);

    }

    private void send_sms_as_sender() {

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");

        final String message;

        message = "Hi!\nYou have been invited by " + name + " to play " + sport + "" +
                ". You can view " + name + "'s profile and accept or decline the " +
                "invitation here : http://yourdomain.com/" + user1 + " ";

        String tag_string_req = "send_sms_as_sender";

        CustomPriorityRequest strReq = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_SEND_SMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("SMS", "Send sms Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Toast.makeText(getApplicationContext(), "SMS as Sender successfully sent", Toast.LENGTH_LONG).show();

                    } else {

                        // Error occurred in addition of location. Get the error
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
                Log.e("SMS", "SMS Sending Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", contact_no);
                params.put("message", message);

                return params;
            }
        };

        strReq.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void send_sms_as_receiver(boolean accepted) {

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");

        if(accepted==true)
        {
            message = "Hi!\nYour invitation to play " + sport + " has been accepted by " + name +
                    ". You can now view " + name + "'s CONTACT DETAILS on the profile" +
                    " here : http://yourdomain.com/" + user1 + " ";
        }
        else if(accepted==false)
        {
            message = "Hi!\nYour invitation to play " + sport + " has been declined by " + name +
                    ". You can look for other players or can reinvite " + name +
                    " here : http://yourdomain.com/" + user1 + " ";
        }

        String tag_string_req = "send_sms_as_receiver";

        CustomPriorityRequest strReq = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_SEND_SMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("SMS", "Send sms Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Toast.makeText(getApplicationContext(), "SMS as Receiver successfully sent", Toast.LENGTH_LONG).show();

                    } else {

                        // Error occurred in addition of location. Get the error
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
                Log.e("SMS", "SMS Sending Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("mobile", contact_no);
                params.put("message", message);

                return params;
            }
        };

        strReq.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}

