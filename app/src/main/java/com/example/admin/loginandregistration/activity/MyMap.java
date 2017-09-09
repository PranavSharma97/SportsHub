package com.example.admin.loginandregistration.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sportsHub.gcm.app.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMap extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemSelectedListener {

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };
    private static final String[] LOCATION_COARSE_PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final String[] CONTACTS_PERMS = {
            Manifest.permission.READ_CONTACTS
    };
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_COARSE_REQUEST = INITIAL_REQUEST + 1;
    private static final int CONTACTS_REQUEST = INITIAL_REQUEST + 2;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;

    private static final int REQUEST_CODE = 1;

    GoogleMap mMap;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final double
            SEATTLE_LAT = 47.60621,
            SEATTLE_LNG = -122.33207,
            SYDNEY_LAT = -33.867487,
            SYDNEY_LNG = 151.20699,
            NEWYORK_LAT = 40.714353,
            NEWYORK_LNG = -74.005973;

    //private static final int POLYGON_POINTS = 3;

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

    private static final String TAG_USERNAME = "username";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_AGE = "age";


    String username;
    String gender;
    int age;

    int checked;

    String spinnerSport;
    ArrayAdapter myAdap;
    int spinnerPosition;

    ArrayList seletedItems;

    AlertDialog subjectDialog;

    Spinner spinner;

    String selectedSport;
    String selectedSportsSecondary[] = new String[10];
    static String selectedSportInDialog = " ";
    String selectedSportAll = " ";
    int count = 0;

    boolean isSportChanged;
    boolean showAllSports = false;
    boolean isFindMatchClicked = false;

    private HashMap<Marker, JSONObject> locationMarkerMap = new HashMap<Marker, JSONObject>();
    private HashMap<Marker, JSONObject> userMarkerMap = new HashMap<Marker, JSONObject>();
    private HashMap<String, Marker> updatedMarkerMap = new HashMap<String, Marker>();
    private HashMap<Integer, Boolean> selectedSportsBoolean;
    private HashMap<String, Integer> getIndexFromStringName = new HashMap<String, Integer>();
    private HashMap<Integer, String> getStringNameFromIndex = new HashMap<Integer, String>();

    private SessionManager session;
    //    private SessionManager sportSession;
    private SQLiteHandler db;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private GoogleApiClient mLocationClient;
    //List<Marker> markers = new ArrayList<>();
    //Polygon shape;
    //private Marker marker1,marker2;
    private Marker marker;
    //Polyline line;
    //private LocationListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_map);

        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        MenuItem item = (MenuItem) findViewById(R.id.currentLocation);

        pref = getApplicationContext().getSharedPreferences("Sports", 0);
        editor = pref.edit();
//        editor.clear();
//        editor.commit();

        selectedSportsBoolean = new HashMap<Integer, Boolean>();

        getIndexFromStringName.put("Cricket", 0);
        getIndexFromStringName.put("Football", 1);
        getIndexFromStringName.put("Badminton", 2);
        getIndexFromStringName.put("Basketball", 3);
        getIndexFromStringName.put("Lawn Tennis", 4);
        getIndexFromStringName.put("Volleyball", 5);

        getStringNameFromIndex.put(0, "Cricket");
        getStringNameFromIndex.put(1, "Football");
        getStringNameFromIndex.put(2, "Badminton");
        getStringNameFromIndex.put(3, "Basketball");
        getStringNameFromIndex.put(4, "Lawn Tennis");
        getStringNameFromIndex.put(5, "Volleyball");

//        sportSession = new SessionManager(getApplicationContext(),"Login");
//        Toast.makeText(MyMap.this," " + pref.get, Toast.LENGTH_LONG).show();


//        Toast.makeText(MyMap.this," " + pref.getBoolean("isSportSet", true), Toast.LENGTH_LONG).show();


//        spinner = (Spinner) findViewById(R.id.spinner);
//        // Spinner click listener
//        spinner.setOnItemSelectedListener(this);
//
//        // Spinner Drop down elements
//        final List<String> categories = new ArrayList<String>();
//        categories.add("Cricket");
//        categories.add("Football");
//        categories.add("Badminton");
//        categories.add("Basketball");
//        categories.add("Lawn Tennis");
//        categories.add("All");
//
//        // Creating adapter for spinner
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        /*{
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View v = convertView;

                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }

                CheckedTextView tv = (CheckedTextView) v.findViewById(R.id.spinnerTarget);
                tv.setText(categories.get(position));

                switch (position) {
                    case 0:  tv.setBackgroundColor(Color.RED);
                        break;
                    case 1:  tv.setBackgroundColor(Color.BLUE);
                        break;
                    default:  tv.setBackgroundColor(Color.BLACK);
                        break;
                }
                return v;
            }
        };
        */

        // Drop down layout style - list view with radio button
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // attaching data adapter to spinner
//        spinner.setAdapter(dataAdapter);

