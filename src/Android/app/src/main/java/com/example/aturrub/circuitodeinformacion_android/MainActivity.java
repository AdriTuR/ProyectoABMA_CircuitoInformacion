//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
//                 						MAINACTIVITY.JAVA														   //
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
// Adrián Tur Rubio
// Proyecto Aplicaciones de Biometria y Medio Ambiente - Parte Android
// Curso 2022-2023
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
package com.example.aturrub.circuitodeinformacion_android;
//------------------------------------------------------------------//
//----------------------------Imports-------------------------------//
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
//------------------------------------------------------------------//
//------------------------------------------------------------------//

public class MainActivity extends AppCompatActivity {

    //-------------------------------------------------------------//
    //-------------------VARIABLES GLOBALES------------------------//

    private static final String ETIQUETA_LOG = ">>>>";
    private static final int CODIGO_PETICION_PERMISOS = 11223344;
    private BluetoothLeScanner elEscanner;
    private ScanCallback callbackDelEscaneo = null;

    //-------------------------------------------------------------//
    //-------------------------------------------------------------//

//----------------------------------------------------------------------------------------------//
//----------------------------------------ONCREATE()--------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(ETIQUETA_LOG, " onCreate(): empieza ");

        inicializarBlueTooth();

        Log.d(ETIQUETA_LOG, " onCreate(): termina ");
    }

//----------------------------------------------------------------------------------------------//
//--------------------------------ONREQUESTPERMISSIONSRESULT()----------------------------------//

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults);

        switch (requestCode) {
            case CODIGO_PETICION_PERMISOS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): permisos concedidos  !!!!");
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                }  else {

                    Log.d(ETIQUETA_LOG, " onRequestPermissionResult(): Socorro: permisos NO concedidos  !!!!");

                }
                return;
        }
    }

//----------------------------------------------------------------------------------------------//
//---------------------------------INICIALIZARBLUETOOTH()---------------------------------------//
//
//                             --> inicializarBluetooth() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    @SuppressLint("MissingPermission")
    private void inicializarBlueTooth() {
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos adaptador BT ");
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitamos adaptador BT ");
        bta.enable();
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): habilitado =  " + bta.isEnabled() );
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): estado =  " + bta.getState() );
        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): obtenemos escaner btle ");
        this.elEscanner = bta.getBluetoothLeScanner();

        if ( this.elEscanner == null ) {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): Socorro: NO hemos obtenido escaner btle  !!!!");
        }

        Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): voy a perdir permisos (si no los tuviera) !!!!");

        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PETICION_PERMISOS);
        }
        else {
            Log.d(ETIQUETA_LOG, " inicializarBlueTooth(): parece que YA tengo los permisos necesarios !!!!");
        }
    }
//----------------------------------------------------------------------------------------------//
//--------------------------buscarTodosLosDispositivosBTLE()------------------------------------//
//
//                      --> buscarTodosLosDispositivosBTLE() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    @SuppressLint("MissingPermission")
    private void buscarTodosLosDispositivosBTLE() {
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empieza ");
        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): instalamos scan callback ");
        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanResult() ");
                mostrarInformacionDispositivoBTLE(resultado);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onBatchScanResults() ");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): onScanFailed() ");
            }
        };

        Log.d(ETIQUETA_LOG, " buscarTodosLosDispositivosBTL(): empezamos a escanear ");
        this.elEscanner.startScan(this.callbackDelEscaneo);
        Log.d(ETIQUETA_LOG,"final de la mierda de buscar");
    }
//----------------------------------------------------------------------------------------------//
//-------------------------BOTONBUSCARDISPOSITIVOSBTLEPULSADO()---------------------------------//
//
//               <View>  --> detenerBusquedaDispositivosBTLE() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    public void botonBuscarDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton buscar dispositivos BTLE Pulsado");
        this.buscarTodosLosDispositivosBTLE();

    }
