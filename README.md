# Bluetooth-Rssi
Application to get rssi value fro Bluetooth device.

The application is set so that a you can use the getBluetoothDevice() method to connect to an access point of your choosing.

    public boolean getBluetoothDevice(String device) {

        return ssid.equalsIgnoreCase("Bluetooth Device");

    }
Change the name "AccessPoint" to the name of the access point that you wish to scan.

The Values will be written out to a given database
I have used Volley to write out the data, you just need to change the url to your own destination.
    
    private static final String REGISTER_URL = "http://pathTo/yourUploadFile.php";

# Important
This application is created using the older method for scanning for Bluetooth and will be updated to BLE shortly.