//        if(!sportSession.isSportSet())
        if (!pref.getBoolean("isSportSet", false)) {
            showMainSportDialog(false);
        }

        if (servicesOK()) {

            if (initMap()) {
//                gotoLocation(SYDNEY_LAT, SYDNEY_LNG, 15);

                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                mLocationClient.connect();

//                if(canAccessCoarseLocation() && canAccessCoarseLocation())
//                mMap.setMyLocationEnabled(true);
            }
            else
            {
                Toast.makeText(this,"Map not connected!",Toast.LENGTH_LONG).show();
            }


        }
        else
        {
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id)
        {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean servicesOK()
    {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(isAvailable == ConnectionResult.SUCCESS)
        {
            return true;
        }
        else if(GooglePlayServicesUtil.isUserRecoverableError(isAvailable))
        {
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvailable,this,ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else
        {
            Toast.makeText(this,"Can't connect to mapping service",Toast.LENGTH_LONG).show();
        }

        return false;
    }

    private boolean initMap()
    {
        if(mMap == null)
        {
            SupportMapFragment mapFragment =
                    (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();
        }

        if(mMap!=null)
        {
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window,null);
                    TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);

                    LatLng latLng = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    //tvLat.setText("email: " + latLng.latitude);
                    //tvLng.setText("Longitude: " + latLng.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });

            /*mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Geocoder gc = new Geocoder(MyMap.this);
                    List<Address> list = null;
                    try {
                        list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    Address add = list.get(0);
                    MyMap.this.addMarker(add, latLng.latitude, latLng.longitude);

                }
            });
            */

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String msg = marker.getTitle() + " (" + marker.getPosition().latitude + ", "
                            + marker.getPosition().longitude
                            + ")";

                    Toast.makeText(MyMap.this, msg, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });

            /*mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder gc = new Geocoder(MyMap.this);
                    List<Address> list = null;
                    LatLng ll = marker.getPosition();
                    try {
                        list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.setSnippet(add.getCountryName());
                    marker.showInfoWindow();
                }
            });
            */
        }

        return(mMap!=null);
    }

    private void gotoLocation(double lat,double lng,float zoom)
    {
        LatLng latLng = new LatLng(lat,lng);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
        mMap.moveCamera(update);

    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void findPeople(View v) throws JSONException, IOException {

        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        isSportChanged=true;

        showAllSports = false;

        insert_location();

        fetch_location();

        fetch_location();

    }

    public void changeSports(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }
        showMainSportDialog(true);
    }

    public void hideLocation(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }
        db = new SQLiteHandler(getApplicationContext());
        session = new SessionManager(getApplicationContext(),"Login");

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

//        sportSession.setUserChoiceOfSports(false);
//        editor.putBoolean("isSportSet", false);
//        editor.clear();
        // commit changes
