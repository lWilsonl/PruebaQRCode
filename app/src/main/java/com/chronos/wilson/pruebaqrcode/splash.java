package com.chronos.wilson.pruebaqrcode;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

public class splash extends Activity {

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //                                                                                             *
    //Esta actividad realmente no hace nada importante, mas alla de ser la pantalla de bienvenida  *
    //y ademas solicitar los permisos de la app                                                    *
    //                                                                                             *
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    //Variables para solicitud de multiples permisos
    private static final int MULTIPLE_PERMISSIONS_REQUEST_CODE = 3;
    private String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.VIBRATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //Ocultando la 'ActionBar' de la aplicacion
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        //Se inicia el metodo para solicitar los permisos
        permisos();

        //Establecemos el comportamiento de esta actividad, para que se muestre solamente unos segundos
        //y despues se inicie la actividad principal
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(splash.this,principal.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }

    //Metodo que solicita los permisos
    public void permisos () {
        if (ActivityCompat.checkSelfPermission(splash.this, permissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(splash.this, permissions[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(splash.this, permissions[2]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(splash.this, permissions[3]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(splash.this, permissions[4]) != PackageManager.PERMISSION_GRANTED) {
            //Si alguno de los permisos no esta concedido lo solicita
            ActivityCompat.requestPermissions(splash.this, permissions, MULTIPLE_PERMISSIONS_REQUEST_CODE);
        }
    }
}
