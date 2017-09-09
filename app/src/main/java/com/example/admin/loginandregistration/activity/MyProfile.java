package com.example.admin.loginandregistration.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.sportsHub.gcm.app.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyProfile extends AppCompatActivity{

    public static String GET_IMAGE_URL;

    public GetAllImages getAllImages;

    private String email;

    TextView tvGender;
//    TextView tvMainSport;
//    TextView tvSecondarySports;
    TextView tvStatus;
    TextView tvContactNo;
    TextView tvEmail;
    TextView tvName;

    private ImageView profilePhoto;

    public static final String UPLOAD_KEY = "image";
    public static final String EMAIL_KEY = "email";

    private int PICK_IMAGE_REQUEST = 1;


    //    EditText editName;
    EditText editUsername;
//    EditText editEmail;
    EditText editAge;
    EditText newPassword;

    Button editProfileInformation;

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
    private SQLiteHandler db;

    private Bitmap bitmap;

    private Uri filePath;

    String name;
    String emailFromTextView;
    String password;
    String username;
    String contactNo;
    String age;
    String gender;

    private boolean isPasswordChanged = false;

    public static final String BITMAP_ID = "BITMAP_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_my_profile);

        if(!isNetworkAvailable())
        {
            Toast.makeText(MyProfile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        tvName = (TextView) findViewById(R.id.name);
        editUsername = (EditText) findViewById(R.id.username);
        tvEmail = (TextView) findViewById(R.id.email);
        tvGender = (TextView) findViewById(R.id.gender);
        editAge = (EditText) findViewById(R.id.age);
//        tvMainSport = (TextView) findViewById(R.id.mainSport);
//        tvSecondarySports = (TextView) findViewById(R.id.secondarySports);
        tvStatus = (TextView) findViewById(R.id.status);
        tvContactNo = (TextView) findViewById(R.id.contactNo);
        newPassword = (EditText) findViewById(R.id.newPassword);

        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);

        db = new SQLiteHandler(getApplicationContext());

        HashMap<String, String> user = db.getUserDetails();

        email = user.get("email");

        fetch_user_profile(email);

        fetch_user_location(email);

        GET_IMAGE_URL=AppConfig.GET_IMAGE_URL + email;

//        listView = (ListView) findViewById(R.id.listView);
//        listView.setOnItemClickListener(this);
        getURLs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    public void saveChanges(MenuItem item)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(MyProfile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }
        name = tvName.getText().toString().trim();
        emailFromTextView = tvEmail.getText().toString().trim();
        username = editUsername.getText().toString().trim();
        contactNo = tvContactNo.getText().toString().trim();
        age = editAge.getText().toString().trim();
        gender = tvGender.getText().toString().trim();

        if(isPasswordChanged==true)
        {
            password = newPassword.getText().toString().trim();
            if(password.length()==0)
            {
                password = "false";
            }
        }
        else{
            password = "false";
        }

        Toast.makeText(getApplicationContext(), " " + name + email+password+username+gender+age+contactNo, Toast.LENGTH_LONG).show();

        update_user_profile();

    }

    private void getImages(){
        class GetImages extends AsyncTask<Void,Void,Void>{
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MyProfile.this,"Loading...","Please wait...",false,false);
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);
                loading.dismiss();
                Toast.makeText(MyProfile.this,"Success",Toast.LENGTH_LONG).show();
                ImageView image = (ImageView) findViewById(R.id.profilePhoto);

                RoundImage roundedImage;
                Bitmap bm;

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
                loading = ProgressDialog.show(MyProfile.this,"Loading...","Please Wait...",true,true);
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
                        String contact_no = user.getString("contact_no");
                        int age = user.getInt("age");

                        tvName.setText(name);
                        editUsername.setText(username);
                        tvGender.setText(gender);
                        editAge.setText(String.valueOf(age));
                        tvEmail.setText(email);
                        tvContactNo.setText(contact_no);

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
                        String sport = c.getString(TAG_SPORT);
                        String sport2 = c.getString(TAG_SPORT2);
                        String sport3 = c.getString(TAG_SPORT3);
                        String status = c.getString(TAG_STATUS);

//                        tvMainSport.setText("Main Sport : " + sport);
//                        tvSecondarySports.setText("Secondary Sports : " + sport2 + "" +
//                                ", " + sport3);
                        tvStatus.setText("Status : " + status);

                    } else {
                        // user profile not found
                        // Launch Main Activity
//                        Intent i = new Intent(getApplicationContext(),
//                                MainActivity.class);
//                        // Closing all previous activities
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);

                        tvStatus.setText("Status : Offline");

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

    public void editProfileInformation(View v)
    {
//        editName.setEnabled(true);
        editUsername.setEnabled(true);
//        editEmail.setEnabled(true);
        editAge.setEnabled(true);

    }

    public void changePassword(View v)
    {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.changePasswordLayout);

        linearLayout.setVisibility(View.VISIBLE);


        isPasswordChanged=true;
    }

    public void cancelChangePassword(View v)
    {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.changePasswordLayout);

        linearLayout.setVisibility(View.GONE);

        isPasswordChanged=false;
    }

    private void update_user_profile() {
        String tag_string_req_fetch = "update_user_profile";

        CustomPriorityRequest strReq2 = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_UPDATE_USER_PROFILE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Updating User Profile Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");

                    if (error == 1) {

                        Toast.makeText(getApplicationContext(), "User Profile successfully updated", Toast.LENGTH_LONG).show();
//                        Intent i = new Intent(getApplicationContext(),
//                                MainActivity.class);
//                        // Closing all previous activities
////                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);
//                        finish();

                    } else {
                        // user profile not updated
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
                Log.e("Location", "Updating User Profile Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url

                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", emailFromTextView);
                params.put("password", password);
                params.put("username", username);
                params.put("gender", gender);
                params.put("contact_no", contactNo);
                params.put("age", age);

                return params;
            }

        };

        strReq2.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq2, tag_string_req_fetch);
    }

    public void changeProfilePhoto(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(MyProfile.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }
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

                RoundImage roundedImage;

                roundedImage = new RoundImage(bitmap);
                profilePhoto.setImageDrawable(roundedImage);

//                profilePhoto.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        uploadImage();
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 40, baos);
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
                loading = ProgressDialog.show(MyProfile.this, "Uploading...", null,true,true);
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}