//        editor.commit();

        Log.d("Sport Session", "User sport session modified!");

        delete_location(email);
        fetch_location();
        fetch_location();
    }

    public void showAllSports(View v)
    {
        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }
        showAllSports = true;
        fetch_location();
    }

    private void delete_location(String email) {

        String tag_string_req = "delete_location";

        //Request.Priority priority = Request.Priority.HIGH;

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE_LOCATION ,new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Location delete Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");
                    if (error==1) {

                        Toast.makeText(getApplicationContext(), "Location successfully deleted", Toast.LENGTH_LONG).show();
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
                Log.e("Location", "Location Deletion Error: " + error.getMessage());
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
                Location currentLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mLocationClient);

                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);

                return params;
            }
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void update_location(final String sport,final String sport2,final String sport3,final String status) {

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
                Location currentLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mLocationClient);

                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("sport", sport);
                params.put("sport2", selectedSportsSecondary[0]);
                params.put("sport3", selectedSportsSecondary[1]);
                params.put("status", status);

                return params;
            }

        };

        strReq.setPriority(Request.Priority.IMMEDIATE);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void fetch_location() {

        mMap.clear();

//        if(sportSession.isSportSet())
        if(pref.getBoolean("isSportSet", true))
        {

//            String sports[] = sportSession.getSports();
//
//            selectedSport = sports[0];
//            selectedSportsSecondary[0] = sports[1];
//            selectedSportsSecondary[1] = sports[2];

            selectedSport = pref.getString("mainsport"," ");
            selectedSportsSecondary[0] = pref.getString("secondarysport"," ");
            selectedSportsSecondary[1] = pref.getString("tertiarysport"," ");

        }

        String tag_string_req_fetch = "req_location";

        CustomPriorityRequest strReq2 = new CustomPriorityRequest(Request.Method.GET,
                AppConfig.URL_GET_ALL_LOCATIONS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Fetching Location Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");

                    if (error==1) {

                        JSONArray locations = jObj.getJSONArray(TAG_LOCATIONS);

                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject c = locations.getJSONObject(i);

                            // Storing each json item in variable
                            String user = c.getString(TAG_USER);
                            String email = c.getString(TAG_EMAIL);
                            double lat = c.getDouble(TAG_LAT);
                            double lng = c.getDouble(TAG_LNG);
                            String sport = c.getString(TAG_SPORT);
                            String sport2 = c.getString(TAG_SPORT2);
                            String sport3 = c.getString(TAG_SPORT3);
                            String status = c.getString(TAG_STATUS);

                            //Toast.makeText(MyMap.this,user+email+lat+lng+sport, Toast.LENGTH_LONG).show();

                            Geocoder gc = new Geocoder(MyMap.this);
                            List<Address> list = null;
                            try {
                                list = gc.getFromLocation(lat, lng, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            Address add = list.get(0);

//                            fetch_user_profile(add, lat, lng, user, email, sport,sport2,sport3,status, c);

                            if(String.valueOf(showAllSports).equals("true"))
                            {
                                fetch_user_profile(add, lat, lng, user, email, sport, sport2, sport3, status, c);
                            }
                            else if(selectedSport.equals(sport) || selectedSportsSecondary[0].equals(sport) || selectedSportsSecondary[1].equals(sport)) {
                                fetch_user_profile(add, lat, lng, user, email, sport, sport2, sport3, status, c);
                            }

//                            if(selectedSportAll.equals("All"))
//                            {
//                                addMarker(add, lat, lng, user, email, sport,sport2,sport3,status, c);
//                            }
//                            else if(selectedSport.equals(sport)) {
//                                addMarker(add, lat, lng, user, email, sport, sport2,sport3,status,  c);
//                            }

                            //Toast.makeText(getApplicationContext(), "Location successfully added", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        // no locations found
                        // Launch Add New product Activity
//                        Intent i = new Intent(getApplicationContext(),
//                                MainActivity.class);
//                        // Closing all previous activities
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);
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

        };

        strReq2.setPriority(Request.Priority.LOW);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq2, tag_string_req_fetch);

    }

    private void insert_location() {

        isFindMatchClicked = true;

        String tag_string_req = "create_location";

        CustomPriorityRequest strReq = new CustomPriorityRequest(Request.Method.POST,
                AppConfig.URL_CREATE_LOCATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d("Location", "Location Create Response: " + response.toString());
                //hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    Integer error = jObj.getInt("success");
                    if (error==1) {

                        Toast.makeText(getApplicationContext(), "Location successfully added", Toast.LENGTH_LONG).show();
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
                Log.e("Location", "Location creation Error: " + error.getMessage());
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
                Location currentLocation = LocationServices.FusedLocationApi
                        .getLastLocation(mLocationClient);

                LatLng latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

                Map<String, String> params = new HashMap<String, String>();
                params.put("user", name);
                params.put("email", email);
                params.put("lat", String.valueOf(latLng.latitude));
                params.put("lng", String.valueOf(latLng.longitude));
                params.put("sport", selectedSport);
                params.put("sport2", selectedSportsSecondary[0]);
                params.put("sport3", selectedSportsSecondary[1]);
                params.put("status", "Online");

                return params;
            }
        };

        strReq.setPriority(Request.Priority.HIGH);

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void fetch_user_profile(final Address add, final double lat, final double lng, final String user1, final String email, final String sport, final String sport2, final String sport3,final String status, final JSONObject c) {
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

                        addMarker(add, lat, lng, user1, email, sport,sport2,sport3,status, c, user);

                    } else {
                        // user profile not found
                        // Launch Main Activity
//                        Intent i = new Intent(getApplicationContext(),
//                                MainActivity.class);
//                        // Closing all previous activities
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(i);
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

    public void geoLocate(View v) throws IOException {
        hideSoftKeyboard(v);

        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        TextView tv = (TextView)findViewById(R.id.editText1);
        String searchString = tv.getText().toString();
        Toast.makeText(this, "Searching for : " + searchString, Toast.LENGTH_SHORT).show();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if(list.size()>0)
        {
            Address add = list.get(0);
            String locality = add.getLocality();
            Toast.makeText(this,"Found: " + locality  ,Toast.LENGTH_LONG).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();

            gotoLocation(lat,lng,15);

            /*
            if(marker1!=null)
            {
                marker1.remove();
            }
            */

            //addMarker(add,lat,lng,"a","a","a");

        }

    }

    private void addMarker(Address add, final double lat, final double lng, String user, final String email, final String sport, final String sport2, final String sport3,final String status, final JSONObject c,  final JSONObject userDetails) throws JSONException {
        /*if(markers.size() == POLYGON_POINTS)
        {
            removeEverything();
        }
        */

        int height = 140;
        int width = 140;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.ic_compositeicon3);
        Bitmap b=bitmapdraw.getBitmap();
        final Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        MarkerOptions options = new MarkerOptions()
                .title(user)
                .position(new LatLng(lat, lng))
                //.icon(BitmapDescriptorFactory.defaultMarker())
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                .snippet(userDetails.getString(TAG_GENDER) + ", " + userDetails.getString(TAG_AGE) + "yrs");
//                .draggable(true);


        db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from sqlite
        HashMap<String, String> tempUser = db.getUserDetails();

        String tempEmail = tempUser.get("email");
        if(tempEmail.equals(email))
        {
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current));
        }
        else if (c.getString(TAG_SPORT).equals("Cricket")) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cricketicon));
        } else if (c.getString(TAG_SPORT).equals("Football")) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//            options.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
        } else if (c.getString(TAG_SPORT).equals("Badminton")) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
