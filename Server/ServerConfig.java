
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerConfig {

    public static void startServer(final ServerSocket conexion) {
        System.out.println("Iniciando....");
        new Thread() {

            public void run() {
                Integer receivedCalls = 1;
                try {

                    while (true) {
                        Socket accepted = conexion.accept();
                        if (receivedCalls > 3) {
                            System.out.println("El servidor ha recibido más de 3 peticiones en 4 horas, cerrando conexión");
                            break;
                        }
                        accepted.setSoLinger(true, 1000);
                        System.out.println("Conectado con ... " + accepted.getInetAddress().getHostAddress());
                        BufferedReader acceptedInput = new BufferedReader(new InputStreamReader(
                                accepted.getInputStream()));
                        PrintWriter acceptedOutput = new PrintWriter(
                                new OutputStreamWriter(accepted.getOutputStream()));
                        String received = acceptedInput.readLine();

                        /* String password = acceptedInput.readLine(); */
                        System.out.println(received);
                        /* System.out.println(password); */
                        Boolean isCorrect = JavaSecurityMethods.checkDataReceived(received);
                        if (isCorrect) {
                            acceptedOutput.println("Peticion OK");
                            receivedCalls += 1;
                        } else {
                            acceptedOutput.println("Peticion INCORRECTA");
                            receivedCalls += 1;
                        }

                        // output.flush();
                        acceptedOutput.close();
                        acceptedInput.close();
                        accepted.close();

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }
}
