package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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
    protected static String server = "127.0.0.1";
    protected static int port = 7070;
    private String keyMac = "108079546209274483481442683641105470668825844172663843934775892731209928221929";
    String sslPassword = "12345";
    private String caPath = "";

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
            final int camasPedidas = Integer.parseInt(camas.trim());
            final int mesasPedidas = Integer.parseInt(mesas.trim());
            final int sillasPedidas = Integer.parseInt(sillas.trim());
            final int sillonesPedidos = Integer.parseInt(sillones.trim());
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
                                        final JSONObject messageJson = new JSONObject();
                                        messageJson.put("camas", camasPedidas);
                                        messageJson.put("mesas", mesasPedidas);
                                        messageJson.put("sillas", sillasPedidas);
                                        messageJson.put("sillones", sillonesPedidos);
                                        messageJson.put("clientNumber", inputUsuario);
                                        final String message = messageJson.toString();

                                        final String messageSign = generateMessageSign(message);

                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {

                                                try {

                                                    //SSLSocketFactory socketFactory = getSslSocketFactory();
                                                    SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                                                    SSLSocket socket = (SSLSocket) socketFactory.createSocket(server, port);

                                                    String nonce = generateNonce(16);

                                                    String messageToHmac = message+nonce;
                                                    String hmac = calcHmacSha256(keyMac.getBytes("UTF-8"),(messageToHmac).getBytes("UTF-8"));

                                                    JSONObject dataJson = new JSONObject();
                                                    dataJson.put("message", messageJson);
                                                    dataJson.put("messageSign", messageSign);
                                                    dataJson.put("nonce", nonce);
                                                    dataJson.put("hmac", hmac);

                                                    String data = dataJson.toString();

                                                    PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                                                    output.println(data);
                                                    output.flush();

                                                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                                    String response = input.readLine();
                                                    if(response!=null && !response.isEmpty() && response.contains("OK")) {
                                                        Toast.makeText(MainActivity.this, "Petición enviada correctamente", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Ha ocurrido un problema", Toast.LENGTH_SHORT).show();
                                                    }

                                                    // close connection
                                                    output.close();
                                                    input.close();
                                                    socket.close();

                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    Toast.makeText(MainActivity.this, "Ha ocurrido un problema", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });


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

    private static String generateMessageSign(String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        KeyPair kp = getRSAKeyPair();
        PrivateKey pk = kp.getPrivate();

        Signature sg = Signature.getInstance("SHA256withRSA");
        sg.initSign(pk);
        sg.update(message.getBytes());
        byte[] firma = sg.sign();

        return Base64.encodeToString(firma, Base64.DEFAULT);
    }

    public static KeyPair getRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    private String generateNonce(Integer size) {

        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < size; i++) {
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    static public String calcHmacSha256(byte[] secretKey, byte[] message) {
        byte[] hmacSha256 = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return bytesToHex(hmacSha256);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private SSLSocketFactory getSslSocketFactory() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, IOException, CertificateException {
        KeyStore ks = KeyStore.getInstance("JKS");

        // get user password and file input stream
        char[] password = new char[sslPassword.length()];
        for (int i = 0; i < sslPassword.length(); i++) {
            password[i] = sslPassword.charAt(i);
        }

        ClassLoader cl = this.getClass().getClassLoader();
        InputStream stream = cl.getResourceAsStream(caPath);
        ks.load(stream, password);
        stream.close();

        SSLContext sc = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

        kmf.init(ks, password);
        tmf.init(ks);

        sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(),null);

        return sc.getSocketFactory();
    }
}
