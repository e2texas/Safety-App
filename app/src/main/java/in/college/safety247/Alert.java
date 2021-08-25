package in.college.safety247;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.andexert.library.RippleView;

import java.util.ArrayList;
import java.util.List;



public class Alert extends android.support.v4.app.Fragment {

    View rootView;
    ContactsDatabaseAdapter mContactsDatabaseAdapter;
    List<Number> nums;

    private Double lat;
    private Double lang;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private ImageButton im;

    public Alert() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lang = location.getLongitude();
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

        };
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLocationManager.requestLocationUpdates("gps", 2000, 0, mLocationListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_alert, container, false);
        im = (ImageButton) rootView.findViewById(R.id.alertIM);

        nums = new ArrayList<Number>();
        mContactsDatabaseAdapter = new ContactsDatabaseAdapter(this.getContext());
        mContactsDatabaseAdapter = mContactsDatabaseAdapter.open();
        im.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendSMSMessage();
            }
        });
        return rootView;
    }

    private boolean isGPSEnabled() {
        LocationManager cm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return cm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    protected void openSmsSendingApplication(String message, String address) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , address);
        smsIntent.putExtra("sms_body"  , message);

        try {
            startActivity(smsIntent);
            Log.i("Finished sending SMS...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(),
                    "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void sendSMSMessage() {
        String phoneNos = "";
        int i = 0;
        nums = mContactsDatabaseAdapter.getNumDB();
        if (nums.size() != 0) {
            if (!isGPSEnabled()) {
                new AlertDialog.Builder(this.getContext())
                        .setMessage("Please activate your GPS Location!")
                        .setCancelable(false)
                        .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            if (lat == null || lang == null) {
                Toast.makeText(this.getContext(), "Problem Fetching Location. Please Try Again.", Toast.LENGTH_SHORT).show();
            }

            if (lat != null && lang != null) {

                String message = "Help Me My Location is : http://maps.google.com/maps?q=" +lat + "," +lang;
                while (i != nums.size()) {
                    phoneNos = phoneNos.concat(nums.get(i).getNumber().concat(";")) ;
                    i++;
                }
                openSmsSendingApplication(message,phoneNos);
            }
        } else {
            Toast.makeText(this.getContext(), "No added contacts!!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.alert_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pick_contact_item:

                Intent intent = new Intent(Alert.this.getActivity(), Alert_ADD.class);
                startActivity(intent);


                return true;

            case R.id.show_contact_item:


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
