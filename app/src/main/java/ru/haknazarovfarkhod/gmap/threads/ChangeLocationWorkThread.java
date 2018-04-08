package ru.haknazarovfarkhod.gmap.threads;

import android.os.Message;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import ru.haknazarovfarkhod.gmap.handlers.LocationChangeHandler;

public class ChangeLocationWorkThread implements Runnable {
    GoogleMap googleMap;
    LocationChangeHandler locationChangeHandler;

    public ChangeLocationWorkThread(LocationChangeHandler locationChangeHandler) {
        this.locationChangeHandler = locationChangeHandler;
    }

    @Override
    public void run() {
        Message message = this.locationChangeHandler.obtainMessage();
        this.locationChangeHandler.sendMessage(message);
    }

    private void goToLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        googleMap.moveCamera(cameraUpdate);
    }

    public void run(GoogleMap googleMap) {
        Message message = this.locationChangeHandler.obtainMessage();
        this.locationChangeHandler.sendMessage(message);
    }
}
