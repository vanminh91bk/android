package minh.thuan.alarmtrafficjam.location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import minh.thuan.alarmtrafficjam.R;
import minh.thuan.alarmtrafficjam.ulities.GMessage;


/**
 * Created by vanmi on 10/3/2015.
 */
public class MyLocation implements LocationListener {

    private final String TAG = "MyLocation";
    private Context context;
    private LocationManager locationManager;
    private LatLng lastLocation;
    private boolean isEnableLocation = false;
    private ILocationChange mILocationChange;

    private final long MIN_DISTANCE_UPDATE = 1;
    private final long MIN_TIME_UPDATE = 1000 * 60 * 1;


    public MyLocation(Context context, ILocationChange mILocationChange) {
        GMessage.showMessage(context, TAG, "MyLocation");
        this.context = context;
        this.mILocationChange = mILocationChange;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
    }


    public void requestUpdateLocation() {
        GMessage.showMessage(context, TAG, "requestUpdateLocation");
        boolean isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnable) {
            isEnableLocation = true;
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);
            GMessage.showMessage(context, TAG, "Network enable");
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location!=null) {
                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mILocationChange.locationChange(lastLocation);
            }
        }

        boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnable) {
            isEnableLocation = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATE, MIN_DISTANCE_UPDATE, this);
            GMessage.showMessage(context, TAG, "GPS enable");
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location!=null) {
                lastLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mILocationChange.locationChange(lastLocation);
            }
        }


        if ((!isGPSEnable || !isNetworkEnable)) {
            isEnableLocation = false;
            showSettingAlert();
        }
    }

    public boolean isCanGetLocation(){
        return this.isEnableLocation;
    }

    public LatLng getLastLatLng()
    {
        if(lastLocation!=null)
        {
            return lastLocation;
        }
        return null;
    }

    private void showSettingAlert() {
        GMessage.showMessage(context, TAG, "showSettingAlert");
        new AlertDialog.Builder(context)
                .setTitle("Use location?")
                .setMessage("Location is not enabled. Do you want to go to setting?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setIcon(R.drawable.ic_location)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        GMessage.showMessage(context, TAG, "onLocationChanged");
        lastLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mILocationChange.locationChange(lastLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        GMessage.showMessage(context, TAG, "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        isEnableLocation = true;
        GMessage.showMessage(context, TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        isEnableLocation = false;
        GMessage.showMessage(context, TAG, "onProviderDisabled");
    }

}


