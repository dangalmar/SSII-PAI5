package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.KeyStore;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class MainActivity extends AppCompatActivity {

    String camas;
    String mesas;
    String sillas;
    String sillones;

    EditText inputCamas;
    EditText inputMesas;
    EditText inputSillas;
    EditText inputSillones;
    RadioGroup grupoInputsUsuarios;
    RadioButton inputUsuario;


    // Setup Server information
    protected static String server = "192.168.1.133";
    protected static int port = 7070;
    protected static SSLSocket conexion;

    protected static String publicKey1 = "";
    protected static String privateKey1 = "";

    protected static String publicKey2 = "";
    protected static String privateKey2 = "";

    protected static String publicKey3= "";
    protected static String privateKey3 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputCamas = findViewById(R.id.inputCamas);
        inputMesas = findViewById(R.id.inputMesas);
        inputSillas = findViewById(R.id.inputSillas);
        inputSillones = findViewById(R.id.inputSillones);
        grupoInputsUsuarios = findViewById(R.id.radioGroup);

        /**
         * Important para que las operaciones de red se permitan en el hilo principal
         */
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitNetwork().build());

        // Capturamos el boton de Enviar
        View button = findViewById(R.id.button_send);

        // Llama al listener del boton Enviar
        button.setOnClickListener(view -> {
            int radioId = grupoInputsUsuarios.getCheckedRadioButtonId();
            inputUsuario = findViewById(radioId);

            camas = inputCamas.getText().toString();
            mesas = inputMesas.getText().toString();
            sillas = inputSillas.getText().toString();
            sillones = inputSillones.getText().toString();

            showDialog(camas, mesas, sillas, sillones);
        });


    }

    // Creación de un cuadro de dialogo para confirmar pedido
    private void showDialog(String camas, String mesas, String sillas, String sillones) throws Resources.NotFoundException {

        if(TextUtils.isEmpty(camas) ||
                TextUtils.isEmpty(mesas) ||
                TextUtils.isEmpty(sillas) ||
                TextUtils.isEmpty(sillones)){
            Toast.makeText(getApplicationContext(), "Por favor introduce valores en todos los campos", Toast.LENGTH_SHORT).show();
        } else if (grupoInputsUsuarios.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getApplicationContext(), "Por favor seleccione un usuario para firmar", Toast.LENGTH_SHORT).show();
        } else if ( Integer.parseInt(camas.trim()) > 300 ||
                Integer.parseInt(mesas.trim()) > 300 ||
                Integer.parseInt(sillas.trim())  > 300||
                Integer.parseInt(sillones.trim()) > 300){
            Toast.makeText(getApplicationContext(), "Por favor seleccione valores por debajo de 300", Toast.LENGTH_SHORT).show();
        } else {
            final int numSab = Integer.parseInt(camas.trim());
            final int numCam = Integer.parseInt(mesas.trim());
            final int numMes = Integer.parseInt(sillas.trim());
            final int numSil = Integer.parseInt(sillones.trim());
            new AlertDialog.Builder(this)
                    .setTitle("Enviar")
                    .setMessage("Se va a proceder al envio")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                // Catch ok button and send information
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @SuppressLint("ResourceType")
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try{
                                        Signature firma = Signature.getInstance("SHA256withRSA");
                                        String dataNumbers = numSab + "-" + numCam + "-" + numMes + "-" + numSil;

                                        int index = grupoInputsUsuarios.indexOfChild(findViewById(grupoInputsUsuarios.getCheckedRadioButtonId()));
                                        PrivateKey privateKey = getPrivateKey(index);
                                        firma.initSign(privateKey);
                                        firma.update(dataNumbers.getBytes());
                                        byte[] firma_ = firma.sign();
                                        String firma_base64 = Base64.encodeToString(firma_, Base64.NO_WRAP);
                                        String message = dataNumbers + "," + firma_base64 + "," + index;
                                        startClient(message);
                                    } catch (NoSuchAlgorithmException | InvalidKeyException |
                                             SignatureException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }

                    )
                    .setNegativeButton(android.R.string.no, null).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public PrivateKey getPrivateKey(Integer i) throws Exception {
        String keyString = "";
        switch (i){
            case 0:
                keyString = privateKey1;
                break;
            case 1:
                keyString = privateKey2;
                break;
            case 2:
                keyString = privateKey3;
                break;
        }

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpecPKCS8 =
                new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(keyString));
        return kf.generatePrivate(keySpecPKCS8);
    }

    public void createKeyPair() throws Exception {
        KeyPairGenerator keyParGenerator = KeyPairGenerator.getInstance("RSA");
        keyParGenerator.initialize(2048);
        KeyPair keyPair = keyParGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
    }

    private void startClient(String message){
        String ip = "http://10.0.2.2";
        int puerto = 7071;
        String socket = "http://10.0.2.2:7071";
        try{
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(getAssets().open("certs/server/server.bks"),
                    "changeit".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "changeit".toCharArray());

            KeyStore trustedStore = KeyStore.getInstance("BKS");
            trustedStore.load(getAssets().open("certs/server/cacertserverbueno.bks"), "changeit"
                    .toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustedStore);

            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager[] tM = tmf.getTrustManagers();
            KeyManager[] kM = kmf.getKeyManagers();
            context.init(kM, tM, null);

            SSLSocketFactory factory = context.getSocketFactory();
            conexion = (SSLSocket) factory.createSocket(server, puerto);
            conexion.startHandshake();

            PrintWriter slide = new PrintWriter(
                    new OutputStreamWriter(conexion.getOutputStream()),true);

            try {
                System.out.println(message);
                slide.println(message);
                Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                System.out.println(e);
            }
            BufferedReader entrada = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            String messagereceived = entrada.readLine();
            Toast.makeText(getApplicationContext(), messagereceived, Toast.LENGTH_SHORT).show();


            conexion.close();
        }
        catch (Exception e){
            System.out.println("error: " + e);
            Toast.makeText(getApplicationContext(), "No se ha podido realizar una conexión con el servidor", Toast.LENGTH_SHORT).show();

        }
    }


}
