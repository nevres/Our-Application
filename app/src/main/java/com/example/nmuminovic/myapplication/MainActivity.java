package com.example.nmuminovic.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.Marker;
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


public class MainActivity extends ActionBarActivity implements LocationListener, View.OnClickListener {
    GoogleMap googleMap;
    TextView test = null;
    Button callTaxi;
    PendingIntent pendingIntent;
    HttpClient httpclient;
    MyAsyncTask asyncTask;
    GetNearest getNearest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        httpclient = new DefaultHttpClient();


        //testing github and again
        //this is line added from branch nevres
        //commit branch nevres second
        //commit third nevres branch
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

        callTaxi = (Button) findViewById(R.id.callTaxi);
        callTaxi.setOnClickListener(this);

        asyncTask =  new MyAsyncTask();
        asyncTask.execute();
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
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
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
        public void printJSON(String coordinates){
            JSONArray jsonArray = null;
            try{
                jsonArray = new JSONArray(coordinates);

                for (int i=0; i < jsonArray.length(); i++)
                {
                    try {
                        JSONObject oneObject = jsonArray.getJSONObject(i);

                        LatLng latLng = new LatLng(Double.valueOf(oneObject.getString("latitude")), Double.valueOf(oneObject.getString("longitude")));
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(oneObject.getString("driver"))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                        );
                        //marker.remove();

                    } catch (JSONException e) {
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(jsonArray.length() == 1)
                try {
                    LatLng latLng = new LatLng(Double.valueOf(jsonArray.getJSONObject(0).getString("latitude")), Double.valueOf(jsonArray.getJSONObject(0).getString("longitude")));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

        }

    @Override
    public void onClick(View v) {
         /* while(true){
            try {
                Thread.sleep(10000);
                new MyAsyncTask().execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }*/
        if(v.getId()== R.id.callTaxi){
            if(callTaxi.getText() == "POZOVI TAXI") {

                asyncTask.cancel(true);
                getNearest = new GetNearest();
                getNearest.execute();
                callTaxi.setText("OTKAZI TAXI");

            }else if(callTaxi.getText()=="OTKAZI TAXI"){
                getNearest.cancel(true);
                asyncTask = new MyAsyncTask();
                asyncTask.execute();
                callTaxi.setText("POZOVI TAXI");
            }
        }
    }


    private class GetNearest extends AsyncTask<String, Integer, Double>
    {

        StringBuffer buffer = null;
        String neares_coordinates;

        @Override
        protected Double doInBackground(String... params)
        {
            Log.d("GN.doback","GN.doback");
            getNearestTaxi();
            return null;
        }

        protected void onPostExecute(Double result){
            googleMap.clear();
            MainActivity.this.printJSON(neares_coordinates);
            Log.d("GN.onpost","GN.onpost");
        }

        private void getNearestTaxi(){

            HttpPost httppost = new HttpPost("http://taxi.net46.net/nevres/php_googlemaps.php");
            BufferedReader in;

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("lat", "43"));
                nameValuePairs.add(new BasicNameValuePair("lng", "18"));
                nameValuePairs.add(new BasicNameValuePair("radius","100"));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                buffer = new StringBuffer();
                String line = "";
                while((line = in.readLine())!= null)
                    buffer.append(line);
                in.close();
                neares_coordinates = buffer.toString();

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
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
            googleMap.clear();
            MainActivity.this.printJSON(coordinates_json);
        }

        protected void onProgressUpdate(Integer... progress){
        }

        public void getAllCoordinates(){
            // Create a new HttpClient and Post Header

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