package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.example.finalproject.adapter.HomepageAdapter;
import com.example.finalproject.helper.PermissionHelper;
import com.example.finalproject.model.User;
import com.example.finalproject.placesApi.APIInterface;
import com.example.finalproject.placesApi.AddressProvider;
import com.example.finalproject.placesApi.ApiClient;
import com.example.finalproject.placesApi.LocationProvider;
import com.example.finalproject.placesApi.Place;
import com.example.finalproject.placesApi.PlaceProvider;
import com.example.finalproject.placesApi.PlacesResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.example.finalproject.helper.PermissionHelper.MY_PERMISION_CODE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //declaration of all assets
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "MainActivity";
    FirebaseDatabase database;
    DatabaseReference myRef;
    private FirebaseUser firebaseUser;
    private String uid;
    private HomepageAdapter homepageAdapter;
    APIInterface apiService;
    public String latLngString;
    public double source_lat, source_long;


    public static final String PREFS_FILE_NAME = "sharedPreferences";
    ArrayList<PlacesResponse.CustomA> results;

    protected Location mLastLocation;

    ProgressBar progress;


    public List<Place> restaurantsList;


    private LocationProvider locationProvider;
    private long radius = 3 * 1000;



    private boolean Permission_is_granted = false;
    public String mAddressOutput;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        apiService = ApiClient.getClient().create(APIInterface.class);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        //Setting the actionbarToggle to drawer layout
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        locationProvider = new LocationProvider(MainActivity.this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        progress = (ProgressBar) findViewById(R.id.progress);


        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(0, true);
        } else progress.setProgress(0);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
            Log.w(TAG, uid);
            myRef = database.getReference();
            myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
                        startActivity(new Intent(MainActivity.this, CreateProfileActivity.class));
                        finish();
                    } else {
                        loadData();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, databaseError.getMessage());
                }
            });
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.w("ca", "d");
        //return true for ActionBarToggle to handle the touch event
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadData() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            showSnack(true);
        } else {
            progress.setVisibility(View.GONE);
            showSnack(false);
        }
    }


    private void fetchStores(String placeType) {
        restaurantsList = Collections.synchronizedList(new ArrayList<Place>());
        PlaceProvider placeProvider = new PlaceProvider(placeType, latLngString, radius);
        placeProvider.setPlaceProviderListener(new PlaceProvider.PlaceProviderListener() {
            @Override
            public void onError(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onPlaceFetched(Place place) {
                addToList(place);
            }
        });
    }


    private void addToList(Place place) {
        restaurantsList.add(place);

        //Log.i("details : ", info.name + "  " + address);


        Collections.sort(restaurantsList, new Comparator<Place>() {
            @Override
            public int compare(Place lhs, Place rhs) {
                return lhs.distance.compareTo(rhs.distance);
            }
        });
        Log.w("add", place.address);
        progress.setVisibility(View.GONE);
        Log.w("places", restaurantsList.toString());
        HomepageAdapter adapterStores = new HomepageAdapter(getApplicationContext(), restaurantsList, mAddressOutput);
        adapterStores.setOnClickListener(new HomepageAdapter.onClickListener() {
            @Override
            public void OnClick(int position) {
                startActivity(new Intent(MainActivity.this, RestaurantDetailsActivity.class));
            }
        });
        recyclerView.setAdapter(adapterStores);


    }





    private void getUserLocation() {

        if (!PermissionHelper.isPermissionsGranted(MainActivity.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionHelper.askPermissions(MainActivity.this,progress);
            }
            else Permission_is_granted = true;
        } else {

            locationProvider.connectApiClient();
            locationProvider.setLocationProviderListener(new LocationProvider.LocationProviderListener() {
                @Override
                public void onLocationRetrieved(Location location) {
                    mLastLocation = location;
                    source_lat = location.getLatitude();
                    source_long = location.getLongitude();
                    latLngString = location.getLatitude() + "," + location.getLongitude();
                     new AddressProvider(latLngString, new AddressProvider.AddressListener() {
                        @Override
                        public void onAddressFetched(String message) {
                            mAddressOutput = message;
                        }
                    });
                    fetchStores("restaurant");
                }

                @Override
                public void onLocationError(String error_retrieving_location) {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), error_retrieving_location, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("On request permiss", "executed");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Permission_is_granted = true;
                    getUserLocation();
                } else {
                   PermissionHelper.askPermissions(MainActivity.this,progress);
                    Permission_is_granted = false;
                    Toast.makeText(getApplicationContext(), "Please switch on GPS to access the features", Toast.LENGTH_LONG).show();
                    progress.setVisibility(View.GONE);

                }
                break;
        }
    }

    @Override
    protected void onStart() {
        Log.i("on start", "true");
        super.onStart();
        if (locationProvider.getmGoogleApiClient() != null) {
            //locationProvider.getmGoogleApiClient().connect();
        }
    }


    @Override
    protected void onResume() {

        Log.i("on resume", "true");
        super.onResume();
        if (Permission_is_granted) {
            if (locationProvider.getmGoogleApiClient().isConnected()) {
                //getUserLocation();
            }
        }
    }




    public void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Good! Connected to Internet";
            color = Color.WHITE;
            getUserLocation();
        } else {
            message = "Sorry! Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
//        TextView textView = (TextView) sbView.findViewById(a);
//        textView.setTextColor(color);
        snackbar.show();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_profile: {
                break;
            }
            case R.id.reviews: {
                break;
            }
        }
        menuItem.setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }
}
