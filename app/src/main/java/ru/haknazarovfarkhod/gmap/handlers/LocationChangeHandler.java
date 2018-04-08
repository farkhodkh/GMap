package ru.haknazarovfarkhod.gmap.handlers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import ru.haknazarovfarkhod.gmap.MainActivity;
import ru.haknazarovfarkhod.gmap.R;
import ru.haknazarovfarkhod.gmap.services.ChangeCameraLocation;

import static android.content.Context.LOCATION_SERVICE;

public class LocationChangeHandler extends Handler {
    private int access_fine_location_request_code = 1000;
    private String searchText = "";

    private double LocationLat = 59.938858;

    private double LocationLng = 30.315761;
    private GoogleMap googleMap;
    private static Marker markerOnMap;

    MainActivity parentActivity;
    ChangeCameraLocation changeCameraLocation = new ChangeCameraLocation();
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Bundle bundle = msg.getData();
        if (bundle.containsKey("googleMap")) {
            googleMap = (GoogleMap) bundle.get("googleMap");
        }
        if (googleMap != null) {
            changeCameraLocation.setGoogleMap(googleMap);

            if (!searchText.equals("")) {
                Address address = getFirstAddresByLocationName(searchText);
                if(address!=null) {
                    changeCameraLocation.changeCameraLocation(address.getLatitude(), address.getLongitude(), 15);
                    if(markerOnMap!=null){
                        markerOnMap.remove();
                    }
                    MarkerOptions marker = new MarkerOptions()
                            .title(address.getLocality())
                            .position(new LatLng(address.getLatitude(), address.getLongitude()))
                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_icon));
                    String country = address.getCountryName();
                    if(country.length()>0){
                        marker.snippet(country);
                    }

                    markerOnMap = googleMap.addMarker(marker);
                }
            }else {
                changeCameraLocation.changeCameraLocation(LocationLat, LocationLng, 15);
            }
        }
    }

    private Address getFirstAddresByLocationName(String locationName){
        Address address = null;

        Geocoder gc = new Geocoder(parentActivity);
        List<Address> list = null;
        try {
            list = gc.getFromLocationName(locationName, 10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list!=null&&list.size() > 0) {
            address = list.get(0);
            searchText = "";
        }

        return address;
    }

    public MainActivity getParentActivity() {
        return parentActivity;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public double getLocationLat() {
        return LocationLat;
    }

    public void setLocationLat(double locationLat) {
        LocationLat = locationLat;
    }

    public double getLocationLng() {
        return LocationLng;
    }

    public void setLocationLng(double locationLng) {
        LocationLng = locationLng;
    }

    public void setParentActivity(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Location getLastKnownLocation() {

        int permissionCheckFineLocation = parentActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheckFineLocation != PackageManager.PERMISSION_GRANTED) {
            parentActivity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, access_fine_location_request_code);
        }

        LocationManager mLocationManager = (LocationManager) parentActivity.getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
