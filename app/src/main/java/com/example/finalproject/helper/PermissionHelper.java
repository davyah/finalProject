package com.example.finalproject.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.finalproject.MainActivity;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.MODE_PRIVATE;
import static com.example.finalproject.MainActivity.PREFS_FILE_NAME;

public class PermissionHelper {
    public static final int MY_PERMISION_CODE = 10;
    public static void firstTimeAskingPermission(Context context, String permission, boolean isFirstTime) {
        SharedPreferences sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply();
    }


    public static boolean isFirstTimeAskingPermission(Context context, String permission) {
        return context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(permission, true);
    }

    public static boolean isPermissionsGranted(Context context) {
        return ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    public static void askPermissions(Context context, ProgressBar progress){
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                ACCESS_COARSE_LOCATION)) {
            showAlert(context);

        } else {

            if (isFirstTimeAskingPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                firstTimeAskingPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                        MY_PERMISION_CODE);
            } else {
                Toast.makeText(context, "You won't be able to access the features of this App", Toast.LENGTH_LONG).show();
                progress.setVisibility(View.GONE);
                //Permission disable by device policy or user denied permanently. Show proper error message
            }
        }


    }
    public static void showAlert(Context context) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings are OFF \nPlease Enable Location")
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                        ActivityCompat.requestPermissions((Activity)context,
                                new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},
                                MY_PERMISION_CODE);


                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }
}
