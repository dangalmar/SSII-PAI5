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
import android.util.Log;
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
import java.security.KeyFactory;
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
import java.security.spec.PKCS8EncodedKeySpec;
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
    int inputUsuario;


    // Setup Server information
    protected static String server = "10.0.2.2";
    protected static int port = 7070;
    private String keyMac = "602538936278945789227338510671310720268497086123762351854019088246135254642085";
    String sslPassword = "12345";
    private String caPath = "";
    protected static String privateKey1 = "MIIEowIBAAKCAQEAnsx97A4Ss7l1IJ+06o9vY4r6UnwSlcoeiqm/FCDBaqmgaeWACR1cPbUfUO7UaRy2IncyFTUIzj3z04whUiCy+L2fEurNdzkuPnQTokfLtRb1w6KdwGy0x/06qBqgUU36jjQX2Vje435hz4EIrxuwQpl2/FcTXEH/FHvKKxeF17+si4iZWmRICuYZq+3zGIMe84Tm/l/CHtt7+QhFAg3j98yqlWC1HIBv+ES5FMk1L77Vcnm9/9SkBQSl0SRG8knbq6Ilu77YQTSNLdTdRsjpI6/Yr+pZTnBE4xsjbqAEuXhNi2ivHbIN5r/EcYfQ8C+A/fcR1VBDHZwEMibs6nNWdQIDAQABAoIBAA3eJ0ikNmKApmegQUeGTQCt8RkFlQZRkOPXwB3TYHpeo7Yrxy5eMVSB1637NXL6gWnQHzN2Ueik+DREyN707eKs69JWNA+FEr9JTTLYv0PD/gsRzZUxr4LGYBPj9pUzHXFdJkOiY1CnR4ZvqwdgmfHiMhLTNEsg+yENHUVj6zN0k96fL8Q8Zsz5KOqgDU9ePUMSd3dtfdRMdL9rReV9I9c2zkvoQFNA/6KDSeo++I0JrEz8DqnhFIyEw3jxQXJvao2QCV8MmTW0On6HsLTMIxhPoz3V3z4V5I9MSS8HtTerW/zpdBiGwa5hf1gE+4Cb0pCncxzEvde1slj84ILc1Y8CgYEA3NOjxm6HNZ/xGeS9RJqjjpoA3Va4PpxBSI0jZDc7O+YP/fhWEgLO7X7TcPBFDRpI77ed0FgcMwspZbU62CHWSjqHVSwYdtybEsIcpeVnZHObu3ahuSQWprk1DmNKcoRvASDcgSbTfJN///7lu3JF0TxXS9GhZXl1pUMxub+/UkcCgYEAuBefC+fYBBC0J8MsxCYW7bnxNQZaWTEsTZNszRzvgE+z/ed100YTzZjS2VChCLb1puHqDBnRn2srQiTaHQ7mXL8773Fml/edUg8MwgB/3z6wH6UTmZHaN6hZ785pw6uFa427/9uuNdjrZKQbcWFDNGp8bOGXpEqSrRcB6FUq02MCgYByYYVhUPrkAdaGjP6kPF0fjhGIlFSWyaTfCYwdaqZZ3k7GRA2BAsdgKOMoiquZn9XLpRYvRpREtDFbJ0Y1kUFH7Y0PxuldF+OTEcx4+ZxWYbN2pHocRd8duK0MqM11B7ffo7/TfeLpkhUo6kTglM4pFNI6yXiJ8SHI2kc1aWaWHQKBgDLwJkwPDNCjJpZSBRMJTxLcfoejiGBhoNaeUl63APZKF43L++hH3bbn8H+9NbHJnlNc83wBsOUhrEiTFd8wfkFvkNACWtb15wwLqgYMSVWmbOl0reWyW+VGqgwW22EPFs3Z3AlB6gRwT0H7vrQyq3vXczJVgbYuBEPst9RnrH/tAoGBAKjHb6RKBkgeSfUOhwFWrIFl9IE6RcWN1cc9t3u9ocwpjGO1oorSQeKYhz2KXmTW0IyFAGfHBeM55e9jdWJqHS1uaxj2Ii6XKnefd2HOiyePWfCqmpO5BO7/bcboyu3gshEAtRlii2vZzKMAaPmE7TXhANqO4xtCMZmO41H9WKBk";
    protected static String privateKey2 = "MIIEpAIBAAKCAQEAxhP8nyNikmgkaDQmnncDFhfBd4FQOsPr6SBkjKlsetSutwdZCu6JQsTqZr80i+ZSCYwGP/SAZ2I7Gq5XL4YUqcCjHG8e5LauZIc9+y1u9j80YViFP+DLhJGs/UKErtR9CcKcgQ7El2SBDfbxTSyxH33uhkoycknDO/VuWlPvC9ruCQEXzPfyXBth0GYUxAwfdvxuHl2M41kBdY3qhXcwK1DDVfvDV7PEB//W/LeZFWLbIFTuqeBb4HYwscrwjmaULsC4NwTgf/rBpWyMMSv0P3rFEmhFPoiXKwOMJteImwXCp7F1PbYyLQpgvcCCshU6mIu/xp9jRCZtZehk9jtiHQIDAQABAoIBAC4RM+2nAyPdyKnbhyfPsg4PVFWA47HnIxTDDd+Q/8PJdyDmOpVKSO4YchOXYJcw59+Ei2eQa9SfifN4nJpW6rNYNBdG2L6EpiaHNakXNflbVclWFxd2Jp8cakk0kTs//ByrAE8bqikznLgsuI4kWMVkOndNBlKCST/GovWrE6uDxDjFBa07eK2nDWRMFHh924TClFDdsfSE65XhfyM+l8xyQbPCQpytAv/ZHppOlSzDNFP80b5gO4stcrobnbyGpB7BLXg5JggRb9tkA3Budu4oiaARusSsOlPaICfYqAs5EibwnR7dDypy7tEj7g5icy4XZVZp/vRp3o+aG0fYKMECgYEA9aFEb21zrZjEtLC/r36yQyD9hJEe5Zi/Vr8cG/yTOVds9JCPjEk37fgoMTQVEdSEGRTMzljpLBAZhRHOvxYa1djCpzlig/p5UIZK/onCy4fsHHoCUpBXBhd+gNrk7H3rJM09WnuzBu5JHqt4x852TZ7Q0Ibw8jL22j5bVd5V9DkCgYEAznDJJW35rAmmXwBAOzRy9AqDpb1yvWyuSkq7fTsqHntO9jUMK8mpSnENyhJy54mOOJpGoKYewj+qzjtFujfT4Oleo0qU66rm2wV4K+3agdMT2iHmDS1jMq7fTdd5AsVgzK33NfEr8slIvpfthF1ywqQpy7ZWYCntyVimgaQMhQUCgYEAmtuvb6jLs3iLG3gLgbMY5CWab8emeQ4SI7idGi/lCdPKh23Ucpd+wXMgs9SoK/OVzNJGRRnc98C2tiSB+gsPpB7iqdcN+zMg5Nml0lA8FWF+RH3PlQoXM2oAgXB/v3GrnCohMjimqAn243Ur0pyiDVHBSJaqtafX5cRGhR6FhlECgYBxvIvltkiHMy3ZS18X/1aSA7TyPIUZveXsgm33mgAjmRYw6ZsSCOHfZJgPS8jIre2QW9crrTpnFQK66scIyLdQQ+LjHjUb/iWAZ+wEb+AiBLP8Sw7fFbGAe/4FegBuuWYSfoojywLGmYUdlMDEW2PdvupUFNpT0uXyN4hNBp/rLQKBgQCZJpxQhen1mvalv2/yGipvNBMVvqhqcqjubpZ7LuKXWRkImv1RJBM2JV7qlmnEtUGfwNvnj9hbF1m1D6IHlgSy/46owJHQk8hI2RVvT+7MtEZDvQF+cGbg+TOXSJjR0BnTAd9W1j2qF6nuyW2TMdEnTpRZ4SO5mu/YSZjmIBxafQ==";
    protected static String privateKey3 = "MIIEpAIBAAKCAQEAoxkMiQKgfBgQ9UQhpQKt74OMnsdr8j6FfFeh2YdP+BD/cDN05uA0vq0TIy4rqBKxZT2ypT1sDjSAxiwXpvanfjb0FSxsCtNAZAVeF+smAeUzBqNyuwABv4bkaH1GqTKJ1TQ4fGDHf31jqIC2TcwW9XdwQzKfhJocCahYjVZyX4RE9sm6ixCrLegbxKWud/KX3+cgUxveLyFBiBLmZU6bI0vBca4sNfUriEf9tkOK0k9ps1Bc67DUBDNWsqrrj2NgK7ZWscsxdnCrYHWOxSRS6+qfhK72eGAgSS8E4uOaVQTizpOXvEcokab5mepm/uxsrEGbfYNoHAR7FkdkCJMUFQIDAQABAoIBACcWys1Pht0W+4F86b9djAhac+peCz4Mw/tQGWdEKeNfV4kxm/P1ik6ktnB5EVd/22pTRiyaMvqYlMBqMuT+OphiXU9xzRcvGECglLOQ1RQhNYCCze2Ji+G2V6m/VJ8akLQ7hsowe7/PcoIhDxBIOLxt+sbwLBEgylV6NnbxYAoKDEL2HJmLA5pJM8mTG27dJlZLmCXnKYWdssoZKmSsnqkUaEivw2z8yAv6Tv1pJ5ps6Ue51Jy3NYRQFvZMNbQVfFJJnDtdI7UQyEtn/q1W7OMJ4uEdiwp1irHsA8WE4PnJ1mJTDBzL9t7xrscXCYbllTNdNOCIfZhbG0x+mHL1nyECgYEA2fBGwfIIqhiKTv3THAlqer8LFAPhnqNs5xT5iEfPFYkLzbPi3YToFMZU0L0tzoiu22uPXXFEcG5zdl41x8d6eBf+I3LNe1tcwfwTfP449nyR7fLdVqt1TmD7EvZuc5wBfq2ds+dqpG6RYxcNvmLWXtNUQy1/37YXat9JbUrk0C0CgYEAv5Tr6GLtOTXikOZURmqLnVuAp8W+su0ZvU2Y2KImsJYkOPWdmssPYuydDhu3f17rvtKyTrqPjqtqpapZAjOGhbxcYeizcCZAI82A43OK/AvhcGiHJ2uCFWAIsaJBo9n4xywA+2r8PQdA+p68P0pEHKnd6Ali9U6Xa6fcUyC93IkCgYBqLyqadk2IapqAsCT6pQ/PLGO+ZcG45FE8KbkGkE4yZOpuV/XyDj5xWMycQ4Ac//WKDNb2JSi+SuJQysgTRXrDJpV5Ogcp0jLYhPQN02N3nOwQ7mKvRqzJ+nB6Jb1c8Ka9zONocxk/cmu9xs64czmVTAvjCzkhi3vPY2lIKdRgJQKBgQCazp6WCKJCMX2jssr0n5GzlMXwTATdlmPPKQ3SN6zl0lzwveAdvenv+NysEs+DF9ONPbohjfUExxUFSxJjifkwxdUstJmSjQYVKDD3Gl17b6o6Z+yWePQDi7dauo8p9K9nWfJtNrUeJ2dlXLEmvz8snkKXBka1jE0lC94oOfPRaQKBgQCkK4noxmGmul/QMk9d54YfI924o8FhubRX4PrA1tqARpyWIQEv3M690PKLUd3zCcBivqApV+DUUgcrIUQquIJ2g8a18UHTlo8+mOikC8P8fzqBLdYxtoVxXhrsYFEspr4fAwqQvHsnJ+jrIz9PjC+d5mBbE/O01Az3gF2SbipjbA==";

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
            inputUsuario = grupoInputsUsuarios.indexOfChild(findViewById(grupoInputsUsuarios.getCheckedRadioButtonId()));

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

                                        final String messageSign = generateMessageSign(message, getPrivateKey(inputUsuario));

                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {

                                                try {

                                                    //SSLSocketFactory socketFactory = getSslSocketFactory();
                                                    SocketFactory socketFactory = (SocketFactory) SocketFactory.getDefault();
                                                    Socket socket = (Socket) socketFactory.createSocket(server,port);

                                                    String nonce = generateNonce(16);

                                                    String messageToHmac = message+nonce;
                                                    String hmac = calcHmacSha256(keyMac.getBytes("UTF-8"),(messageToHmac).getBytes("UTF-8"));

                                                    JSONObject dataJson = new JSONObject();
                                                    dataJson.put("message", messageJson);
                                                    dataJson.put("clientNumber", inputUsuario);
                                                    dataJson.put("messageSign", messageSign);
                                                    dataJson.put("nonce", nonce);
                                                    dataJson.put("hmac", hmac);

                                                    String data = dataJson.toString();

                                                    Log.i("Data", data);

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

    private static String generateMessageSign(String message, PrivateKey pk) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sg = Signature.getInstance("SHA256withRSA");
        sg.initSign(pk);
        sg.update(message.getBytes());
        byte[] firma = sg.sign();

        return Base64.encodeToString(firma, Base64.DEFAULT);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static PrivateKey getPrivateKey(Integer i) throws Exception {
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
        PrivateKey privateKey = kf.generatePrivate(keySpecPKCS8);
        return privateKey;
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
