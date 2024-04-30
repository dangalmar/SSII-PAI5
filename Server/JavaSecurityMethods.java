
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class JavaSecurityMethods {

    protected static String publicKey1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzvziRpETVJ+agiN8iNs/VFpe5sxemrxnogunkfsNczHqUMa7Jw+VELtrD/G1cjT7LwQt2DoJ34UHWg3S7VgVs3xAqE30/im0HzgINRQyg0/proHIlL2rYqi4kANIPPo32BIRu0mbqfF6yWQ2ye0Ol1yfEyCgL90GysRb/BZunlJdGEFDvVY+u14r2WPWjpk1a3CJYpGQ1yDsXzYZZG372+ZwSmBI/qIX4AaDKeoAj+JuMLHTXYDhEQzRsc+qZhzJ7vv9/xSuxiPcL9fjpNFRIUoUQYlSC1XZ9o5mip3+ldzpKX/p8Du0JwFKdOtbmVaZdZR13VNRl2j0PgFBFICxYwIDAQAB";
    protected static String publicKey2 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoCYI49zqOcQ+3zTCWaFduIMDgVUDECL3Ayer/GAA7DajZGuSan/RtYFz//ZhPMsGW9N/Aeq9FjbfJQSXrdfoyffmD38IqwOtlcDHUh5/nMFAb2aHP1xCYPpLvrWyBxwxewvlqkRInoDvma/lEt9EJkTCcEmGuTcfJmqHYgdU6qv8DhAfiX38Dxe3gN/VQiI+rlsl54oTsxY4Px3iqFMy1S7cAg8alPk4ms9S+btem7EOuX/BcX8r8PFLUl3Ds6+sVvCtw9G2gG8eJdY+4HTnA5q1GODDjUMJ5nTJXqb7LIVF79/vODwmRSdBEurj36cBS+2tbEg1pW7w1mehXxrN/wIDAQAB";
    protected static String publicKey3 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyZgO0jNogfzqCoEa/zDp0CaJijC+07lu9Mc8iTzY5/B5XNV+RxnRnYjFwUIDq9nugAimY0QCVk/EC6bG+LYbp8qNCH2dpK2WLWBmYGh6IMD0sSSnhTiQsptYszTbiFIc0alQmSBy3AnaQpqTpq3DNenunrFCJ2ZtX9BU0Zjrt09iqWfTnk/4/EsvVWRR9ntn9rpnB6GA50PvkI02+whHl4uldUrUXRqrRFoJ7pRVgjW1OdVyOk/WTfhSMypcv1SkpoO6w2dj+rRe/DNT1SQGVogIcC2kEAyntbUai+3r5/Cl/kY4soUHZZnNbUVkiUblUOxpmG3R0h0xY8wall6p/wIDAQAB";

    public static RSAPublicKey getPublicKey(Integer user) throws Exception {
        String publicKey = "";
        switch (user) {
            case (0) -> {
                publicKey = publicKey1;
                System.out.println("Petición del usuario 1");
            }
            case (1) -> {
                publicKey = publicKey2;
                System.out.println("Petición del usuario 2");
            }
            case (2) -> {
                publicKey = publicKey3;
                System.out.println("Petición del usuario 3");
            }
        }

        KeyFactory kf = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

        return pubKey;
    }

    public static TrustManagerFactory getTrustFactoryClient() throws Exception {

        KeyStore tKeyStore = KeyStore.getInstance("JKS");
        tKeyStore.load(new FileInputStream("certs/server/cacerts.jks"), "changeit".toCharArray());

        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(tKeyStore);

        return trustFactory;
    }

    public static KeyManagerFactory getKeyFactoryClient() throws Exception {

        KeyStore kS = KeyStore.getInstance("JKS");
        kS.load(new FileInputStream("certs/client/keystore2.jks"), "changeit".toCharArray());

        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(kS, "changeit".toCharArray());

        return keyFactory;
    }

    public static KeyManagerFactory getKeyFactoryServer() throws Exception {
        KeyStore kS = KeyStore.getInstance("JKS");
        kS.load(new FileInputStream("certs/server/server.jks"), "changeit".toCharArray());

        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(kS, "changeit".toCharArray());

        return keyFactory;
    }

    public static TrustManagerFactory getTrustFactoryServer() throws Exception {

        KeyStore tKeyStore = KeyStore.getInstance("JKS");
        tKeyStore.load(new FileInputStream("certs/server/cacertsserverjksder.jks"), "changeit".toCharArray());

        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(tKeyStore);

        return trustFactory;
    }

    public static Boolean checkDataReceived(String data) throws Exception {
        System.out.println("Data received");
        String[] dataSplit = data.split(",");
        String[] values = dataSplit[0].split("-");
        Integer sabanas = Integer.valueOf(values[0]);
        Integer camas = Integer.valueOf(values[1]);
        Integer sillas = Integer.valueOf(values[2]);
        Integer mesas = Integer.valueOf(values[3]);
        String message = dataSplit[0];
        String firm = dataSplit[1];
        Integer user = Integer.valueOf(dataSplit[2]);
        if (sabanas <= 0 || camas <= 0 || sillas <= 0 || mesas <= 0
                || sabanas > 300 || camas > 300 || sillas > 300 || mesas > 300) {
            // Call to error
            System.out.println("Se han recibido valores incorrectos, petición denegada");
            writeIntermediateFile("No");
            writeTransactionsFile(dataSplit[0] + "NOTOKEY");
            return false;
        } else {
            // byte[] bytesPublicKey = Base64.getDecoder().decode(dataSplit[2].getBytes());
            byte[] bytesFirma = Base64.getDecoder().decode(firm.getBytes());
            RSAPublicKey publicK = getPublicKey(user);
            Signature sg = Signature.getInstance("SHA256withRSA");
            sg.initVerify(publicK);
            sg.update(message.getBytes());
            Boolean result = sg.verify(bytesFirma);
            System.out.println(result);
            writeIntermediateFile("OK");
            writeTransactionsFile(dataSplit[0] + "OK");
            return true;
        }
    }

    public static void writeIntermediateFile(String state) {
        BufferedReader objReader;
        String strCurrentLine;
        try {
            objReader = new BufferedReader(new FileReader("./Server/log.txt"));
            if ("OK".equals(state)) {
                Integer total = 0;
                Integer acert = 0;
                Integer i = 0;
                while ((strCurrentLine = objReader.readLine()) != null) {
                    if (i == 0) {
                        total = Integer.valueOf(strCurrentLine);
                    } else if (i == 1) {
                        acert = Integer.valueOf(strCurrentLine);
                    }
                    i++;
                }
                total = total + 1;
                acert = acert + 1;
                List<String> lines_ = new ArrayList<>();
                lines_.add(String.valueOf(total));
                lines_.add(String.valueOf(acert));
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/log.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines_) {
                        out.write(line);
                        out.newLine();
                    }
                }

            } else {
                Integer total = 0;
                Integer acert = 0;
                Integer i = 0;
                while ((strCurrentLine = objReader.readLine()) != null) {
                    if (i == 0) {
                        total = Integer.valueOf(strCurrentLine.trim());
                    } else if (i == 1) {
                        acert = Integer.valueOf(strCurrentLine.trim());
                    }
                    i++;
                }
                total = total + 1;
                List<String> lines_ = new ArrayList<>();
                lines_.add(String.valueOf(total));
                lines_.add(String.valueOf(acert));
                System.out.println(lines_);
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/log.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines_) {
                        out.write(line);
                        out.newLine();
                    }
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block

        }

    }

    public static void writeTransactionsFile(String transaction) {
        BufferedReader objReader = null;

        try {
            objReader = new BufferedReader(new FileReader("./Server/transactions.txt"));
            final List<String> lines = Files.lines(Paths.get("./Server/transactions.txt"))
                    .collect(Collectors.toList());
            lines.add(Math.min(lines.size(), lines.size()), transaction);
            try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/transactions.txt"),
                    Charset.forName("UTF-8"))) {
                for (final String line : lines) {
                    out.append(line).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void writeTendenceFile() {
        BufferedReader objReader = null;

        String strCurrentLine;

        try {
            objReader = new BufferedReader(new FileReader("./Server/tendences.txt"));
            Integer i = 0;
            while ((strCurrentLine = objReader.readLine()) != null) {
                i++;
            }
            objReader = new BufferedReader(new FileReader("./Server/log.txt"));
            float total = 0;
            float acert = 0;
            Integer j = 0;
            while ((strCurrentLine = objReader.readLine()) != null) {
                if (j == 0) {
                    total = Float.valueOf(strCurrentLine);
                } else if (j == 1) {
                    acert = Float.valueOf(strCurrentLine);
                }
                j++;
            }
            objReader = new BufferedReader(new FileReader("./Server/tendences.txt"));
            float ratio_ = acert / total;
            if (i < 2) {
                final List<String> lines = Files.lines(Paths.get("./Server/tendences.txt"))
                        .collect(Collectors.toList());
                Month mes = LocalDateTime.now().getMonth();
                int anyo = LocalDateTime.now().getYear();
                String result = mes.name() + "," + String.valueOf(anyo) + "," + String.valueOf(ratio_) + "," + "0";
                lines.add(Math.min(lines.size(), lines.size()), result);
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/tendences.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines) {
                        out.append(line).append(System.lineSeparator());
                    }
                }
                final List<String> lines_ = Files.lines(Paths.get("./Server/log.txt")).collect(Collectors.toList());

                lines_.add(Math.min(1, lines.size()), "0");
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/log.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines_) {
                        out.append(line).append(System.lineSeparator());
                    }
                }

                final List<String> lines__ = Files.lines(Paths.get("./Server/log.txt"))
                        .collect(Collectors.toList());

                lines__.add(Math.min(2, lines.size()), "0");
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/log.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines__) {
                        out.append(line).append(System.lineSeparator());
                    }
                }

            } else {
                final List<String> lines = Files.lines(Paths.get("./Server/tendences.txt"))
                        .collect(Collectors.toList());
                Month mes = LocalDateTime.now().getMonth();
                int anyo = LocalDateTime.now().getYear();
                objReader = new BufferedReader(new FileReader("./Server/tendences.txt"));
                List<String> linesList = new ArrayList<>();
                while ((strCurrentLine = objReader.readLine()) != null) {
                    linesList.add(strCurrentLine.split(",")[2]);
                }
                float pastMonth = Float.valueOf(linesList.get(linesList.size() - 1));
                float past2Month = Float.valueOf(linesList.get(linesList.size() - 2));
                String tende = "";
                if ((pastMonth < ratio_ || past2Month < ratio_) || ((pastMonth == ratio_ || past2Month < ratio_))
                        || (pastMonth < ratio_ || past2Month == ratio_)) {
                    tende += "+";
                } else if (pastMonth > ratio_ || past2Month > ratio_) {
                    tende += "-";
                } else if (pastMonth == ratio_ && past2Month == ratio_) {
                    tende += "0";

                }

                String result = mes.name() + "," + String.valueOf(anyo) + "," + String.valueOf(ratio_) + "," + tende;
                lines.add(Math.min(lines.size(), lines.size()), result);
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/tendences.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines) {
                        out.append(line).append(System.lineSeparator());
                    }
                }

                final List<String> lines_ = Files.lines(Paths.get("./Server/log.txt")).collect(Collectors.toList());

                lines_.add(Math.min(1, lines.size()), "0");
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./log.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines_) {
                        out.append(line).append(System.lineSeparator());
                    }
                }

                final List<String> lines__ = Files.lines(Paths.get("./Server/log.txt"))
                        .collect(Collectors.toList());

                lines__.add(Math.min(2, lines.size()), "0");
                try (final BufferedWriter out = Files.newBufferedWriter(Paths.get("./Server/log.txt"),
                        Charset.forName("UTF-8"))) {
                    for (final String line : lines__) {
                        out.append(line).append(System.lineSeparator());
                    }
                }

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block

        }

    }
}
