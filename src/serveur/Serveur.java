package serveur;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Serveur {

    public static String host;
    public static String identifiant;
    public static String password;
    private int port = 8952;

    private Serveur_IOT_JFrame frame;
    private ServerSocket serverSocket;
    private ArrayList<ServeurThread> threadList = new ArrayList<>();
    private InetAddress InetAddressinetAddress;
    private volatile boolean running = true;
    private Thread daemonThread;

    public Serveur() {
        Accueil_JFrame frameAccueil = new Accueil_JFrame(this);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                frameAccueil.setVisible(true);
            }
        });
    }

    public void setFrame(Serveur_IOT_JFrame frame) {
        this.frame = frame;
    }

    public void deconnexion() {
        this.running = false;
        ListIterator<ServeurThread> li = this.threadList.listIterator();
        while (li.hasNext()) {
            ServeurThread capteurThread = li.next();
            capteurThread.getCapteur().deconnecterCapteurBDD();
            String outputString = "Deconnexion " + capteurThread.getCapteur().getNom();
            this.frame.deconnecterClient(outputString);
            capteurThread.interrupt();
            this.threadList.remove(li);
        }
        System.out.println("Le serveur est déconnecté");
    }

    public void connexion() {

        //list to add all the clients thread
        try {
            this.InetAddressinetAddress = InetAddress.getByName("localhost");
            this.serverSocket = new ServerSocket(this.port);
            System.out.println("Le serveur est à l'écoute...");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getStackTrace());
        }

        this.running = true;

        this.daemonThread = new Thread(
                new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    while (running) {
                        Socket socket;
                        socket = serverSocket.accept();
                        ServeurThread serverThread = new ServeurThread(socket, threadList);
                        serverThread.setFrame(frame);
                        //starting the thread                        
                        threadList.add(serverThread);
                        serverThread.start();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Serveur.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    System.out.println("Fin demon");
                }
            }
        }, "Demon");

        this.daemonThread.setDaemon(true);
        this.daemonThread.start();
        System.out.println("Le serveur est en ligne");
    }

    public boolean isOnline() {
        return this.running;
    }

    public void envoyer(String _message) {
        //this.output.println(_message);
        //this.output.flush();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        Serveur serveur = new Serveur();
    }
}