//            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_badminton_512));
        } else if (c.getString(TAG_SPORT).equals("Basketball")) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
//            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.basketball2noun));
        } else if (c.getString(TAG_SPORT).equals("Lawn Tennis")) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tennisicon));
        } else if (c.getString(TAG_SPORT).equals("Volleyball")) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
//            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_tennisicon));
        }

        marker = mMap.addMarker(options);
        locationMarkerMap.put(marker,c);
        userMarkerMap.put(marker,userDetails);
        updatedMarkerMap.put(email,marker);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window, null);
                TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                ImageView infoWindowImage = (ImageView) v.findViewById(R.id.imageView1);
                ImageView status = (ImageView) v.findViewById(R.id.status);
                TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);
                JSONObject location = locationMarkerMap.get(marker);
                JSONObject user = userMarkerMap.get(marker);

                try {
                    if (location.getString("status").equals("Online")) {
                        status.setImageResource(R.drawable.ic_online);
                    } else if (location.getString("status").equals("Offline")) {
                        status.setImageResource(R.drawable.ic_offline);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    if (location.getString(TAG_SPORT).equals("Cricket")) {
                        infoWindowImage.setImageResource(R.mipmap.cricket);
                    } else if (location.getString(TAG_SPORT).equals("Football")) {
//                        infoWindowImage.setImageBitmap(smallMarker);
                        infoWindowImage.setImageResource(R.mipmap.footbal);
                    } else if (location.getString(TAG_SPORT).equals("Badminton")) {
                        infoWindowImage.setImageResource(R.mipmap.badminton);
                    } else if (location.getString(TAG_SPORT).equals("Basketball")) {
                        infoWindowImage.setImageResource(R.mipmap.basketball);
                    } else if (location.getString(TAG_SPORT).equals("Lawn Tennis")) {
                        infoWindowImage.setImageResource(R.mipmap.lawntennis);
                    } else if (location.getString(TAG_SPORT).equals("Volleyball")) {
                        infoWindowImage.setImageResource(R.mipmap.ic_volleyball_new);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                setImage(sport);

                LatLng latLng = marker.getPosition();
                tvLocality.setText(marker.getTitle());
                try {
                    tvLat.setText("username: " + user.getString(TAG_USERNAME));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    tvLng.setText("Primary sport: " + location.getString(TAG_SPORT));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tvSnippet.setText(marker.getSnippet());

                return v;
            }

        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(!isNetworkAvailable())
                {
                    Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject location = locationMarkerMap.get(marker);
                Intent intent = new Intent(MyMap.this, Profile.class);
                try {
                    intent.putExtra("email",location.getString(TAG_EMAIL));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        String country = add.getCountryName();
        /*if(country.length()>0)
        {
            options.snippet(country);
        }
        */

        //markers.add(mMap.addMarker(options));

        /*if(markers.size() == POLYGON_POINTS)
        {
            drawPolygon();
        }
        */

    }

    private void setImage(String sport) {

        ImageView infoWindowImage = (ImageView)findViewById(R.id.imageView1);

        if(sport.equals("Cricket"))
        {
            infoWindowImage.setImageResource(R.mipmap.cricket);
        }
        else if(sport.equals("Football"))
        {
            infoWindowImage.setImageResource(R.mipmap.footbal);
        }
        else if(sport.equals("Badminton"))
        {
            infoWindowImage.setImageResource(R.mipmap.badminton);
        }
        else if(sport.equals("Basketball"))
        {
            infoWindowImage.setImageResource(R.mipmap.basketball);
        }
        else if(sport.equals("Lawn Tennis"))
        {
            infoWindowImage.setImageResource(R.mipmap.lawntennis);
        }

    }

    /*private void drawline() {
        PolylineOptions lineOptions = new PolylineOptions()
                .add(marker1.getPosition())
                .add(marker2.getPosition());
        line = mMap.addPolyline(lineOptions);


    }
    */

    /*private void drawPolygon()
    {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.BLUE);

        for(int i=0; i<POLYGON_POINTS; i++)
        {
            options.add(markers.get(i).getPosition());
        }

        shape = mMap.addPolygon(options);
    }
    */

    /*private void removeEverything()
    {
        for(Marker marker : markers)
        {
            marker.remove();
        }

        markers.clear();
        if(shape!=null)
        {
            shape.remove();
            shape = null;
        }

        /*marker1.remove();
        marker1 = null;
        marker2.remove();
        marker2 = null;
        if(line!=null)
        {
            line.remove();
        }

    }
    */



    public void showCurrentLocation(MenuItem item) throws IOException {

        if(!isNetworkAvailable())
        {
            Toast.makeText(MyMap.this, "No Internet Access", Toast.LENGTH_LONG).show();
            return;
        }

        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);

        if(currentLocation == null)
        {
//            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(MyMap.this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE+2);
//            } else {
//                //Do the stuff that requires permission...
//            }

            if (Build.VERSION.SDK_INT >= 23) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MyMap.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
                } else {
                    //Do the stuff that requires permission...
                }

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MyMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE + 1);
                } else {
                    //Do the stuff that requires permission...
                }
            }

//            Toast.makeText(this,"Couldn't Connect!",Toast.LENGTH_LONG).show();

        } else
        {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng,15);
            mMap.animateCamera(update);

            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocation(latLng.latitude,latLng.longitude,1);

            if(isFindMatchClicked==false) {
                MarkerOptions options = new MarkerOptions()
                        .title("Current Location")
                        .position(new LatLng(latLng.latitude, latLng.longitude))
//                    .icon(BitmapDescriptorFactory.defaultMarker())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current))
                        .snippet("This is the current location");

                mMap.addMarker(options);
                }
            //Address add = list.get(0);

            //addMarker(add,latLng.latitude,latLng.longitude);
        }
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(MyMap.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSportChooserDialog(final boolean isChanged) {

        count = pref.getInt("count",0);
//        Toast.makeText(MyMap.this," " + pref.getBoolean("isSportSet", true), Toast.LENGTH_LONG).show();
        selectedSport=selectedSportInDialog;

        if(isChanged==false)
        {
            selectedSportsBoolean.put(0,false);
            selectedSportsBoolean.put(1,false);
            selectedSportsBoolean.put(2,false);
            selectedSportsBoolean.put(3,false);
            selectedSportsBoolean.put(4,false);
            selectedSportsBoolean.put(5,false);
        }
        AlertDialog dialog;
        // arraylist to keep the selected items
        seletedItems = new ArrayList();

//        if((sportSession.isSportSet()))
        if(pref.getBoolean("isSportSet", false))
        {
            Log.d("Sport chooser", "Inside if statement");
//            Toast.makeText(MyMap.this,"Inside if statement", Toast.LENGTH_LONG).show();
//            selectedSportsBoolean.put(0,sportSession.getUserSecondarySportSelection(String.valueOf(0)));
//            selectedSportsBoolean.put(1,sportSession.getUserSecondarySportSelection(String.valueOf(1)));
//            selectedSportsBoolean.put(2,sportSession.getUserSecondarySportSelection(String.valueOf(2)));
//            selectedSportsBoolean.put(3,sportSession.getUserSecondarySportSelection(String.valueOf(3)));
//            selectedSportsBoolean.put(4,sportSession.getUserSecondarySportSelection(String.valueOf(4)));


            selectedSportsBoolean.put(0,pref.getBoolean(String.valueOf(0), false));
            selectedSportsBoolean.put(1,pref.getBoolean(String.valueOf(1), false));
            selectedSportsBoolean.put(2,pref.getBoolean(String.valueOf(2), false));
            selectedSportsBoolean.put(3,pref.getBoolean(String.valueOf(3), false));
            selectedSportsBoolean.put(4,pref.getBoolean(String.valueOf(4), false));
            selectedSportsBoolean.put(5,pref.getBoolean(String.valueOf(5), false));
        }

//        LayoutInflater inflater = getLayoutInflater();
//        View alertLayout = inflater.inflate(R.layout.alertdialoglayout, null);
        //final Intent intent = new Intent(MyMap.this, MyMap.class);
        // Strings to Show In Dialog with Radio Buttons
       final CharSequence[] items = {" Cricket "," Football "," Badminton ","Basketball"," Lawn Tennis ", "Volleyball"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MyMap.this);
        builder.setTitle("Please Select 2 Secondary Sports");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(items, new boolean[]{selectedSportsBoolean.get(0),selectedSportsBoolean.get(1),
                        selectedSportsBoolean.get(2),selectedSportsBoolean.get(3),
                        selectedSportsBoolean.get(4), selectedSportsBoolean.get(5)} ,
                new DialogInterface.OnMultiChoiceClickListener() {
                    // indexSelected contains the index of item (of which checkbox checked)
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {

//                        ((AlertDialog) dialog).getListView().getChildAt(getIndexFromStringName.get(selectedSport)).setEnabled(false);

                        if (isChecked) {

                            // If the user checked the item, add it to the selected items
                            // write your code when user checked the checkbox

                            //String a = String.valueOf(((AlertDialog)dialog).getListView().getChildAt(indexSelected));

                            //Toast.makeText(MyMap.this, a , Toast.LENGTH_LONG).show();
                            count++;
                            editor.putInt("count",count);
                            editor.commit();
                            //((AlertDialog) dialog).getListView().setEnabled(false);

                            if(count>2)
                            {
                                selectedSportsBoolean.put(indexSelected, false);
                                editor.putBoolean(String.valueOf(indexSelected), false);
                                editor.commit();
//                                sportSession.storeUserSecondarySportsSelection(String.valueOf(indexSelected),false);
                                Toast.makeText(MyMap.this, "You can only select 2 Sports", Toast.LENGTH_LONG).show();
                                count--;
                                editor.putInt("count", count);
                                editor.commit();
                                dialog.dismiss();
                                showSportChooserDialog(true);
                            }
                            else if(getStringNameFromIndex.get(indexSelected).equals(selectedSport))
                            {
                                selectedSportsBoolean.put(indexSelected, false);
//                                sportSession.storeUserSecondarySportsSelection(String.valueOf(indexSelected), false);
                                editor.putBoolean(String.valueOf(indexSelected), false);
                                Toast.makeText(MyMap.this, "Primary sport cannot be selected as secondary sport", Toast.LENGTH_LONG).show();
                                count--;
                                editor.putInt("count", count);
                                editor.commit();
                                dialog.dismiss();
                                showSportChooserDialog(true);
                            }
                            else
                            {
                                seletedItems.add(indexSelected);
                                selectedSportsBoolean.put(indexSelected, true);
//                                sportSession.storeUserSecondarySportsSelection(String.valueOf(indexSelected), true);
                                editor.putBoolean(String.valueOf(indexSelected), true);

                                editor.commit();

                            }
                        } else{
                            count--;
                            editor.putInt("count",count);
                            editor.commit();
                            // Else, if the item is already in the array, remove it
                            // write your code when user Unchecked the checkbox
                            seletedItems.remove(Integer.valueOf(indexSelected));
                            //selectedSportsBoolean.remove(Integer.valueOf(indexSelected));
                            selectedSportsBoolean.put(Integer.valueOf(indexSelected), false);
//                            sportSession.storeUserSecondarySportsSelection(String.valueOf(indexSelected), false);
                            editor.putBoolean(String.valueOf(indexSelected), false);

                            editor.commit();
//                            Toast.makeText(MyMap.this, "Unchecked", Toast.LENGTH_SHORT).show();
                            //selectedSportsBoolean.put(Integer.valueOf(indexSelected),false);
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        if (count < 2) {
                            showSportChooserDialog(true);
                        }

                        int j=0;
//                        String a = String.valueOf(((AlertDialog) dialog).getListView().getChildAt(0));
//
//                        Toast.makeText(MyMap.this, a , Toast.LENGTH_LONG).show();
                        for(int i=0; i<6; i++)
                        {
                            if(String.valueOf(selectedSportsBoolean.get(i)).equals("true"))
                            {
                                switch(i)
                                {
                                    case 0 : selectedSportsSecondary[j] = "Cricket";
                                            j++;
                                            break;
                                    case 1 : selectedSportsSecondary[j] = "Football";
                                        j++;
                                        break;
                                    case 2 : selectedSportsSecondary[j] = "Badminton";
                                        j++;
                                        break;
                                    case 3 : selectedSportsSecondary[j] = "Basketball";
                                        j++;
                                        break;
                                    case 4 : selectedSportsSecondary[j] = "Lawn Tennis";
                                        j++;
                                        break;
                                    case 5 : selectedSportsSecondary[j] = "Volleyball";
                                        j++;
                                        break;
                                }
                            }
                        }
                        //Toast.makeText(MyMap.this, selectedSportsSecondary[j-1] + selectedSportsSecondary[j-2] , Toast.LENGTH_LONG).show();
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here

                        insert_location();
//                        sportSession.setUserChoiceOfSports(true);
                        editor.putBoolean("isSportSet", true);

                        // commit changes
                        editor.commit();

                        Log.d("Sport Session", "User sport session modified!");

//                        Toast.makeText(MyMap.this," " + pref.getBoolean("isSportSet", true), Toast.LENGTH_LONG).show();

//                        sportSession.setSports(selectedSport, selectedSportsSecondary[0], selectedSportsSecondary[1]);
                        editor.putString("mainsport", selectedSport);
                        editor.putString("secondarysport", selectedSportsSecondary[0]);
                        editor.putString("tertiarysport", selectedSportsSecondary[1]);

                        // commit changes
                        editor.commit();

                        Log.d("Sport Session", "User sport session modified!");

                        if(isChanged==true) {
                            mMap.clear();
                            update_location(selectedSport,selectedSportsSecondary[0],selectedSportsSecondary[1],"Online");

                            fetch_location();
                            fetch_location();

                        }

//                        if(isSportChanged==false)
//                        {
//                            isSportChanged=true;
//                            selectedSport=selectedSportInDialog;
//                        }
                        dialog.dismiss();
                    }
                });
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        //  Your code when user clicked on Cancel
//                        dialog.dismiss();
//                    }
//                });


        //dialog = builder.create();//AlertDialog dialog; create like this outside onClick
        //dialog.show();

        subjectDialog = builder.create();
        subjectDialog.show();
        //This line has to go after your dialog.show(); call
        //CheckBox chkBox = (CheckBox)(AlertDialog)subjectDialog.getListView().;

    }

    private void showMainSportDialog(final boolean isChanged) {

//        if(isChanged==false)
//        {
//            checked=-1;
//            sportSession.storeUserSelection(checked);
//        }

//        checked = sportSession.getUserSelection();
        checked = pref.getInt("userSelection", -1);

        final CharSequence[] items = {" Cricket "," Football "," Badminton ","Basketball"," Lawn Tennis ", "Volleyball"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MyMap.this);
        builder.setTitle("Please Select your Main Sport");
        builder.setCancelable(false);

        builder.setSingleChoiceItems(items, checked, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {

                switch (item) {
                    case 0:
                        selectedSportInDialog = "Cricket";
                        checked=0;
//                        spinnerSport = selectedSportInDialog; //the value you want the position for
//
//                        myAdap = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
//
//                        spinnerPosition = myAdap.getPosition(spinnerSport);
//
//                        //set the default according to value
//                        spinner.setSelection(spinnerPosition);
                        break;
                    case 1:
                        checked=1;
                        selectedSportInDialog = "Football";
//                        spinnerSport = selectedSportInDialog; //the value you want the position for
//
//                        myAdap = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
//
//                        spinnerPosition = myAdap.getPosition(spinnerSport);
//
//                        //set the default according to value
//                        spinner.setSelection(spinnerPosition);
//                        //startActivity(intent);
                        // Your code when 2nd  option seletced

                        break;
                    case 2:
                        checked=2;
                        selectedSportInDialog = "Badminton";
//                        spinnerSport = selectedSportInDialog; //the value you want the position for
//
//                        myAdap = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
//
//                        spinnerPosition = myAdap.getPosition(spinnerSport);
//
//                        //set the default according to value
//                        spinner.setSelection(spinnerPosition);
                        //startActivity(intent);
                        // Your code when 3rd option seletced
                        break;
                    case 3:
                        checked=3;
                        selectedSportInDialog = "Basketball";
//                        spinnerSport = selectedSportInDialog; //the value you want the position for
//
//                        myAdap = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
//
//                        spinnerPosition = myAdap.getPosition(spinnerSport);
//
//                        //set the default according to value
//                        spinner.setSelection(spinnerPosition);
                        //startActivity(intent);
                        // Your code when 4th  option seletced
                        break;
                    case 4:
                        checked=4;
                        selectedSportInDialog = "Lawn Tennis";
//                        spinnerSport = selectedSportInDialog; //the value you want the position for
//
//                        myAdap = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
//
//                        spinnerPosition = myAdap.getPosition(spinnerSport);
//
//                        //set the default according to value
//                        spinner.setSelection(spinnerPosition);
                        //startActivity(intent);
                        // Your code when 4th  option seletced
                        break;
                    case 5:
                        checked=5;
                        selectedSportInDialog = "Volleyball";
//                        spinnerSport = selectedSportInDialog; //the value you want the position for
//
//                        myAdap = (ArrayAdapter) spinner.getAdapter(); //cast to an ArrayAdapter
//
//                        spinnerPosition = myAdap.getPosition(spinnerSport);
//
//                        //set the default according to value
//                        spinner.setSelection(spinnerPosition);
                        //startActivity(intent);
                        // Your code when 4th  option seletced
                        break;
                }
                subjectDialog.dismiss();
//                sportSession.storeUserSelection(checked);
                editor.putInt("userSelection", checked);

                editor.commit();
//                Toast.makeText(MyMap.this," " + pref.getBoolean("isSportSet", true), Toast.LENGTH_LONG).show();
                showSportChooserDialog(isChanged);
            }
        });


        subjectDialog = builder.create();
        subjectDialog.show();

    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"Ready to map!",Toast.LENGTH_LONG).show();

        //mListener = new LocationListener() {
            /*@Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MainActivity.this,"Location Changed: " + location.getLatitude() + ", " +
                        location.getLongitude(),Toast.LENGTH_SHORT).show();
                gotoLocation(location.getLatitude(),location.getLongitude(),15);
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, request, mListener
        );
        */

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();

//        db = new SQLiteHandler(getApplicationContext());
//        session = new SessionManager(getApplicationContext());
//
//        if (!session.isLoggedIn()) {
//            logoutUser();
//        }
//
//        // Fetching user details from sqlite
//        HashMap<String, String> user = db.getUserDetails();
//
//        String name = user.get("name");
//        String email = user.get("email");
//
//        delete_location(email);

        /*LocationServices.FusedLocationApi.removeLocationUpdates(
                mLocationClient,mListener
        );
        */
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item = parent.getItemAtPosition(position).toString();
        selectedSportAll=" ";

        if(position!=5)
        {
            if(isSportChanged==true) {
                mMap.clear();
                selectedSport=item;
                //update_location(item);
                //HashMap<String, String> user = db.getUserDetails();

                //String name = user.get("name");
                //String email = user.get("email");
                //Marker marker = updatedMarkerMap.get(email);
                //marker.remove();
                //marker.remove();
                //marker.remove();
                //Toast.makeText(MyMap.this,"Trying to remove marker",Toast.LENGTH_LONG).show();
                //mMap.clear();
                fetch_location();
                fetch_location();


            }
            if(isSportChanged==false)
            {
                isSportChanged=true;
                selectedSport=item;
            }


        }
        else
        {
            selectedSportAll = item;
            fetch_location();

        }



        // Showing selected spinner item
        //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Do the stuff that requires permission...
                View view = (View) findViewById(R.id.currentLocation);
                view.performClick();
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                // Should we show an explanation?

                if (ActivityCompat.shouldShowRequestPermissionRationale(MyMap.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //Show permission explanation dialog...
                    new AlertDialog.Builder(this)
                            .setTitle("Important")
                            .setMessage("Without the Location Permission You will not be able to " +
                                    "come to your current location")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface arg0, int arg1) {

                                }
                            }).create().show();
                } else{
                    //Never ask again selected, or device policy prohibits the app from having that permission.
                    //So, disable that feature, or fall back to another situation...
                }
            }

