import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.CertificateException;

public class RSAKeyPairGenerator {

    public static void main(String[] args) {
        String keystoreFileName = "keystore.jks";
        String alias = "mykey";
        char[] keystorePassword = "password".toCharArray();
        char[] keyPassword = "keypassword".toCharArray();

        try {
            // Generar un par de claves RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Tamaño de clave de 2048 bits
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Crear un almacén de claves y guardarlo en un archivo
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, keystorePassword);

            // Agregar la clave privada y el certificado al almacén de claves
            Certificate selfSignedCertificate = generateSelfSignedCertificate(keyPair);
            keyStore.setKeyEntry(alias, keyPair.getPrivate(), keyPassword, new Certificate[]{selfSignedCertificate});

            // Guardar el almacén de claves en un archivo
            FileOutputStream fos = new FileOutputStream(keystoreFileName);
            keyStore.store(fos, keystorePassword);
            fos.close();

            System.out.println("Par de claves RSA generado y almacenado correctamente en " + keystoreFileName);
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Certificate generateSelfSignedCertificate(KeyPair keyPair) throws CertificateException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // Generar un certificado autofirmado usando la clave pública proporcionada
        return CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(
                        new X509CertInfo(keyPair.getPublic().getEncoded())));
    }

}
