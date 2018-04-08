package ru.haknazarovfarkhod.gmap;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.haknazarovfarkhod.gmap.handlers.LocationChangeHandler;
import ru.haknazarovfarkhod.gmap.services.AppLocationListener;
import ru.haknazarovfarkhod.gmap.threads.ChangeLocationWorkThread;

;

public class MainActivity extends FragmentActivity {
    private static final int GPS_ERROR_DIALOG = 9001;
    private MainActivity mainActivity;
    private GoogleMap googleMap;
    private LocationListener locationListener;
    public final Context mainContext = this;
    private static final int POLYGON_POINTS = 5;
    List<Marker> markers = new ArrayList<>();
    private Polygon shape;
    @BindView(R.id.searchButton)
    Button searchButton;

    @BindView(R.id.searchEditText)
    TextView searchEditText;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MapStyleOptions style;
        int id = item.getItemId();
        switch (id) {
            case R.id.mapTypeStandard:
                style = null;
                break;
            case R.id.mapTypeSilver:
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_silver);
                break;
            case R.id.mapTypeRetro:
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_retro);
                break;
            case R.id.mapTypeAubergine:
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_aubergine);
                break;
            case R.id.mapTypeDark:
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_dark);
                break;
            case R.id.mapTypeNight:
                style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
                break;
            case R.id.license:
                displayGooglePlayLicense();
                return false;
            default:
                return false;
        }

        googleMap.setMapStyle(style);
        return super.onOptionsItemSelected(item);
    }

    private void displayGooglePlayLicense() {
        Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;

        if (serviceOK()) {

            Toast.makeText(this, "Connected to Google Service!", Toast.LENGTH_SHORT).show();

            setContentView(R.layout.activity_map);

            if (initMap()) {
                Toast.makeText(this, "Map loaded!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Can't connected to Google Map Service!", Toast.LENGTH_SHORT).show();
            }
        } else {
            setContentView(R.layout.activity_main);
        }
        ButterKnife.bind(this);
    }

    private void drawPolygon(){
        PolygonOptions options = new PolygonOptions()
                .fillColor(0X330000FF)
                .strokeWidth(3)
                .strokeColor(Color.BLUE);

        for (int i = 0; i<POLYGON_POINTS; i++){
            options.add(markers.get(i).getPosition());
        }

        shape = googleMap.addPolygon(options);
    }

    public boolean serviceOK() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int isAvailable = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApiAvailability.isUserResolvableError(isAvailable)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, isAvailable, GPS_ERROR_DIALOG);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to Google Play Service", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    private void addMarker(Address address){
        if(markers.size()==POLYGON_POINTS){
            removeEverything();
        }

        MarkerOptions marker = new MarkerOptions()
                .title(address.getLocality())
                .draggable(true)
                .snippet(address.getCountryName())
                .position(new LatLng(address.getLatitude(), address.getLongitude()));

        String country = address.getCountryName();
        if(country.length() > 0){
            marker.snippet(country);
        }
        markers.add(googleMap.addMarker(marker));

        if(markers.size()==POLYGON_POINTS){
            drawPolygon();
        }
    }

    private void removeEverything() {
        for (Marker marker:markers){
            marker.remove();
        }
        markers.clear();

        if(shape!=null){
            shape.remove();
            shape = null;
        }
    }

    private boolean initMap() {
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView));

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(final GoogleMap map) {
                    googleMap = map;
                    googleMap.setMyLocationEnabled(true);
                    locationListener = new AppLocationListener((MainActivity) mainContext, googleMap);

                    googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(LatLng latLng) {
                            Geocoder gc = new Geocoder(MainActivity.this);
                            List<Address> list = null;
                            try {
                                list = gc.getFromLocation(latLng.latitude,latLng.longitude, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }

                            Address add = list.get(0);
                            MainActivity.this.addMarker(add);
                        }
                    });

                    googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(Marker marker) {
                            //Unused method
                        }

                        @Override
                        public void onMarkerDrag(Marker marker) {
                            //Unused method
                        }

                        @Override
                        public void onMarkerDragEnd(Marker marker) {
                            Geocoder gc = new Geocoder(MainActivity.this);
                            List<Address> list = null;
                            LatLng latLng = marker.getPosition();

                            try {
                                list = gc.getFromLocation(latLng.latitude,latLng.longitude, 1);
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

                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            View v = getLayoutInflater().inflate(R.layout.info_window, null);
                            TextView tvLocality = v.findViewById(R.id.tvLocality);
                            TextView tvLat = v.findViewById(R.id.tvLat);
                            TextView tvLng = v.findViewById(R.id.tvLng);
                            TextView tvSnippet = v.findViewById(R.id.tvSnippet);

                            LatLng latLng = marker.getPosition();
                            tvLocality.setText(marker.getTitle());
                            tvLat.setText("Latitude: " + latLng.latitude);
                            tvLng.setText("Longitude: " + latLng.longitude);
                            tvSnippet.setText(marker.getSnippet());

                            return v;
                        }
                    });
                }
            });
        }
        return true;
    }

    @OnClick(R.id.searchButton)
    public void OnClick() {
        String searchText = (String) searchEditText.getText().toString();
        if (!searchText.equals("")) {

            LocationChangeHandler locationChangeHandler = new LocationChangeHandler();
            locationChangeHandler.setParentActivity(this);
            locationChangeHandler.setGoogleMap(googleMap);
            locationChangeHandler.setSearchText(searchText);

            ChangeLocationWorkThread workThread = new ChangeLocationWorkThread(locationChangeHandler);
            Thread thread = new Thread(workThread);
            thread.start();
        }
    }
}