//            if (grantResults[1] == PackageManager.PERMISSION_DENIED)
//            {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(MyMap.this, Manifest.permission.READ_SMS)) {
//                    //Show permission explanation dialog...
//                    new AlertDialog.Builder(this)
//                            .setTitle("Important")
//                            .setMessage("Without the Read SMS Permission We will not be able to " +
//                                    "automatically detect the OTP")
//                            .setNegativeButton(android.R.string.no, null)
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//
//                                public void onClick(DialogInterface arg0, int arg1) {
//
//                                }
//                            }).create().show();
//                }
//            }
        }

//        switch(requestCode) {
//            case LOCATION_COARSE_REQUEST:
//                if (canAccessCoarseLocation()) {
//                    doCameraThing();
//                }
//                else {
//                    bzzzt();
//                }
//                break;
//
//            case CONTACTS_REQUEST:
//                if (canAccessContacts()) {
//                    doContactsThing();
//                }
//                else {
//                    bzzzt();
//                }
//                break;
//
//            case LOCATION_REQUEST:
//                if (canAccessLocation() && canAccessCoarseLocation()) {
//                    doLocationThing();
//                }
//                else {
//                    bzzzt();
//                }
//                break;
//        }
    }


//    private boolean canAccessLocation() {
//        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
//    }
//
//    private boolean canAccessCoarseLocation() {
//        return(hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
//    }
//
//    private boolean canAccessContacts() {
//        return(hasPermission(Manifest.permission.READ_CONTACTS));
//    }
//
//    private boolean hasPermission(String perm) {
//        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
//    }
//
//    private void bzzzt() {
//        Toast.makeText(this, "No access", Toast.LENGTH_LONG).show();
//    }
//
//    private void doCameraThing() {
//        Toast.makeText(this, "Camera granted", Toast.LENGTH_SHORT).show();
//    }
//
//    private void doContactsThing() {
//        Toast.makeText(this, "Contacts granted", Toast.LENGTH_SHORT).show();
//    }
//
//    private void doLocationThing() {
//        Toast.makeText(this, "Location granted", Toast.LENGTH_SHORT).show();
//    }

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


    @Override
    public void onBackPressed() {

        update_location("Offline");
        MyMap.super.onBackPressed();

//        new AlertDialog.Builder(this)
//                .setTitle("Really Exit?")
//                .setMessage("Are you sure you want to exit?")
//                .setNegativeButton(android.R.string.no, null)
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        update_location("Offline");
//                        MyMap.super.onBackPressed();
//                    }
//                }).create().show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}

