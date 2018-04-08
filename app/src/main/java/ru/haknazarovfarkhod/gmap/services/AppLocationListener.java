package ru.haknazarovfarkhod.gmap.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import ru.haknazarovfarkhod.gmap.MainActivity;
import ru.haknazarovfarkhod.gmap.handlers.LocationChangeHandler;
import ru.haknazarovfarkhod.gmap.threads.ChangeLocationWorkThread;

public class AppLocationListener implements LocationListener {
    static Location imHere;
    private static Context parentActivity;
    private static GoogleMap googleMap;

    @Override
    public void onLocationChanged(Location location) {
        imHere = location;

        Toast.makeText(parentActivity, "Coordinates: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();

        LocationChangeHandler locationChangeHandler = new LocationChangeHandler();
        locationChangeHandler.setParentActivity((MainActivity) parentActivity);
        locationChangeHandler.setGoogleMap(googleMap);
        locationChangeHandler.setLocationLat(location.getLatitude());
        locationChangeHandler.setLocationLng(location.getLongitude());
        ChangeLocationWorkThread workThread = new ChangeLocationWorkThread(locationChangeHandler);
        workThread.run(googleMap);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    public AppLocationListener() {
    }

    public AppLocationListener(MainActivity mainActivity, GoogleMap Map) {
        parentActivity = mainActivity;
        googleMap = Map;
        setUpLocationListener(parentActivity, googleMap);
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @SuppressLint("MissingPermission")
    public void setUpLocationListener(Context context, GoogleMap Map) {
        googleMap = Map;
        parentActivity = context;

        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new AppLocationListener();

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                10,
                locationListener);

        imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }
}
