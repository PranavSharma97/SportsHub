package com.example.admin.loginandregistration.activity;

/**
 * Created by admin on 4/14/2016.
 */
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.R;
import com.example.admin.loginandregistration.app.AppConfig;
import com.example.admin.loginandregistration.app.AppController;
import com.example.admin.loginandregistration.helper.GetAllImages;
import com.example.admin.loginandregistration.helper.RequestHandler;
import com.example.admin.loginandregistration.helper.SQLiteHandler;
import com.example.admin.loginandregistration.helper.SessionManager;
import com.example.admin.loginandregistration.service.HttpService;
import com.sportsHub.gcm.app.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends Activity implements View.OnClickListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFullName;
    private EditText inputUsername;
    private EditText inputContactNo;
    private EditText inputAge;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private ProgressDialog loading;
    private SessionManager session;
    private SQLiteHandler db;

    //From SmsActivity
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private Button btnRequestSms, btnVerifyOtp;
    private EditText inputName, inputMobile, inputOtp;
    private ProgressBar progressBar;
    private ImageButton btnEditMobile;
    private TextView txtEditMobile;
    private LinearLayout layoutEditMobile;
    private SessionManager pref;

    public static final String UPLOAD_KEY = "image";
    public static final String EMAIL_KEY = "email";

    private int PICK_IMAGE_REQUEST = 1;

    private Bitmap bitmap;

    String name;
    String email;
    String password;
    String username;
    String contactNo;
    String age;

    Spinner spinner;
    String gender;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (Build.VERSION.SDK_INT >= 23) {
            new AlertDialog.Builder(this)
                    .setTitle("Important")
                    .setMessage("To verify the mobile number and automatically detect the" +
                            " OTP, we need the read SMS permission")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
                            } else {
                                //Do the stuff that requires permission...
                            }
                        }
                    }).create().show();
        }

        inputFullName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputUsername = (EditText) findViewById(R.id.username);
        inputContactNo = (EditText) findViewById(R.id.contact_no);
        inputAge = (EditText) findViewById(R.id.age);
        inputPassword = (EditText) findViewById(R.id.password);
//        btnRegister = (Button) findViewById(R.id.btn_request_sms);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);

        inputName = inputFullName;
        viewPager = (ViewPager) findViewById(R.id.viewPagerVertical);
        //inputName = (EditText) findViewById(R.id.inputName);
//        inputEmail = (EditText) findViewById(R.id.inputEmail);
        inputMobile = (EditText) findViewById(R.id.contact_no);
        inputOtp = (EditText) findViewById(R.id.inputOtp);
        btnRequestSms = (Button) findViewById(R.id.btn_request_sms);
        btnVerifyOtp = (Button) findViewById(R.id.btn_verify_otp);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnEditMobile = (ImageButton) findViewById(R.id.btn_edit_mobile);
        txtEditMobile = (TextView) findViewById(R.id.txt_edit_mobile);
        layoutEditMobile = (LinearLayout) findViewById(R.id.layout_edit_mobile);

        btnEditMobile.setOnClickListener(this);
        btnRequestSms.setOnClickListener(this);
        btnVerifyOtp.setOnClickListener(this);

        // hiding the edit mobile number
        layoutEditMobile.setVisibility(View.GONE);

        pref = new SessionManager(this,"abc");

        // Checking for user session
        // if user is already logged in, take him to main activity
//        if (pref.isLoggedIn()) {
//            Intent intent = new Intent(RegisterActivity.this, MainActivity2.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//
//            finish();
//        }

        adapter = new ViewPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        /**
         * Checking if the device is waiting for sms
         * showing the user OTP screen
         */
        if (pref.isWaitingForSms()) {
            viewPager.setCurrentItem(1);
            layoutEditMobile.setVisibility(View.VISIBLE);
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

//        loading = new ProgressDialog(this);
//        loading.setCancelable(false);

        spinner = (Spinner) findViewById(R.id.spinner);
        // Spinner click listener
//        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        final List<String> categories = new ArrayList<String>();
        categories.add("Male");
        categories.add("Female");
        categories.add("Other");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new  AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapterView,
                                       View view, int i, long l) {
                // TODO Auto-generated method stub
                Toast.makeText(RegisterActivity.this,"You Selected : "
                        + categories.get(i), Toast.LENGTH_SHORT).show();
                gender = categories.get(i);
            }
            // If no option selected
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });

        // Session manager
        session = new SessionManager(getApplicationContext(),"Login");

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this,
                    MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
