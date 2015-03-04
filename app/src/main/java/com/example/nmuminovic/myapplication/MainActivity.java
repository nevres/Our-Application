package com.example.nmuminovic.myapplication;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends ActionBarActivity implements LocationListener {
    GoogleMap googleMap;
    TextView test = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        //testing github
        String hello;

        setContentView(R.layout.activity_main);

        test = (TextView)findViewById(R.id.tvTest);

        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 20000, 0, (android.location.LocationListener) this);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(43.8541,18.4002)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));

        new MyAsyncTask().execute();
    }

    @Override

    public void onLocationChanged(Location location) {
        TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
         );
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    private class GetNearest extends AsyncTask<String, Integer, Double>
    {

        @Override
        protected Double doInBackground(String... params) {
            return null;
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Double> {

        StringBuffer buffer = null;
        String coordinates_json;

        double latitude_coordinate = 0;
        double longitude_coordinate = 0;

        protected Double doInBackground(String... params) {
            // TODO Auto-generated method stub
            getAllCoordinates();
            //need to create seperate class extending asynctask for postData, this function needs to be removed from here
            //postData();
            return null;
        }

        protected void onPostExecute(Double result){
            try{
                JSONArray jsonArray = new JSONArray(coordinates_json);

                for (int i=0; i < jsonArray.length(); i++)
                {
                    try {
                        JSONObject oneObject = jsonArray.getJSONObject(i);
                        latitude_coordinate = Double.valueOf(oneObject.getString("latitude"));
                        longitude_coordinate = Double.valueOf(oneObject.getString("longitude"));

                        LatLng latLng = new LatLng(latitude_coordinate, longitude_coordinate);
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(oneObject.getString("driver"))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                                //can add rating as snippet or distance
                                //.snippet(oneObject.getString("rating"))
                        );
                        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    } catch (JSONException e) {
                        // Oops
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        protected void onProgressUpdate(Integer... progress){
        }

        public void getAllCoordinates(){
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://taxi.net46.net/nevres/getlocations.php");
            BufferedReader in;

            try {
                HttpResponse response = httpclient.execute(httppost);
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                buffer = new StringBuffer();
                String line = "";
                while((line = in.readLine())!= null)
                    buffer.append(line);
                in.close();
                coordinates_json = buffer.toString();

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }

        }

        public void postData() {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://taxi.net46.net/nevres/getcoordinates.php");
            BufferedReader in;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("latitude", "27"));
                nameValuePairs.add(new BasicNameValuePair("longitude", "26!"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                buffer = new StringBuffer();
                String line = "";
                while((line = in.readLine())!= null)
                    buffer.append(line);
                in.close();
                // this should be implemented in postExecute method, this is why it is crashing if you uncomment
                // test.setText(buffer.toString());

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }

    }
}