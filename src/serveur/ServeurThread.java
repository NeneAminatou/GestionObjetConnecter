package serveur;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Statement;
import java.util.ArrayList;


public class ServeurThread extends Thread {
    private Socket socket;
    private ArrayList<ServeurThread> threadList;
    private PrintWriter output;
    private Serveur_IOT_JFrame frame;
    private int port;
    private Statement statement;
    Capteur capteur;
    
    public ServeurThread(Socket socket, ArrayList<ServeurThread> threads) {
        this.socket = socket;
        this.threadList = threads;
    }

    public void setFrame(Serveur_IOT_JFrame frame) {
        this.frame = frame;
    }

    public Capteur getCapteur() {
        return capteur;
    }
        
    @Override
    public void run() {
        try {
            //Reading the input from Client
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            
            //returning the output to the client : true statement is to flush the buffer otherwise
            //we have to do it manuallyy
             output = new PrintWriter(socket.getOutputStream(),true);


            //inifite loop for server
            while(true) {
                String outputString = input.readLine();
                //if user types exit command
                if(outputString.equals("exit")) {
                    break;
                }
                printToALlClients(outputString);
                //output.println("Server says " + outputString);
                System.out.println("Server received " + outputString);
                /*
                capteur = new Capteur(outputString);
                this.capteur.taskManager(outputString);
                */
                String[] percept = outputString.split(" ");        
                if (percept.length > 0){
                    switch (percept[0]) {
                        case "Connexion":
                            capteur = new Capteur(outputString);
                            this.capteur.ajouterCapteurBDD();
                            frame.ajouterCapteur(capteur.getRow());
                            break;
                        case "Deconnexion":
                            this.capteur.deconnecterCapteurBDD();
                            frame.deconnecterClient(outputString);
                            break;
                        case "Donnee":
                            this.capteur.donneeCapteurBDD(percept[2]);
                            //boolean res = this.capteur.isOutOfLimit();
                            frame.donneeClient(percept[2], percept[1], true);
                            
                            
                            break;
                        default:
                            break;
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Error occured " +e.getStackTrace());
        }
    }

    private void printToALlClients(String outputString) {
        for( ServeurThread sT: threadList) {
            sT.output.println(outputString);
        }

    }
}
