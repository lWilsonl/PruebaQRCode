package com.chronos.wilson.pruebaqrcode;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

//Imports para la generacion de codigos QR
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

//Imports para el guardado de imagenes en dispositivo
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class generarQR extends Activity {

    //Instanciamos las variables a utilizar para la generacion de codigos QR
    EditText edtText;
    Button btnGenerar;
    ImageView miQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_qr);
        //Ocultamos la ActionBar de la actividad
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        //En cuanto la actividad se inicie, se hace un llamado a los metodos de instanciado
        //de variables utilizadas con sus respectivos ID en la actividad y ademas se inicia
        //el metodo para que espere acciones del boton de la actividad
        inicializarComponentes();
        clickButton();
    }

    //Enlace entre los objetos de Java con los de la Activity
    private void inicializarComponentes() {
        edtText = findViewById(R.id.edtText);
        btnGenerar = findViewById(R.id.btnGenerar);
        miQR = findViewById(R.id.imgQR);
    }

    //Se establece el 'onclicklistener' para nuestro boton, que al ser presionado llama al
    //metodo designado para la generacion del codigo QR utilizando la libreria de zxing
    private void clickButton() {
        btnGenerar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generarQR();
            }
        });
    }

    //Metodo que utiliza la libreria zxing para generar el codigo QR necesario
    private void generarQR() {
        String Texto = edtText.getText().toString();
        //Validacion del editText : Que no este vacio
        if(Texto.equals(null) || Texto.equals("") || Texto.isEmpty()){
            AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(this);
            String alert_title3 = "Campo vacío";
            String alert_description3 = "Ingrese un número de carnet";
            alertDialogBuilder3.setTitle(alert_title3);
            alertDialogBuilder3
                    .setMessage(alert_description3)
                    .setCancelable(false)
                    .setNeutralButton("Ok",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("Dialogos", "Se dio cuenta del error");
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog3 = alertDialogBuilder3.create();
            alertDialog3.show();
        }else{
            try{
                //Se genera el codigo QR para el No. de carnet ingresado
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = multiFormatWriter.encode(Texto, BarcodeFormat.QR_CODE,500,500);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                miQR.setImageBitmap(bitmap);
                //Se manda a llamar el conjunto de metodos encargados de guardar los codigos QR en el celular
                GuardarImagen(bitmap);
            }catch (WriterException e){
                e.printStackTrace();
            }
        }
    }

    //Metodo encargado de convertir el archivo Bitmap generado en la codificacion del codigo QR
    //en una imagen tipo PNG para poder guardarlo. Ademas de ser el encargado de llamar el metodo que
    //se encarga de buscar la ruta destino del codigo QR y darle nombre a la imagen
    private void GuardarImagen(Bitmap image) {
        //Se le da el nombre al archivo de imagen, ademas de la ruta en la que se guardara la imagen con el siguiente
        //metodo
        File pictureFile = obtenerArchivo();
        if (pictureFile == null) {
            Toast.makeText(this, "Error al crear el archivo, verifique los permisos de la apliación", Toast.LENGTH_SHORT).show();
            return;
        }
        //Convierte el objeto tipo File 'pictureFile' en una imagen tipo .PNG y ademas
        //La guarda en la ruta especificada por el metodo 'obtenerArchivo'
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }catch (FileNotFoundException e) {
            Toast.makeText(this, "No se encontró el archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            Toast.makeText(this, "Error al accesar el archivo/carpeta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Crea un objeto tipo 'File', para guardar la imagen, ademas de darle nombre y una ruta de guardado
    private  File obtenerArchivo(){
        //Validaciones para que pueda encontrar una ruta de almacenamiento valida en los distintos
        //dispositivos
        String strSDCardPath = ("/sdcard");
        if(Environment.getExternalStorageState().equals("MEDIA_MOUNTED")){
            strSDCardPath = System.getenv("SECONDARY_STORAGE");
            if ((strSDCardPath == null) || (strSDCardPath.length() == 0)) {
                strSDCardPath = System.getenv("EXTERNAL_STORAGE");
            }
            if ((strSDCardPath == null) || (strSDCardPath.length() == 0)) {
                strSDCardPath = System.getenv("EXTERNAL_SDCARD_STORAGE");
            }
            if ((strSDCardPath == null) || (strSDCardPath.length() == 0)) {
                strSDCardPath = "Environment.getExternalStorageDirectory()";
            }
        }
        //Generamos una ruta en la que guardar el archivo
        File mediaStorageDir = new File(strSDCardPath
                + "/Congreso PROXY/"
                + getApplicationContext().getPackageName()
                + "/QR Generados");
        // Crea la carpeta de almacenamiento si no existe
        if (! mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        // Crea un archivo de media (Imagen) con el nombre especificado (Fecha y hora actual)
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        //Creamos el objeto file que va a guardar la imagen con el nombre y la ruta
        File mediaFile;
        String nombreImagen="QR_"+ timeStamp +".png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + nombreImagen);
        Toast.makeText(getApplicationContext(), "Qr creado en: " + mediaStorageDir, Toast.LENGTH_LONG).show();
        return mediaFile;
    }

    //Metodo que despliega el dialog para salir del activity
    public void SalirModulo (View view){
        String Texto = edtText.getText().toString();
        if(TextUtils.isEmpty(Texto)){
            generarQR.this.finish();
        }else{
            AlertDialog.Builder alertDialogBuilder3 = new AlertDialog.Builder(this);
            String alert_title3 = "Salir del módulo";
            String alert_description3 = "¿Desea salir de este módulo?";
            alertDialogBuilder3.setTitle(alert_title3);
            alertDialogBuilder3
                    .setMessage(alert_description3)
                    .setCancelable(false)
                    .setPositiveButton("Si",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.i("Dialogos", "Confirmación Aceptada");
                            generarQR.this.finish();
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
}
