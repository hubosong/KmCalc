package com.machczew.kmcalc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.edtDistance) EditText edtDistance;
    @BindView(R.id.edtFrom) EditText edtFrom;
    @BindView(R.id.edtTo) EditText edtTo;
    @BindView(R.id.edtMedia) EditText edtMediaKm;
    @BindView(R.id.edtPreco) EditText edtPreco;

    @BindView(R.id.switchBack) Switch switchBack;

    @BindView(R.id.txtResValor) TextView txtResValor;
    @BindView(R.id.txtResConsumo) TextView txtResConsumo;
    @BindView(R.id.txtResDist) TextView txtResDist;
    @BindView(R.id.txtResTime) TextView txtResTime;

    @BindView(R.id.progWait) ProgressBar progWait;
    @BindView(R.id.txtWait) TextView txtWait;
    @BindView(R.id.llProgress) LinearLayout llWait;

    private DecimalFormat dec = new DecimalFormat("#.00");
    private String from, to;
    private int internet = 0;

    private ViewGroup mainLayout;
    private int xDelta;
    private int yDelta;

    //google maps
    private GoogleMap mMap;
    private MarkerOptions origin, destination;
    private String olat, olng, dlat, dlng;
    private String mapDistance, mapDuration, oCity, dCity, mapDistanceValue;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        overridePendingTransition(0, 0);

        startMap();

        findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(internet == 0){
                    edtDistance.setVisibility(View.VISIBLE);
                    edtFrom.setVisibility(View.GONE);
                    edtTo.setVisibility(View.GONE);
                    findViewById(R.id.llPrevisao).setVisibility(View.GONE);
                    internet++;
                } else {
                    edtDistance.setVisibility(View.GONE);
                    edtFrom.setVisibility(View.VISIBLE);
                    edtTo.setVisibility(View.VISIBLE);
                    findViewById(R.id.llPrevisao).setVisibility(View.VISIBLE);
                    internet = 0;
                }
            }
        });

        findViewById(R.id.img).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent("android.intent.action.MAIN");
                i.setComponent(new ComponentName("com.whatsapp", "com.whatsapp.Conversation"));
                i.setAction(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT,"");
                i.putExtra("jid", "555591993356" +"@s.whatsapp.net");
                i.setPackage("com.whatsapp");
                startActivity(i);

                return false;
            }
        });

        // animation up down layout
        mainLayout = findViewById(R.id.clMain);
        findViewById(R.id.lcMain).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams)v.getLayoutParams();
                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_UP:
                        //mainLayout.setBackgroundColor(Color.parseColor("#77000000"));
                        break;

                    case MotionEvent.ACTION_MOVE:
                        /*
                        // move all directions
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                        layoutParams.leftMargin = x - xDelta;
                        layoutParams.topMargin = y - yDelta;
                        layoutParams.rightMargin = xDelta - x;
                        layoutParams.bottomMargin = yDelta - y;
                        v.setLayoutParams(layoutParams);
                        break;

                         */

                        // move up and down
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                        layoutParams.topMargin = y - yDelta;
                        layoutParams.bottomMargin = yDelta - y;
                        v.setLayoutParams(layoutParams);
                        break;

                }
                mainLayout.invalidate();
                return true;
            }
        });



        // test internet
        ConnectivityManager manager =(ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                internet = 0;
            }
        } else{
            Toast.makeText(this, "Ops!\nHá algum problema na internet", Toast.LENGTH_LONG).show();
            edtDistance.setVisibility(View.VISIBLE);
            edtFrom.setVisibility(View.GONE);
            edtTo.setVisibility(View.GONE);
            findViewById(R.id.llPrevisao).setVisibility(View.GONE);

            internet++;
        }

        edtPreco.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    callButton();
                    return true;
                }
                return false;
            }
        });
        findViewById(R.id.btnCalculator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callButton();
            }
        });

    }

    public void callButton (){
        if(internet == 1){
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            calcularDistancia();
        } else {
            if(edtFrom.getText().toString().equals("") || edtTo.getText().toString().equals("")
                    || edtMediaKm.getText().toString().equals("") || edtPreco.getText().toString().equals("")){
                Toast.makeText(MainActivity.this, "Ops! Algum dado não inserido.", Toast.LENGTH_SHORT).show();
            } else {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                from = edtFrom.getText().toString();
                to = edtTo.getText().toString();
                //AsyncPage(from, to);
                new MainActivity.oData().execute(from);
                new MainActivity.dData().execute(to);

                //txtResDist.setText("291.000");
            }
        }
    }

    /*
    @SuppressLint("StaticFieldLeak")
    private void AsyncPage(final String from, final String to) {
        new AsyncTask<Void, Void, String>() {

            String dist = null;
            String time = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progWait.setVisibility(View.VISIBLE);
                txtWait.setVisibility(View.VISIBLE);
                llWait.animate().alpha(1f).setDuration(1000);
            }

            @SuppressLint("WrongThread")
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Document htmlDocument = Jsoup.connect("http://br.distanciacidades.net/calcular?from="+ from + "&to=" + to +"\"").get();

                    Elements distCond = htmlDocument.select("strong.driving");
                    Elements timeCond = htmlDocument.select("strong.time");

                    String distNormal = distCond.text().substring(0, distCond.text().indexOf(" "));
                    dist = distNormal.replace(",", "");
                    time = timeCond.text();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }


            @SuppressLint("SetTextI18n")
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                final float distance = Float.parseFloat(dist);
                final float mediaKm = Float.parseFloat(edtMediaKm.getText().toString());
                final float preco = Float.parseFloat(edtPreco.getText().toString());

                if(switchBack.isChecked()){
                    float resValor = ((distance * 2) / mediaKm) * preco;
                    float resConsumo = resValor / preco;
                    txtResValor.setText(dec.format(resValor));
                    txtResConsumo.setText(dec.format(resConsumo) + " l");
                    txtResDist.setText((distance * 2) + " km");
                    txtResTime.setText(time);
                } else {
                    float resValor = ((distance) / mediaKm) * preco;
                    float resConsumo = resValor / preco;
                    txtResValor.setText(dec.format(resValor));
                    txtResConsumo.setText(dec.format(resConsumo) + " l");
                    txtResDist.setText(dist + " km");
                    txtResTime.setText(time);
                }

                progWait.setVisibility(View.GONE);
                txtWait.setVisibility(View.GONE);
                llWait.animate().alpha(0).setDuration(300);

            }

        }.execute();

    }
    */


    /*calculo offline*/
    public void calcularDistancia(){
        final Float distance = Float.parseFloat(edtDistance.getText().toString());
        final Float mediaKm = Float.parseFloat(edtMediaKm.getText().toString());
        final Float preco = Float.parseFloat(edtPreco.getText().toString());

        if(switchBack.isChecked()){
            float resValor = ((distance * 2) / mediaKm) * preco;
            float resConsumo = resValor / preco;
            txtResValor.setText(dec.format(resValor));
            txtResConsumo.setText(dec.format(resConsumo) + " l");

            txtResDist.setText((distance * 2) + " km");
        } else {
            float resValor = ((distance) / mediaKm) * preco;
            float resConsumo = resValor / preco;
            txtResValor.setText(dec.format(resValor));
            txtResConsumo.setText(dec.format(resConsumo) + " l");

            txtResDist.setText(edtDistance.getText().toString() + " km");
        }
    }

    //google maps code
    public void startMap(){
        if(olat != null){
            origin = new MarkerOptions().position(new LatLng(Double.parseDouble(olat), Double.parseDouble(olng)))
                    .title(edtFrom.getText().toString().toUpperCase())
                    .snippet("Origem")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            destination = new MarkerOptions().position(new LatLng(Double.parseDouble(dlat), Double.parseDouble(dlng)))
                    .title(edtTo.getText().toString().toUpperCase())
                    .snippet("Destino")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            String url = getDirectionsUrl(origin.getPosition(), destination.getPosition());
            MainActivity.DownloadTask downloadTask = new MainActivity.DownloadTask();
            downloadTask.execute(url);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        } else{
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(olat != null){
            mMap.clear();
            mMap.addMarker(origin);
            mMap.addMarker(destination);

            //camera alway center of route
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(origin.getPosition());
            builder.include(destination.getPosition());
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-23.533773, -46.625290), 2));
        }
    }

    //calculo online
    public void calcular(){

        String distance = mapDistance.replaceAll("[^0-9]*", "");
        String duration = mapDuration
                .replaceAll("hours", "horas")
                .replaceAll("hour", "hora")
                .replaceAll("days", "dias")
                .replaceAll("day", "dia");

        final float mediaKm = Float.parseFloat(edtMediaKm.getText().toString());
        final float preco = Float.parseFloat(edtPreco.getText().toString());

        if(switchBack.isChecked()){

            float resValor = ((Integer.parseInt(distance) * 2) / mediaKm) * preco;
            float resConsumo = resValor / preco;
            txtResValor.setText(dec.format(resValor));
            txtResConsumo.setText(dec.format(resConsumo) + " l");
            txtResTime.setText(duration);
            int res = (Integer.parseInt(distance) * 2);
            txtResDist.setText( res + " km");


        } else {
            float resValor = ((Integer.parseInt(distance)) / mediaKm) * preco;
            float resConsumo = resValor / preco;

            txtResValor.setText(dec.format(resValor));
            txtResConsumo.setText(dec.format(resConsumo) + " l");

            txtResTime.setText(duration);
            txtResDist.setText(mapDistance);
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            MainActivity.ParserTask parserTask = new MainActivity.ParserTask();
            parserTask.execute(result);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @SuppressLint({"SetTextI18n", "WrongThread"})
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();
                routes = parser.parse(jObject);

                JSONObject jsonObject = new JSONObject(jsonData[0]).getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONArray("legs")
                        .getJSONObject(0);
                mapDistance = jsonObject.getJSONObject("distance").getString("text");
                mapDuration = jsonObject.getJSONObject("duration").getString("text");
                oCity = jsonObject.getString("start_address");
                dCity = jsonObject.getString("end_address");
                mapDistanceValue = jsonObject.getJSONObject("distance").getString("value");

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = new ArrayList();
            PolylineOptions lineOptions = new PolylineOptions();

            calcular();

            for (int i = 0; i < result.size(); i++) {
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.parseColor("#007fff"));
                lineOptions.geodesic(true);
            }
            if (points.size() != 0)
                mMap.addPolyline(lineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=google_maps_api_key";
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            //Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;

    }

    private class oData extends AsyncTask<String, Void, String> {
        StringBuilder result = null;
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + strings[0] + "&key=google_maps_api_key");
                urlConnection = (HttpURLConnection) url.openConnection();

                BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                result = new StringBuilder();
                String line;

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return (result != null) ? result.toString() : null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s).getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                olat = jsonObject.getString("lat");
                olng = jsonObject.getString("lng");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class dData extends AsyncTask<String, Void, String> {
        StringBuilder result = null;
        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?address=" + strings[0] + "&key=google_maps_api_key");
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();
                if (code !=  200) {
                    throw new IOException("Invalid response from server: " + code);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                result = new StringBuilder();
                String line;

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return (result != null) ? result.toString() : null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s).getJSONArray("results")
                        .getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                dlat = jsonObject.getString("lat");
                dlng = jsonObject.getString("lng");

                startMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finishAffinity();
    }
}