//        btnRegister.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                name = inputFullName.getText().toString().trim();
//                email = inputEmail.getText().toString().trim();
//                password = inputPassword.getText().toString().trim();
//                username = inputUsername.getText().toString().trim();
//                contactNo = inputContactNo.getText().toString().trim();
//                age = inputAge.getText().toString().trim();
//
//                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()
//                        && !username.isEmpty() && !gender.isEmpty() && !contactNo.isEmpty()
//                        && !age.isEmpty()) {
//                    registerUser(name, email, password, username, gender, contactNo, age);
//                } else {
//                    Toast.makeText(getApplicationContext(),
//                            "Please enter your details!", Toast.LENGTH_LONG)
//                            .show();
//                }
//            }
//        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    /**
     * Function to store user in MySQL database will post params(tag, name,
     * email, password) to register url
     * */
    private void registerUser(final String name, final String email,
                              final String password, final String username, final String gender,
                              final String contactNo, final String age) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
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

                        // Inserting row in users table
//                        db.addUser(name, email, username, gender, contact_no, age, uid, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                RegisterActivity.this,
                                LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                params.put("username", username);
                params.put("gender", gender);
                params.put("contact_no", contactNo);
                params.put("age", age);

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkForUsernameAndEmail(final String email, final String username) {
        // Tag used to cancel the request
        String tag_string_req1 = "req_check_for_username_and_email";

        StringRequest strReq1 = new StringRequest(Method.POST,
                AppConfig.URL_CHECK_FOR_USERNAME_AND_EMAIL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Checking for username and email Response: " + response.toString());
//                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Toast.makeText(getApplicationContext(), "No Error in unique credentials!", Toast.LENGTH_LONG).show();
                        validateForm();

                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Checking for username and email Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
//                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("username", username);

                return params;
            }

        };
        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq1, tag_string_req1);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showImageDialog() {
        if (!loading.isShowing())
            loading.show();
    }

    private void hideImageDialog() {
        if (loading.isShowing())
            loading.dismiss();
    }

    //For SMS verification

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_request_sms:
                if(!isNetworkAvailable())
                {
                    Toast.makeText(RegisterActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                email = inputEmail.getText().toString().trim();
                username = inputUsername.getText().toString().trim();
                checkForUsernameAndEmail(email, username);
                break;

            case R.id.btn_verify_otp:
                if(!isNetworkAvailable())
                {
                    Toast.makeText(RegisterActivity.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                verifyOtp();
                break;

            case R.id.btn_edit_mobile:
                viewPager.setCurrentItem(0);
                layoutEditMobile.setVisibility(View.GONE);
                pref.setIsWaitingForSms(false);
                break;
        }
    }

    /**
     * Validating user details form
     */
    private void validateForm() {
//        String name = inputName.getText().toString().trim();
//        String email = inputEmail.getText().toString().trim();
//        String mobile = inputMobile.getText().toString().trim();

        // validating empty name and email
//        if (name.length() == 0 || email.length() == 0) {
//            Toast.makeText(getApplicationContext(), "Please enter your details", Toast.LENGTH_SHORT).show();
//            return;
//        }

        name = inputFullName.getText().toString().trim();
        email = inputEmail.getText().toString().trim();
        password = inputPassword.getText().toString().trim();
        username = inputUsername.getText().toString().trim();
        contactNo = inputContactNo.getText().toString().trim();
        age = inputAge.getText().toString().trim();

        if(!isValidEmail(email))
        {
            Toast.makeText(getApplicationContext(),
                    "Invalid Email!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()
                && !username.isEmpty() && !gender.isEmpty() && !contactNo.isEmpty()
                && !age.isEmpty()) {

        } else {
            Toast.makeText(getApplicationContext(),
                    "Please enter your details!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // validating mobile number
        // it should be of 10 digits length
        if (isValidPhoneNumber(contactNo)) {

            // request for sms
            progressBar.setVisibility(View.VISIBLE);

            // saving the mobile number in shared preferences
            pref.setMobileNumber(contactNo);

            // requesting for sms
            requestForSMS(name, email, contactNo);

        } else {
            Toast.makeText(getApplicationContext(), "Please enter valid mobile number", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method initiates the SMS request on the server
     *
     * @param name   user name
     * @param email  user email address
     * @param mobile user valid mobile number
     */
    private void requestForSMS(final String name, final String email, final String mobile) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REQUEST_SMS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    // checking for error, if not error SMS is initiated
                    // device should receive it shortly
                    if (!error) {
                        // boolean flag saying device is waiting for sms
                        pref.setIsWaitingForSms(true);

                        // moving the screen to next pager item i.e otp screen
                        viewPager.setCurrentItem(1);
                        txtEditMobile.setText(pref.getMobileNumber());
                        layoutEditMobile.setVisibility(View.VISIBLE);

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Error: " + message,
                                Toast.LENGTH_LONG).show();
                    }

                    // hiding the progress bar
                    progressBar.setVisibility(View.GONE);

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    progressBar.setVisibility(View.GONE);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }) {

            /**
             * Passing user parameters to our server
             * @return
             */
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", name);
                params.put("email", email);
                params.put("mobile", mobile);

                Log.e(TAG, "Posting params: " + params.toString());

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
    }

    /**
     * sending the OTP to server and activating the user
     */
    private void verifyOtp() {
        String otp = inputOtp.getText().toString().trim();

        if (!otp.isEmpty()) {
//            Intent grapprIntent = new Intent(getApplicationContext(), HttpService.class);
//            grapprIntent.putExtra("otp", otp);
//            startService(grapprIntent);
            verifyOtp(otp);
        } else {
            Toast.makeText(getApplicationContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Regex to validate the mobile number
     * mobile number should be of 10 digits length
     *
     * @param mobile
     * @return
     */
    private static boolean isValidPhoneNumber(String mobile) {
        String regEx = "^[0-9]{10}$";
        return mobile.matches(regEx);
    }


    class ViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((View) object);
        }

        public Object instantiateItem(View collection, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.layout_sms;
                    break;
                case 1:
                    resId = R.id.layout_otp;
                    break;
            }
            return findViewById(resId);
        }
    }


    private void verifyOtp(final String otp) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_VERIFY_OTP, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {

                    JSONObject responseObj = new JSONObject(response);

                    // Parsing json object response
                    // response will be a json object
                    boolean error = responseObj.getBoolean("error");
                    String message = responseObj.getString("message");

                    if (!error) {
                        // parsing the user profile information
                        JSONObject profileObj = responseObj.getJSONObject("profile");

//                        String name = profileObj.getString("name");
//                        String email = profileObj.getString("email");
                        String mobile = profileObj.getString("mobile");

//                        SessionManager pref = new SessionManager(getApplicationContext(),"abc");
//                        pref.createLogin(name, email, mobile);

                        registerUser(name, email, password, username, gender, contactNo, age);

                        Bitmap icon;

                        if(gender.equals("Male"))
                        {
                            icon = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                    R.drawable.man);
                            bitmap=icon;
                        }
                        else if(gender.equals("Female"))
                        {
                            icon = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                    R.drawable.woman);
                            bitmap=icon;
                        }

                        uploadImage();

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("otp", otp);

                Log.e(TAG, "Posting params: " + params.toString());
                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq);
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

            RequestHandler rh = new RequestHandler();

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                loading.setMessage("Uploading...");
//                showImageDialog();
                loading = ProgressDialog.show(RegisterActivity.this, "Uploading...", null,true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if ((loading != null) && loading.isShowing()) {
                    loading.dismiss();
                }

//                hideImageDialog();
//                loading.dismiss();
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Bitmap... params) {
                Bitmap bitmap = params[0];
                String uploadImage = getStringImage(bitmap);

//                HashMap<String, String> user = db.getUserDetails();

//                String email = user.get("email");
                HashMap<String,String> data = new HashMap<>();

                data.put(UPLOAD_KEY, uploadImage);
                data.put(EMAIL_KEY, email);
                String result = rh.sendPostRequest(AppConfig.UPLOAD_URL,data);

                return result;
            }
        }

        UploadImage ui = new UploadImage();
        ui.execute(bitmap);
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if ((loading != null) && loading.isShowing())
            loading.dismiss();
        loading = null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
