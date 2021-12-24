/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author valen
 */
public class Capteur {
    private String nom;
    private String capteurID;
    private String batiment;
    private String etage;
    private String Fluide;
    private String salle;
    private double tresholdMax;
    private double tresholdMin;    
    private String measureUnit;
    
    public Capteur(String _outputString) {
        //tabLine stocke les informations du capteur au format du tableau cf. panneau gestion des capteurs
        Object[] tabLine = parsingDataCapteurToTab(_outputString);
        this.nom = (String)tabLine[0];
        this.Fluide = (String)tabLine[1];
        this.batiment = (String)tabLine[2];
        this.etage = (String)tabLine[3];
        this.salle = (String)tabLine[4];
        this.capteurID = this.nom + "-" + this.Fluide + "-" + this.batiment + "-" +  this.etage + "-" + this.salle;
        this.defineTreshold();
    }

    public double getTresholdMax() {
        return tresholdMax;
    }

    public String getMeasureUnit() {
        return measureUnit;
    }    

    public String getNom() {
        return nom;
    }

    public String getCapteurID() {
        return capteurID;
    }

    public String getBatiment() {
        return batiment;
    }

    public String getEtage() {
        return etage;
    }

    public String getFluide() {
        return Fluide;
    }

    public String getSalle() {
        return salle;
    }
    
    //Retourne la valeur initale du capteur en fonctiopn de son fluide
    //les résultats seront retourné sous la forme d'un tuple : (seuil min, seuil max); 
    private void defineTreshold() {
        switch (this.Fluide) {
            case "EAU" :
                this.tresholdMin = 0.0;
                this.tresholdMax = 10.0;
                this.measureUnit = "m³";
            case "ELECTRICITE" :
                this.tresholdMin = 10.0;
                this.tresholdMax = 500.0;
                this.measureUnit = "kWh";
            case "TEMPERATURE" :
                this.tresholdMin = 17.0;
                this.tresholdMax = 22.0;
                this.measureUnit = "°C";
            case "AIR COMPRIME" :
                this.tresholdMin = 0.0;
                this.tresholdMax = 5.0;
                this.measureUnit = "m³/h";
        }
    }    
    
    
    public void ajouterCapteurBDD() {
        try {
           
 
            String sql = "SELECT capteurID FROM capteurs WHERE capteurID = '" + this.capteurID + "';";
            Statement st = SQLDatabaseConnection.getStatement(); 
            ResultSet rs = st.executeQuery(sql);
            if (rs.next() == false) {
                //--------------------- AJOUT CAPTEUR -------------------------
                    sql = "INSERT INTO capteurs(capteurID, nom, fluide, batiment, etage, salle, tresholdMin, tresholdMax, measureUnit) VALUES (" 
                            + "'" + this.capteurID + "'," 
                            + "'" + this.nom + "',"
                            + "'" + this.Fluide + "',"
                            + "'" + this.batiment + "',"
                            + "'" + this.etage + "',"
                            + "'" + this.salle + "',"
                            + "'" + this.tresholdMin + "',"
                            + "'" + this.tresholdMax + "',"
                            + "'" + this.measureUnit + "'"                             
                            + ");";
                    int resInsert = st.executeUpdate(sql);
                    rs.close();
            }       
            //--------------------- AJOUT CONNEXION -------------------------
                sql = "INSERT INTO co_deco_capteur(connect, capteurID) VALUES ("                         
                        + "NOW(),"
                        + "'" + this.capteurID + "'"
                        + ");";
                System.out.println("SQL = " + sql);
                int resInsert = st.executeUpdate(sql);
                
        } catch (SQLException ex) {
            Logger.getLogger(Capteur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void deconnecterCapteurBDD() {

        try {
                Statement st = SQLDatabaseConnection.getStatement();
                //------------------- DECONNEXION -------------------------                
                String sql = "UPDATE co_deco_capteur SET deconnect = NOW()"
                        + "WHERE capteurID = '" + this.capteurID + "' AND deconnect IS NULL;";                        
                System.out.println("SQL = " + sql);
                int resUpadate = st.executeUpdate(sql);
                //-------------------------------------------------------------
                
        } catch (SQLException ex) {
            Logger.getLogger(Capteur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void donneeCapteurBDD(String value) {
        try {
            Statement st = SQLDatabaseConnection.getStatement();
            //------------------- INSERT DATAS ------------------------- 
            String sql = "INSERT INTO datas_capteur(datas, date, capteurID) VALUES (" 
                        + "'" + value + "', "
                        + "NOW(), "
                        + "'" + this.capteurID + "'" 
                        + ");";
            int resUpdate = st.executeUpdate(sql);
            //-------------------------------------------------------------
        } catch (SQLException ex) {
            Logger.getLogger(Capteur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    //retourne la date de connection du capteur (this) au format yyyy-MM-dd HH:mm:ss
    private String dateActuelle() {
        DateFormat pattern = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        Date dateDeCreation = new Date();
        return pattern.format(dateDeCreation);
    }
    
    //getter sur la ligne modèle tableau
    public Object[] getRow(){
        Object[] row = new Object[6];
        row[0] = this.nom;
        row[1] = this.Fluide;
        row[2] = this.batiment;
        row[3] = this.etage;
        row[4] = this.salle;
        //La 6ème valeur correspond à la valeur du capteur qui n'est pas renseigné avec le message de connexion donc laissé vide par cette méthode
        row[5] = "";
        return row;
    }
    
    //Cette fonction me permet de découper les informations du capteur
    //et de les mettre sous forme d'une ligne de mon tableau 
    public Object[] parsingDataCapteurToTab(String _outputString) {
        Object[] row = new Object[6];
        String[] percept = _outputString.split(" ");
        String[] capteurDatas;
        if (percept.length > 0) {
            //percept[0] = type du message (connexion, données...) et percept[1] = nom du capteur
            row[0] = percept[1];
            //format type:batiment:etage:lieu donc j'utilise un split : pour séparer les champs
            capteurDatas = percept[2].split(":");
            row[1] = capteurDatas[0];
            row[2] = capteurDatas[1];
            row[3] = capteurDatas[2];
            row[4] = capteurDatas[3];
            //La 6ème valeur correspond à la valeur du capteur qui n'est pas renseigné avec le message de connexion donc laissé vide
            row[5] = "";
        }
        return row;
    }    
    
    //génère un ID unique pour le capteur
    private String createID(String[] _ID) {
        return (_ID[0]+ "-" + _ID[1] + "-" + _ID[2] + "-" + _ID[3] + "-" + _ID[4]);
    } 
}
