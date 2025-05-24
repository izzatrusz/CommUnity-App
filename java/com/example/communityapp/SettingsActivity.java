package com.example.communityapp;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class SettingsActivity extends AppCompatActivity {

    ImageButton backButton;
    private TextView txtWifiCheck;
    private Button btnWifi, btnBluetoothCheck, btnLocationCheck, btnInternetAvailablity, btnVibratePermission, btnAccelerometer;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //txtWifiCheck = findViewById(R.id.txtWifiCheck);
        btnWifi = findViewById(R.id.btnWifi);
        btnBluetoothCheck = findViewById(R.id.btnBluetoothCheck);
        btnLocationCheck = findViewById(R.id.btnLocationCheck);
        btnInternetAvailablity = findViewById(R.id.btnInternetAvailablity);
        btnVibratePermission = findViewById(R.id.btnVibratePermission);
        btnAccelerometer = findViewById(R.id.btnAccelerometer);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if device is connected to WiFi
                if (WifiManagerUtil.isConnectedToWifi(SettingsActivity.this)) {
                    showToast("Connected to WiFi");
                } else {
                    showToast("Not connected to WiFi");
                }
            }
        });

        // Check if Bluetooth Permission is Granted
        /*
        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH): Checks if the Bluetooth permission is granted.
        PackageManager.PERMISSION_GRANTED: Constant indicating that the permission is granted.*/
        btnBluetoothCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if Bluetooth Permission is Granted
                boolean isBluetoothPermissionGranted = ContextCompat.checkSelfPermission(SettingsActivity.this,
                        Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED;

                // Show Toast based on Permission Status
                if (isBluetoothPermissionGranted) {
                    Toast.makeText(SettingsActivity.this, "Bluetooth Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Check if Location Permission is Granted
        btnLocationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if Location Permission is Granted
                boolean isLocationPermissionGranted = ContextCompat.checkSelfPermission(SettingsActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                // Show Toast based on Permission Status
                if (isLocationPermissionGranted) {
                    Toast.makeText(SettingsActivity.this, "Location Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Internet Availablity
        btnInternetAvailablity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    Toast.makeText(SettingsActivity.this, "Internet Connection Available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //
        btnVibratePermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) {
                    Toast.makeText(SettingsActivity.this, "Vibrator Available", Toast.LENGTH_SHORT).show();
                    // Vibrate for 500 milliseconds
                    vibrator.vibrate(500);
                } else {
                    Toast.makeText(SettingsActivity.this, "Vibrator Not Available", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // check Accelerometer
        btnAccelerometer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the SensorManager service
                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                // Check if the accelerometer is available
                if (sensorManager != null) {
                    Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                    if (accelerometer != null) {
                        Toast.makeText(SettingsActivity.this, "TYPE_GYROSCOPE Available", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "TYPE_GYROSCOPE Not Available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
//   closed
    }


    // Method to show toast message
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    // method for network avilability
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
