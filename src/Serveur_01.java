/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author bbword
 */
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Serveur_01 extends Thread {

    //socket server port on which it will listen
    private static int port = 8952;
    //static ServerSocket variable
    private ServerSocket server;
    /* Unique connecteur/socket du serveur */
    private Socket socketClient;
    private BufferedReader input;
    private PrintWriter output;

    public Serveur_01() {
    }

    public void online() {
        try {
            if (!this.isOnline()) {
                this.server = new ServerSocket(port);
                System.out.println("Le serveur est à l'écoute...");
                this.socketClient = this.server.accept();
            }
        } catch (IOException ex) {
            Logger.getLogger(Serveur_01.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean status() {
        if (this.server.isBound()) {
            System.out.println("Le serveur est connecté à un client");
        } else {
            System.out.println("Le serveur ,n'est pas connecté à un client");
        }
        return this.server.isBound();
    }

    public boolean isOnline() {
        boolean status_online;
        if (this.server != null) {
            if (this.server.isClosed()) {
                System.out.println("Le serveur n'est pas en ligne");
                status_online = false;
            } else {
                System.out.println("Le serveur est en ligne");
                status_online = true;
            }
        } else {
            status_online = false;
        }
        return status_online;
    }

    public void deconnexion() {
        try {
            if (this.isOnline()) {
                this.server.close();
                System.out.println("Le serveur est déconnecté");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connexion() {
        try {
            System.out.println("Le serveur est à l'écoute...");
            this.socketClient = this.server.accept();

            // Connection recupérée, on determine l'ip et le port
            String c_ip = this.socketClient.getInetAddress().toString();
            int c_port = this.socketClient.getPort();
            int d_port = this.socketClient.getLocalPort();
            System.out.format("client admis IP %s  sur le port %d\n", c_ip, c_port);

            // flux pour envoyer
            this.output = new PrintWriter(socketClient.getOutputStream());
            // flux pour reçevoir
            this.input = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));

            Thread receive = new Thread(new Runnable() {
                String received;

                @Override
                public void run() {
                    while (true) {
                        try {
                            received = input.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (received != null) {
                            System.out.println("Client : " + received);
                        }
                    }
                }
            });
            receive.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void envoyer(String _message) {
        this.output.println(_message);
        this.output.flush();
    }

    public ServerSocket getServer() {
        return server;
    }

    public Socket getSocketClient() {
        return socketClient;
    }

}
