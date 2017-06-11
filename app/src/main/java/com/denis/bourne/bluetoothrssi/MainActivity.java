package com.denis.bourne.bluetoothrssi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;*/

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter btAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private static final int REQUEST_ENABLE_BT = 1;
    private Button btnScan;
    private ListView listViewLE;
    private List<BluetoothDevice> listBluetoothDevice;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000;

    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;

    /*private static final String REGISTER_URL = "http://localhost/blue-rssi-upload.php";
    private static final String KEY_SSID = "ssid";
    private static final String KEY_RSSI = "rssi";
    private static final String KEY_DISTANCE = "distance";

    private String name;
    private String rssi;
    private String distance;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get permissions to access the location
        requestPermission();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        getBluetoothAdapterAndLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "No Bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnScan = (Button) findViewById(R.id.start);
        btnScan.setOnClickListener(v -> {
            scanning = true;
            scanLeDevice(scanning);
        });

        listViewLE = (ListView) findViewById(R.id.listView1);

        listBluetoothDevice = new ArrayList<>();
        ListAdapter adapterLeScanResult = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        listViewLE.setAdapter(adapterLeScanResult);
    }

    private void getBluetoothAdapterAndLeScanner() {

        // Get BluetoothAdapter and BluetoothLeScanner.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = btAdapter.getBluetoothLeScanner();
    }

    /**
     * Method to allow user to access a single access point
     *
     * @param device Bluetooth Device name
     * @return boolean
     */
    public boolean getBluetoothDevice(String device) {

        return device.equalsIgnoreCase("TestPhoneOne");

    }

    @Override
    protected void onResume() {

        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!btAdapter.isEnabled()) {

            if (!btAdapter.isEnabled()) {

                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

            finish();
            return;
        }

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (btAdapter == null) {

            Toast.makeText(this, "bluetoothManager.getAdapter()==null", Toast.LENGTH_SHORT).show();
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        scanLeDevice(false);
    }

    private ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            super.onScanResult(callbackType, result);

            Log.i(TAG, result.getDevice().getName());

            addBluetoothDevice(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            super.onBatchScanResults(results);
            for (ScanResult result : results) {

                addBluetoothDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {

            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this, "onScanFailed: " + String.valueOf(errorCode), Toast.LENGTH_LONG).show();
        }

        private void addBluetoothDevice(BluetoothDevice device) {

            if (!listBluetoothDevice.contains(device)) {

                listBluetoothDevice.add(device);
                listViewLE.invalidateViews();
            }
        }
    };

    private void scanLeDevice(final boolean enable) {

        if (enable) {

            listBluetoothDevice.clear();
            listViewLE.invalidateViews();

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(() -> {

                bluetoothLeScanner.stopScan(scanCallback);
                listViewLE.invalidateViews();

                Toast.makeText(MainActivity.this, "Scan timeout", Toast.LENGTH_LONG).show();

                scanning = false;
                btnScan.setEnabled(true);

            }, SCAN_PERIOD);

            bluetoothLeScanner.startScan(scanCallback);
            scanning = true;
            btnScan.setEnabled(false);

        } else {

            bluetoothLeScanner.stopScan(scanCallback);
            scanning = false;
            btnScan.setEnabled(true);
        }
    }

    /**
     * Get user to give access to location for app
     */
    public void requestPermission() {

        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("GPS");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        (dialog, which) -> requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS));
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted

                    Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
/*
    *//**
     * Method to calculate rssi into distance in meters
     *
     * @param value rssi read
     * @return calculated distance
     *//*
    private String calculateDistance(String value) {

        double distance;
        // The one meter read and the path-loss exponent
        double _1MeterRead = -53.2376;
        double pathLossExponent = 2;

        distance = Math.pow(10, ((Double.valueOf(value) - _1MeterRead) / (-10 * pathLossExponent)));

        return String.valueOf(distance);
    }*/
}
