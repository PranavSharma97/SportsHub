package com.example.admin.loginandregistration.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.R;
import com.example.admin.loginandregistration.helper.Place;
import com.example.admin.loginandregistration.helper.PlacesService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.util.HashMap;
import java.util.List;

public class CheckInActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String[] placeName;
    private String[] placeVicinity;
    private Double[] placeLat;
    private Double[] placeLng;
    private String[] imageUrl;

    private static final int ERROR_DIALOG_REQUEST = 9001;

    GoogleMap mMap;
    Marker marker;

    private HashMap<Marker, String> locationMarkerMap = new HashMap<>();
    private HashMap<Marker, String> vicinityMap = new HashMap<>();
    private HashMap<Marker, String> titleMap = new HashMap<>();

    private GoogleApiClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_check_in);

        if(servicesOK())
        {

            if(initMap())
            {

                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                mLocationClient.connect();

                mMap.setMyLocationEnabled(true);
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

        new GetPlaces(this).execute();
//        try {
//            findNearLocation();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    class GetPlaces extends AsyncTask<Void, Void, Void>{
        Context context;
        private ListView listView;
        private ProgressDialog bar;
        public GetPlaces(Context context) {
            // TODO Auto-generated constructor stub
            this.context = context;
            //this.listView = listView;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            bar.dismiss();
            try {
                addMarker(placeLat,placeLng,placeName,placeVicinity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //this.listView.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, placeName));

        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            bar =  new ProgressDialog(context);
            bar.setIndeterminate(true);
            bar.setTitle("Loading");
            bar.show();


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            try {
                findNearLocation();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

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
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);
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

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String msg = marker.getTitle() + " (" + marker.getPosition().latitude + ", "
                            + marker.getPosition().longitude
                            + ")";

                    Toast.makeText(CheckInActivity.this, msg, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        return(mMap!=null);
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


    public void findNearLocation() throws JSONException {

        PlacesService service = new PlacesService("AIzaSyBKrIKf7ESlz57ViD-FSitbSLfoRHwQ9Vo");

       /*
        Hear you should call the method find nearst place near to central park new delhi then we pass the lat and lang of central park. hear you can be pass you current location lat and lang.The third argument is used to set the specific place if you pass the atm the it will return the list of nearest atm list. if you want to get the every thing then you should be pass "" only
       */

/* hear you should be pass the you current location latitude and langitude, */
        List<Place> findPlaces = service.findPlaces(28.6303494, 77.0751921, "park");

        placeName = new String[findPlaces.size()];
        placeLat = new Double[findPlaces.size()];
        placeLng = new Double[findPlaces.size()];
        placeVicinity = new String[findPlaces.size()];

        imageUrl = new String[findPlaces.size()];

        for (int i = 0; i < findPlaces.size(); i++) {

            Place placeDetail = findPlaces.get(i);
            placeDetail.getIcon();

            double lat = placeDetail.getLatitude();
            double lng = placeDetail.getLongitude();
            String name = placeDetail.getName();


            //addMarker(lat,lng,name,placeDetail);

            //placeName[i] = placeDetail.getName() + " ," + String.valueOf(placeDetail.getLatitude())
            //+ " , " + String.valueOf(placeDetail.getLongitude());

            placeName[i] = placeDetail.getName();
            imageUrl[i] = placeDetail.getIcon();
            placeLat[i] = placeDetail.getLatitude();
            placeLng[i] = placeDetail.getLongitude();
            placeVicinity[i] = placeDetail.getVicinity();

        }


    }

    private void addMarker(final Double[] lat, final Double[] lng, String name[], String vicinity[]) throws JSONException {

        for (int i = 0; i < name.length; i++) {
            MarkerOptions options = new MarkerOptions()
                    .title("Park")
                    .position(new LatLng(lat[i], lng[i]))
                            //.icon(BitmapDescriptorFactory.defaultMarker())
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker))
                    //.snippet(vicinity[i])
                    .draggable(false);

            marker = mMap.addMarker(options);

            vicinityMap.put(marker,vicinity[i]);
            titleMap.put(marker,name[i]);

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
                    TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                    TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);
                    String vicinity= vicinityMap.get(marker);
                    String name = titleMap.get(marker);

                    //setImage(sport);

                    LatLng latLng = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    infoWindowImage.setImageResource(R.drawable.ic_parkicon);
                    tvLat.setText(name);
                    tvLng.setText(vicinity);
                    //tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });

        }
    }

}