package com.jofiagtech.testlocation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final long LOCATION_FREQUENCE = 5000; //5 seconds
    private static final long LOCATION_FASTEST_FREQUENCE = 5000; //5 seconds
    private static final int ALL_PERMISSION_RESULT = 1;
    private GoogleApiClient mClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<String> mPermissionsToRequest;
    private ArrayList<String> mPermissions = new ArrayList<>();
    private ArrayList<String> mPermissionsRejected = new ArrayList<>();
    private TextView locationText;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationText = findViewById(R.id.location_text);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        //Permissions needed to request location
        mPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        mPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        mPermissionsToRequest = addPermissionsNotRequestedYet(mPermissions);

        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private ArrayList<String> addPermissionsNotRequestedYet(ArrayList<String> permissionsNeeded) {
        ArrayList<String> result = new ArrayList<>();

        for (String permission : permissionsNeeded){
            if (!isGrantedPermission(permission))
                result.add(permission);
        }

        return result;
    }

    private boolean isGrantedPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //Allow lower SDK support
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;

        return true;
    }

    private void checkLocationServices(){
        int errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorCode, errorCode,
                            new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Toast.makeText(MainActivity.this, "No services",
                                            Toast.LENGTH_LONG).show();

                                    finish();
                                }
                            });

            errorDialog.show();
        }
        else
            Toast.makeText(MainActivity.this, "Services are good !",
                    Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mClient != null)
            mClient.connect();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        checkLocationServices();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mClient != null && mClient.isConnected()){
            LocationServices.getFusedLocationProviderClient(this)
                    .removeLocationUpdates(new LocationCallback());

            mClient.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mClient != null && mClient.isConnected())
            mClient.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override //It's here we get the location if the  permissions are granted.
    public void onConnected(@Nullable Bundle bundle) {
        //Check if permission is granted before access to location
        String fineLocationAccess = Manifest.permission.ACCESS_FINE_LOCATION;
        String coarseLocationAccess = Manifest.permission.ACCESS_COARSE_LOCATION;
        int accessGranted = PackageManager.PERMISSION_GRANTED;

        if (ActivityCompat.checkSelfPermission(this, fineLocationAccess) != accessGranted &&
            ActivityCompat.checkSelfPermission(this, coarseLocationAccess) != accessGranted){
            return;
        }

        mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        //Get the last location if nonnull
                        setLocationTextView(location);
                    }
                });

        //Track the location / get the location instantly / get the location whenever it changes
        //Here we get the location every 5 seconds
        startLocationUpdates();

    }

    private void setLocationTextView(Location location){
        if (location != null)
            locationText.setText(String.format("Lat:%sLon: %s", location.getLatitude(), location.getLongitude()));
    }

    //Track the location / get the location instantly / get the location whenever it changes
    private void startLocationUpdates() {
        String fineLocationAccess = Manifest.permission.ACCESS_FINE_LOCATION;
        String coarseLocationAccess = Manifest.permission.ACCESS_COARSE_LOCATION;
        int accessGranted = PackageManager.PERMISSION_GRANTED;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_FREQUENCE);
        mLocationRequest.setFastestInterval(LOCATION_FASTEST_FREQUENCE);

        boolean locationAccessNotGranted =
                ActivityCompat.checkSelfPermission(this, fineLocationAccess) != accessGranted &&
                ActivityCompat.checkSelfPermission(this, coarseLocationAccess) != accessGranted;

        boolean locationAccessGranted =
                ActivityCompat.checkSelfPermission(this, fineLocationAccess) == accessGranted &&
                ActivityCompat.checkSelfPermission(this, coarseLocationAccess) == accessGranted;

        if (locationAccessNotGranted){
            Toast.makeText(MainActivity.this, "You need to admit permission to display the location",
                    Toast.LENGTH_LONG).show();
        }
        else if (locationAccessGranted) {
            LocationServices.getFusedLocationProviderClient(MainActivity.this)
                    .requestLocationUpdates(mLocationRequest, new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);

                            if (locationResult != null){
                                Location location = locationResult.getLastLocation();
                                setLocationTextView(location);
                            }
                        }

                        @Override
                        public void onLocationAvailability(LocationAvailability locationAvailability) {
                            super.onLocationAvailability(locationAvailability);
                        }
                    }, null);
        }
    }

    //Get the access that are not granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        final int size = mPermissionsRejected.size();

        switch (requestCode){
            case ALL_PERMISSION_RESULT:
                for (String permission : mPermissionsToRequest){
                    if (!isGrantedPermission(permission))
                        mPermissionsRejected.add(permission);
                }

                //If permission is not granted then show in a dialog why the permission is needed. And request it.
                if (size > 0){
                    if (shouldShowRequestPermissionRationale(mPermissionsRejected.get(0))){
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("This permission is mandatory to get location")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            requestPermissions(mPermissionsRejected.toArray(new String[size]),
                                                ALL_PERMISSION_RESULT);
                                    }
                                })
                                .setNegativeButton("cancel", null)
                                .create()
                                .dismiss();
                    }
                }
                //If location permissions are granted, then connect the client.
                //When the client is connected, the function onConnected() is called.
                else{
                    if (mClient != null)
                        mClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
