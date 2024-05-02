package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.net.ServerSocketFactory;

public class Server {

    private static final String DATABASE_URL = "jdbc:sqlite:server_data.db";

    public static void main(String[] args) throws Exception {

        // Crear la base de datos y la tabla si no existen
        createDatabase();

        // Crea un servidor
        ServerSocketFactory socketFactory = ServerSocketFactory.getDefault();
        ServerSocket serverSocket = socketFactory.createServerSocket(8088);

        while (true) {
            try {
                System.err.println("Esperando conexiones en 192.168.1.134:8088 ..");

                // Espera la conexión de un cliente
                Socket socket = serverSocket.accept();

                // Abre BufferedReader para leer datos del cliente
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Abre PrintWriter para enviar datos al cliente
                PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                // Lee los datos enviados por el cliente
                String values = input.readLine();
                String str_firma = input.readLine();

                // Parsea los valores de la firma
                byte[] firma = Hex.decode(str_firma.getBytes(Charset.forName("UTF-8")));

                // Verifica la firma digital
                boolean overload = checkSobrecarga();
                System.out.println("OV: " + overload);

                if (!overload) {
                    boolean verified = verificaFirmaDigital(values, firma, str_firma);
                    // Divide los valores enviados por el cliente
                    String[] allvalues = values.split(",");
                    Integer camas = Integer.parseInt(allvalues[0]);
                    Integer mesas = Integer.parseInt(allvalues[1]);
                    Integer sillas = Integer.parseInt(allvalues[2]);
                    Integer sillones = Integer.parseInt(allvalues[3]);
                    Integer usuario = Integer.parseInt(allvalues[4]);

                    // Inserta los datos en la base de datos SQLite
                    saveToDatabase(mesas, sillas, sillones, camas, usuario, verified);

                } else {
                    System.out.println("Demasiados intentos");
                }

                // Cierra los flujos y el socket
                output.close();
                input.close();
                socket.close();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static boolean verificaFirmaDigital(String message, byte[] firma2, String str_firma) throws InvalidKeySpecException {
        boolean res = false;

        try {
            PublicKey publicKey = getPublicKey();
            Signature firma = Signature.getInstance("SHA256withRSA");
            firma.initVerify(publicKey);
            firma.update("1234".getBytes());
            res = firma.verify(firma2);
            System.out.println(res);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static PublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String pk = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvvDZHoi0VNfb8uWI+u2Tp/qvP76Gst/ZCDTDueAUl1c1slBd43Wk2t/WSwbkoQp2Gqk2v0/3f5rK7N4pJ0oTkh2QC0tqShxWLfhWy8mH1z4DGXET5jKJBYgxhOJmPMl9ptJDPSIexd5tKoaNrwHX/K2NMn5LyPAPNRK+K8/+7s/4/MQ7dFKVMBDOvzMdB3rYSuYP149Woz+O9ja8qRCO1NTkHHz8v+M8CfLYe8zsyVgXpsTZclWUa1H6lPBjDa9t4R+MAEuJxoZIbcMfg2gcOZU4Wso88mFaqe6ifAQYltIRdId4jE1X7TK1BRf3ntLpnMV7YA+TJJ82K779xzs0dQIDAQAB";
        byte[] publicBytes = Base64.decode(pk);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        System.out.println(pubKey);
        return pubKey;
    }

    public static boolean checkSobrecarga() {
        // Aquí se realiza la comprobación de sobrecarga
        return false;
    }

    public static void saveToDatabase(Integer mesas, Integer sillas, Integer sillones, Integer camas, Integer usuario, boolean verified) {
        String sql = "INSERT INTO pedidos (mesas, sillas, sillones, camas, usuario, verificado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mesas);
            pstmt.setInt(2, sillas);
            pstmt.setInt(3, sillones);
            pstmt.setInt(4, camas);
            pstmt.setInt(5, usuario);
            pstmt.setDate(6, dateTimestamp.getTime());
            pstmt.executeUpdate();
            System.out.println("Datos guardados en la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL); Statement stmt = conn.createStatement()) {
            // Crea la tabla pedidos si no existe
            String sql = "CREATE TABLE IF NOT EXISTS pedidos (\n"
                    + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                    + "    mesas INTEGER NOT NULL,\n"
                    + "    sillas INTEGER NOT NULL,\n"
                    + "    sillones INTEGER NOT NULL,\n"
                    + "    camas INTEGER NOT NULL,\n"
                    + "    usuario INTEGER NOT NULL,\n"
                    + "    fecha DATE NOT NULL\n"
                    + ")";
            stmt.execute(sql);
            System.out.println("Base de datos creada exitosamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static KeyPair getRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        return kp;
    }

}
