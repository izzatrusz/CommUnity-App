package com.example.communityapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;

public class WifiManagerUtil {
    // Method to check if the device is connected to the internet via WiFi
    public static boolean isConnectedToWifi(Context context) {
        /*This retrieves the ConnectivityManager system service using the provided Context.
        It casts the Context object to a ConnectivityManager and assigns it to the variable connectivityManager.
        This service allows the app to query information about the state of network connectivity.*/
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        /*This retrieves information about the Wi-Fi network.
        It calls the getNetworkInfo() method on the connectivityManager object,
        passing ConnectivityManager.TYPE_WIFI as an argument to specify that we want information about the Wi-Fi network specifically.
        The result is assigned to the variable wifiNetwork, which is of type NetworkInfo.*/
        NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();


        /*This returns a boolean value based on whether the wifiNetwork object is not null and is connected.
        It first checks if wifiNetwork is not null (i.e., if it was successfully obtained), and then it checks if it is connected.
        If both conditions are true, it returns true, indicating that the device is connected to a Wi-Fi network.
        Otherwise, it returns false.*/
        return wifiNetwork != null && wifiNetwork.isConnected();
        //return wifiNetwork != null && networkInfo.isAvailable();
    }
}