//----------------------------------------------------------------------------------------------//
//-------------------------mostrarInformacionDispositivosBTLE()---------------------------------//
//
//          Resultado <ScanResult>  --> buscarTodosLosDispositivosBTLE() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    @SuppressLint("MissingPermission")
    private void mostrarInformacionDispositivoBTLE(ScanResult resultado) {

        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        int rssi = resultado.getRssi();

        Log.d(ETIQUETA_LOG, " ****************************************************");
        Log.d(ETIQUETA_LOG, " ****** DISPOSITIVO DETECTADO BTLE ****************** ");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        Log.d(ETIQUETA_LOG, " nombre = " + bluetoothDevice.getName());
        Log.d(ETIQUETA_LOG, " toString = " + bluetoothDevice.toString());

        /*
        ParcelUuid[] puuids = bluetoothDevice.getUuids();
        if ( puuids.length >= 1 ) {
            //Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].getUuid());
           // Log.d(ETIQUETA_LOG, " uuid = " + puuids[0].toString());
        }*/

        Log.d(ETIQUETA_LOG, " dirección = " + bluetoothDevice.getAddress());
        Log.d(ETIQUETA_LOG, " rssi = " + rssi);

        Log.d(ETIQUETA_LOG, " bytes = " + new String(bytes));
        Log.d(ETIQUETA_LOG, " bytes (" + bytes.length + ") = " + Utilidades.bytesToHexString(bytes));

        TramaIBeacon tib = new TramaIBeacon(bytes);

        Log.d(ETIQUETA_LOG, " ----------------------------------------------------");
        Log.d(ETIQUETA_LOG, " prefijo  = " + Utilidades.bytesToHexString(tib.getPrefijo()));
        Log.d(ETIQUETA_LOG, "          advFlags = " + Utilidades.bytesToHexString(tib.getAdvFlags()));
        Log.d(ETIQUETA_LOG, "          advHeader = " + Utilidades.bytesToHexString(tib.getAdvHeader()));
        Log.d(ETIQUETA_LOG, "          companyID = " + Utilidades.bytesToHexString(tib.getCompanyID()));
        Log.d(ETIQUETA_LOG, "          iBeacon type = " + Integer.toHexString(tib.getiBeaconType()));
        Log.d(ETIQUETA_LOG, "          iBeacon length 0x = " + Integer.toHexString(tib.getiBeaconLength()) + " ( "
                + tib.getiBeaconLength() + " ) ");
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToHexString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " uuid  = " + Utilidades.bytesToString(tib.getUUID()));
        Log.d(ETIQUETA_LOG, " major  = " + Utilidades.bytesToHexString(tib.getMajor()) + "( "
                + Utilidades.bytesToInt(tib.getMajor()) + " ) ");
        Log.d(ETIQUETA_LOG, " minor  = " + Utilidades.bytesToHexString(tib.getMinor()) + "( "
                + Utilidades.bytesToInt(tib.getMinor()) + " ) ");
        Log.d(ETIQUETA_LOG, " txPower  = " + Integer.toHexString(tib.getTxPower()) + " ( " + tib.getTxPower() + " )");
        Log.d(ETIQUETA_LOG, " ****************************************************");

        if (Utilidades.bytesToString(tib.getUUID()).equals("ADRIANTUR-GTI-3A")) {
            this.detenerBusquedaDispositivosBTLE();
            Medicion medicion = recibirMedicion(resultado);

            //Hacemos un POST a la bbdd
            guardarMedicion("https://aturrub.upv.edu.es/serv/apiREST.php",medicion);

            //Temporizador
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                                          @Override
                                          public void run() {
                                              buscarTodosLosDispositivosBTLE();
                                          }
                                      },
                    0, 20000);   // Recordatorio: 1000 Milisegundo = 1 segundo
        }

    }
//----------------------------------------------------------------------------------------------//
//----------------------------DETENERBUSQUEDADISPOSITIVOSBTLE()---------------------------------//
//
//                       --> detenerBusquedaDispositivosBTLE() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    @SuppressLint("MissingPermission")
    private void detenerBusquedaDispositivosBTLE() {
        if (this.callbackDelEscaneo == null) {
            return;
        }
        this.elEscanner.stopScan(this.callbackDelEscaneo);
        this.callbackDelEscaneo = null;
    }
//----------------------------------------------------------------------------------------------//
//----------------------BOTONDETENERBUSQUEDADISPOSITIVOSBTLEPULSADO()---------------------------//
//
//                          <View>  --> metodoPOST() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    public void botonDetenerBusquedaDispositivosBTLEPulsado(View v) {
        Log.d(ETIQUETA_LOG, " boton detener busqueda dispositivos BTLE Pulsado");
        this.detenerBusquedaDispositivosBTLE();
    }
//----------------------------------------------------------------------------------------------//
//-------------------------------------recibirMedicion()----------------------------------------//
//
//              Resultado <ScanResult>  --> recibirMedicion() --> <Medicion>
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    Medicion recibirMedicion(ScanResult resultado){
        BluetoothDevice bluetoothDevice = resultado.getDevice();
        byte[] bytes = resultado.getScanRecord().getBytes();
        TramaIBeacon tib = new TramaIBeacon(bytes);
        String uuid=Utilidades.bytesToString(tib.getUUID());
        String device=String.valueOf(bluetoothDevice);

        //Pasamos la informacion del beacon a objeto Medicion para guardarlo en la BBDD.
        String fechaSegundos=String.valueOf(System.currentTimeMillis()); //obtenemos el tiempo
        String valor = String.valueOf(Utilidades.bytesToInt(tib.getMinor())); //obtenemos el valor
        Medicion medicion = new Medicion(fechaSegundos,"o2",valor);//creamos el objeto medicion para poder mandarlo a la BBDD

        this.detenerBusquedaDispositivosBTLE();
        return medicion;
    }
//----------------------------------------------------------------------------------------------//
//-------------------------------------guardarMedicion()----------------------------------------//
//
//           <String>, <Medicion>  --> guardarMedicion() -->
//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
    private void guardarMedicion(String URL,Medicion medicion){
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "DATO ENVIADO CON EXITO", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> datos=new HashMap<String,String>();
                datos.put("Tiempo",medicion.getTiempo());
                datos.put("Sensor",medicion.getSensor());
                datos.put("valor",medicion.getValor());
                return datos;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
//----------------------------------------------------------------------------------------------//
}