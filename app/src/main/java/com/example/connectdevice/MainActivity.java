package com.example.connectdevice;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Button startScan;
    Button stopScan;
    ListView deviceList;
    TextView logView;
    private static final String TAG = "MyActivity";
    boolean isDiscoverable = false;
    boolean stopScanning = false;

    //default bluetooth adapter is called and name is set to mBluetoothAdapter
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<BluetoothDevice> deviceData = new ArrayList<BluetoothDevice>();

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            //bluetooth is enabled if it's not
        int REQUEST_ENABLE_BT = 1;
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        //button is initialized
        startScan =  findViewById(R.id.btnStartScan);
        stopScan = findViewById(R.id.btnStopScan);

        //list is initialized
        deviceList = findViewById(R.id.deviceList);
        deviceList.setOnItemClickListener(this);

        logView = findViewById(R.id.LogView);

        //added listener on button that call the function listPairedDevices
        startScan.setOnClickListener(this::scanDevicesButton);
        stopScan.setOnClickListener(this::stopScanButton);
    }

    @SuppressLint("MissingPermission")
    private void scanDevicesButton(View view) {

        stopScanning = false;

        //adapter is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        //makes the device discoverable the first time the button is pressed
        if (isDiscoverable==false){
            isDiscoverable=true;
            makeDiscoverable();
        }

        // if the adapter is not discovering start the discovery
        if (!mBluetoothAdapter.isDiscovering()) {
            startBluetoothScan();
        }
    }

    @SuppressLint("MissingPermission")
    private void startBluetoothScan(){

        //arrays are initialized
        iniArray();

        //starts bluetooth discovery and checks if it  started by printing the value true
        boolean hasDiscoveryStarted;
        hasDiscoveryStarted = mBluetoothAdapter.startDiscovery();
        Log.d(TAG, "Discovery started: "+String.valueOf(hasDiscoveryStarted));

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                //if device is found
                if (deviceName!=null){
                    Log.d(TAG,device.getName());

                    //device data is given to the array loader
                    loadDevicesToList(device);
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                if (!stopScanning){
                    //if bluetooth scan has finished it restarts the process
                    Log.d(TAG,"Discovery has finished");
                    startBluetoothScan();
                }
                else{
                    iniArray();
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void loadDevicesToList(BluetoothDevice device){

        //if there is not the same device already
        if (!arrayList.contains(device.getName() + "\n" + device.getAddress())) {

            //device data is added to the device data array
            deviceData.add(device);

            //device name and mac address added to the array
            arrayList.add(device.getName() + "\n" + device.getAddress());
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);

            //array is set to the listview
            deviceList.setAdapter(arrayAdapter);
        }
    }

    @SuppressLint("MissingPermission")
    private void makeDiscoverable(){
        int requestCode = 1;
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1000000000);
        startActivityForResult(discoverableIntent, requestCode);
    }

    @SuppressLint("MissingPermission")
    private void connectBluetoothDevice(int i){

        //gets the data from the device data array
        BluetoothDevice device = deviceData.get(i);

        //scanning is stopped
        stopScanning=true;
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "Pairing with: " + device.getName());

        //bonded is 11, not bonded is 10 ????? bruh
        int isBonded = device.getBondState();
        Log.d("Bond state", String.valueOf(isBonded));

        //device is paired
        device.createBond();

        //device.setPairingConfirmation(true);

        //waits for user pairing confirmation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //device.setPairingConfirmation(true);
                int devicePairCheck=device.getBondState();
                Log.d("Bond state", String.valueOf(devicePairCheck));

                //if device is not paired scan again
                if (devicePairCheck==10){
                    startBluetoothScan();
                }
                else{
                    logView.setText("Device paired");
                }
            }
        }, 5000);   //10 seconds
    }
    @SuppressLint("MissingPermission")
    private void stopScanButton(View view){
        Log.d(TAG,"Stop scan button clicked");
        stopScanning=true;
        mBluetoothAdapter.cancelDiscovery();
    }

    private void iniArray(){
        //arrayList is initialized
        arrayList = new ArrayList<>();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);

        //device list is sent to the listview
        deviceList.setAdapter(arrayAdapter);

        //bluetooth device data array is cleared
        deviceData.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, String.valueOf(i));
        connectBluetoothDevice(i);
    }

    /*
    @SuppressLint("MissingPermission")
    private boolean listPairedDevices(String deviceToFind) {

        //get list of bonded devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        Log.d("Paired device size: ", String.valueOf(pairedDevices.size()));
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                //arrayList.add(deviceName);
                //Log.d("Paired device name: ", deviceName);
                //Log.d("Paired device address: ", deviceHardwareAddress);
                if (deviceName==deviceToFind){
                    return true;
                }
            }
            return false;
        }
        return false;
    }
     */
}