package com.chronos.wilson.pruebaqrcode;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

//Imports para la lectura de QR
import com.blikoon.qrcodescanner.QrCodeActivity;

//Imports necesarios para la subida de datos a la hoja de calculo
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class principal extends Activity {

    //Variable que contendra los datos del escaneo para que sea subido al script de Google
    String scannedData;
    String part1; //No.Carnet
    String part2; //No.Control
    String part3; //Nombre
    //Instanciación de varible para el control de la lectura del codigo QR
    private static final int REQUEST_CODE_QR_SCAN = 1;
    //Generación de variable para utilizarla en la salida del LOG
    private static final String LOGTAG = "LogsAndroid";
    //Instanciación de las variables para poder controlar la solicitud de permisos
    private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 3;
    private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        //Ocultando la 'ActionBar' de la aplicacion
        ActionBar actionBar = getActionBar();
        actionBar.hide();

    }

    //Metodo para inicializar la actividad de lectura de codigo QR y solicitud de permisos
    public void LeerQR(View v) {
        //Chequeo de permisos
        if (ActivityCompat.checkSelfPermission(principal.this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(principal.this, permissions[1]) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(principal.this, permissions[2]) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(principal.this, permissions[3]) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(principal.this, permissions[4]) != PackageManager.PERMISSION_GRANTED) {
            //Si alguno de los permisos no esta concedido lo solicita
            ActivityCompat.requestPermissions(principal.this, permissions, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        } else {
            //Si el permiso esá concedico prosigue con el flujo normal = Se inicia la actividad de lectura del codigo QR
            Intent i = new Intent(principal.this, QrCodeActivity.class);
            startActivityForResult( i,REQUEST_CODE_QR_SCAN);
        }
    }

    //Metodo para inicializar la actividad que genera QR's
    public void GenerarQR(View v){
        //Chequeo de permisos
        if (ActivityCompat.checkSelfPermission(principal.this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(principal.this, permissions[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(principal.this, permissions[2]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(principal.this, permissions[3]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(principal.this, permissions[4]) != PackageManager.PERMISSION_GRANTED){
            //Si alguno de los permisos no esta concedido lo solicita
            ActivityCompat.requestPermissions(principal.this, permissions, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        } else {
            //Si el permiso esá concedico prosigue con el flujo normal = Se inicia la actividad de generar el codigo QR
            Intent i = new Intent(getApplicationContext(),generarQR.class);
            startActivity(i);
        }
    }

    //Metodo 'onActivityResult' invocado por el metodo 'LeerQR' para obtener y mostrar el codigo QR
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            Log.d(LOGTAG,"No se pudo obtener un buen resultado.");
            if(data==null)
                return;
            //Obteniendo el resultado enviado desde la actividad de escaneo, en este caso, de error
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if( result!=null) {
                AlertDialog alertDialog = new AlertDialog.Builder(principal.this).create();
                alertDialog.setTitle("Error de escaneo");
                alertDialog.setMessage("El código QR no se pudo leer.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;
        }

        //Si el resultado del escaneo es positivo, se inicia el metodo para la subida al Script de Google
        if(requestCode == REQUEST_CODE_QR_SCAN) {
            if(data==null)
                return;
            //Obteniendo el resultado enviado desde la actividad de escaneo
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d(LOGTAG,"Aqui tiene el resultado del escaneo :"+ result);
            //ignor me pls
            AlertDialog alertDialog = new AlertDialog.Builder(principal.this).create();
            alertDialog.setTitle("Resultado");
            alertDialog.setMessage(result);
            scannedData = result;
            if (scannedData != null && scannedData.contains(",") && scannedData.length()>=14) {
                String string = scannedData;
                String[] parts = string.split(",");
                part1 = parts[0]; // No. Carnet
                part2 = parts[1]; // No. Control
                part3 = parts[2]; // Nombre
                new SendRequest().execute();
            }else{
                if (scannedData != null && scannedData.contains(",") && scannedData.length()<=12) {
                    String string = scannedData;
                    String[] parts = string.split(",");
                    part1 = parts[0]; // No. Carnet
                    part2 = parts[1]; // No. Control
                    part3 = "Consultar en registro de asistentes";
                    new SendRequest().execute();
                }else{
                    if (scannedData != null && scannedData.length()<=3) {
                        part1 = scannedData; // No. Carnet
                        part2 = "Consultar en registro de asistentes"; // No. Control
                        part3 = "Consultar en registro de asistentes"; // Nombre
                        new SendRequest().execute();
                    }else{
                        Toast.makeText(getApplicationContext(), "Error: Formato de QR Erroneo", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    //Inicia una solicitud asincrona de conexion con el script en linea encargado de subir los datos obtenidos a la hoja de calculo
    public class SendRequest extends AsyncTask<String, Void, String> {
        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                //URL de nuestro script que es llamado una vez se lee de manera correcta el codigo QR
                URL url = new URL("https://script.google.com/macros/s/AKfycbxDCaYEmltqyyZkYLbUDpRhlmC_v9BHcUxdeeWI3A/exec");

                JSONObject postDataParams = new JSONObject();

                //Pasa el codigo escaneo de cada una de las variables instanciadas y asiganada en la lectura a las variables
                //reconocibles por el script de google

                postDataParams.put("sdata", part1);
                postDataParams.put("sdata2", part2);
                postDataParams.put("sdata3", part3);

                Log.e("params",postDataParams.toString());

                //Se establecen parametros necesarios para que google reconozca nuestra conexion al script
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    public void Salir (View view){
        AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(this);
        String alert_title3 = "Salir";
        String alert_description3 = "¿Desea salir de la aplicación?";
        alertDialogBuilder3.setTitle(alert_title3);
        alertDialogBuilder3
                .setMessage(alert_description3)
                .setCancelable(false)
                .setPositiveButton("Si",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmación Aceptada");
                        principal.this.finish();
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.i("Dialogos", "Confirmación Cancelada");
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog3 = alertDialogBuilder3.create();
        alertDialog3.show();
    }
}
