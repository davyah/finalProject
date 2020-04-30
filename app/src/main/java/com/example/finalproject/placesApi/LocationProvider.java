package com.example.finalproject.placesApi;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Context context;
    private LocationProviderListener listener;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "LocationProvider";
    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }



    public LocationProvider(Context context) {
        this.context =context;

    }


    public void setLocationProviderListener(LocationProviderListener locationProviderListener){
        this.listener = locationProviderListener;
    }


    public void connectApiClient(){
        Log.w(TAG,"connecting client");
        mGoogleApiClient = new GoogleApiClient.Builder((Activity)context)
                .addConnectionCallbacks(LocationProvider.this)
                .addOnConnectionFailedListener(LocationProvider.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void getLastLocation(){
        Log.w(TAG,"getting location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((Activity) context);
        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener((Activity) context, location -> {

            if (location != null) {
                if(listener!=null)listener.onLocationRetrieved(location);
            }
            else {
                listener.onLocationError("Error retrieving location");
            }
        }).addOnFailureListener(e -> listener.onLocationError(e.getMessage()));
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("google api client","coonected");
            getLastLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if(listener!=null)listener.onLocationError(connectionResult.getErrorMessage());
    }
    public interface LocationProviderListener{
        void onLocationRetrieved(Location location);
        void onLocationError(String error_retrieving_location);
    }
}
