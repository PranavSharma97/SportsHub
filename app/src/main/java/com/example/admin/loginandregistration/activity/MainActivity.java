package com.example.admin.loginandregistration.activity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.admin.loginandregistration.helper.SQLiteHandler;
import com.example.admin.loginandregistration.helper.SessionManager;
import com.sportsHub.gcm.app.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    public static String GET_IMAGE_URL;

    public GetAllImages getAllImages;

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnChatRoom;
    private Button btnMyProfile;
    private Button btnMyMap;
    private SQLiteHandler db;
    private SessionManager session;
    private ImageView profilePhoto;

    int temp;

    static String selectedSport="";

    public static final String UPLOAD_KEY = "image";
    public static final String EMAIL_KEY = "email";

    private String email;

    private int PICK_IMAGE_REQUEST = 1;
    private Button buttonChoose;
    private Button buttonUpload;
    private Button buttonView;

    private ImageView imageView;

    private Bitmap bitmap;

    private Uri filePath;


    AlertDialog subjectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnMyMap = (Button) findViewById(R.id.btnMyMap);
        btnChatRoom = (Button) findViewById(R.id.btnChatRoom);
        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        btnMyProfile = (Button) findViewById(R.id.btnMyProfile);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext(),"Login");

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");
        String gender = user.get("gender");

        GET_IMAGE_URL=AppConfig.GET_IMAGE_URL + email;

//        Bitmap icon;
//
//        if(gender.equals("Male"))
//        {
//            icon = BitmapFactory.decodeResource(getBaseContext().getResources(),
//                    R.drawable.man);
//            bitmap=icon;
//        }
//        else if(gender.equals("Female"))
//        {
//            icon = BitmapFactory.decodeResource(getBaseContext().getResources(),
//                    R.drawable.woman);
//            bitmap=icon;
//        }
//
//        uploadImage();

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        btnMyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable())
                {
                    Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, MyProfile.class);
                startActivity(intent);
            }
        });
        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable())
                {
                    Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                logoutUser();
            }
        });

        btnMyMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable())
                {
                    Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                update_location("Online", false);
                //Intent intent = getPackageManager().getLaunchIntentForPackage("com.flipkart.android");
                //Intent intent = new Intent("android.intent.category.LAUNCHER");
                //intent.setClassName("com.flipkart.android", "com.flipkart.android.activity.FilterActivity");
                //Intent intent = new Intent("android.intent.category.LAUNCHER");
                //intent.setClassName("com.facebook.katana", "com.facebook.katana.LoginActivity");
                Intent intent = new Intent(MainActivity.this, MyMap.class);
                startActivity(intent);
                finish();
                //startActivity(intent);
                //showSportChooserDialog();
            }
        });

        btnChatRoom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable())
                {
                    Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                update_location("Online", false);
                Intent intent = new Intent(MainActivity.this, com.sportsHub.gcm.activity.LoginActivity.class);
                //Intent intent = new Intent("sportsHub.intent.action.Launch");
                startActivity(intent);
                //startActivity(intent);
                //showSportChooserDialog();
            }
        });

        if(!isNetworkAvailable())
        {
            Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        getURLs();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {

        update_location("Offline",true);

//        session.setLogin(false);
//
//        db.deleteUsers();
//
//        // Launching the login activity
//        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//        startActivity(intent);
//        finish();
    }

    private void update_location(final String status, final boolean logout) {

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
                        if(logout==true)
                        {
                            session.setLogin(false);

                            db.deleteUsers();

                            // Launching the login activity
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }

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

    public void changeProfilePhoto(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

//        HashMap<String, String> user = db.getUserDetails();
//
//        String email = user.get("email");
//        Intent intent = new Intent(MainActivity.this, ProfilePhoto.class);
//        intent.putExtra("email", email);
//        startActivity(intent);

        showFileChooser();
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        uploadImage();
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage(){
        class UploadImage extends AsyncTask<Bitmap,Void,String> {

            ProgressDialog loading;
            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Uploading...", null,false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String uploadImage = getStringImage(bitmap);

                HashMap<String, String> user = db.getUserDetails();

                String email = user.get("email");
                HashMap<String,String> data = new HashMap<>();

                data.put(UPLOAD_KEY, uploadImage);
                data.put(EMAIL_KEY, email);
                String result = rh.sendPostRequest(AppConfig.UPDATE_PROFILE_PHOTO_URL,data);

                return result;
            }
        }

        UploadImage ui = new UploadImage();
        ui.execute(bitmap);
    }




    private void getImages(){
        class GetImages extends AsyncTask<Void,Void,Void>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                loading = ProgressDialog.show(MainActivity.this,"Loading...","Please wait...",false,false);
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                loading.dismiss();
                Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_LONG).show();
                ImageView image = (ImageView) findViewById(R.id.profilePhoto);

                //image.setImageBitmap(Bitmap.createScaledBitmap(GetAllImages.bitmaps[0], 100, 100, false));

                image.setImageBitmap(getAllImages.bitmaps[0]);

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
                loading = ProgressDialog.show(MainActivity.this,"Loading...","Please Wait...",true,true);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

//    public void viewMyProfile(View v)
//    {
//        if(!isNetworkAvailable())
//        {
//            Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
//            return;
//        }
//        Intent intent = new Intent(MainActivity.this, MyProfile.class);
//        startActivity(intent);
//    }
//
//    public void performLogout(View v)
//    {
//        if(!isNetworkAvailable())
//        {
//            Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
//            return;
//        }
//        logoutUser();
//    }
//
//    public void viewChatRooms(View v)
//    {
//        if(!isNetworkAvailable())
//        {
//            Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
//            return;
//        }
//        update_location("Online", false);
//        Intent intent = new Intent(MainActivity.this, com.sportsHub.gcm.activity.LoginActivity.class);
//        startActivity(intent);
//    }
//
//    public void viewMyMap(View v)
//    {
//        if(!isNetworkAvailable())
//        {
//            Toast.makeText(MainActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
//            return;
//        }
//        update_location("Online",false);
//        Intent intent = new Intent(MainActivity.this, MyMap.class);
//        startActivity(intent);
//        finish();
//    }

}