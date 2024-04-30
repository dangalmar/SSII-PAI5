
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;

public class Server {

    private static final String[] protocols = new String[]{"TLSv1.3"};
    private static final String[] cipherSuites = new String[]{"TLS_AES_128_GCM_SHA256"};

    private final SSLServerSocket serverSocket;

    public Server(int port) throws Exception {

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManager[] tM = JavaSecurityMethods.getTrustFactoryServer().getTrustManagers();
        KeyManager[] kM = JavaSecurityMethods.getKeyFactoryServer().getKeyManagers();
        context.init(kM, tM, null);

        SSLServerSocketFactory factory = context.getServerSocketFactory();
        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        serverSocket.setEnabledCipherSuites(cipherSuites);
        serverSocket.setEnabledProtocols(protocols);

    }

    public void startServer() {
        Timer timer = new Timer();
        int timeinterval = 60 * 60 * 4000;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (LocalDateTime.now().getDayOfMonth() == 1) {
                    System.out.println("Creación de informe ");
                    JavaSecurityMethods.writeTendenceFile();

                    ServerConfig.startServer(serverSocket);
                } else {
                    ServerConfig.startServer(serverSocket);
                }
            }
        }, 0, timeinterval);

    }

    public static void main(String[] args) throws Exception {

        new Server(7071).startServer();

    }
}
