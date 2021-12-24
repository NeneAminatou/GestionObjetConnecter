/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serveur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

/**
 *
 * @author valen
 */
public class SQLDatabaseConnection {

    private String url;
    private String identifiant;
    private String password;
    private Connection connect = null;
    public static Statement statement = null;
    public static boolean isOnline = false;

    public SQLDatabaseConnection() throws ClassNotFoundException, SQLException {

        this.url = "jdbc:mariadb://" + Serveur.host;
        this.identifiant = Serveur.identifiant;
        this.password = Serveur.password;

        Class.forName("org.mariadb.jdbc.Driver");

        this.connect = DriverManager.getConnection(this.url, this.identifiant, this.password);
        SQLDatabaseConnection.statement = connect.createStatement();

        if (!this.isExistTable("neocapteurs")) {
            this.createDatabase();
        }
        this.connect.close();
        this.connect = DriverManager.getConnection(this.url + "/neocapteurs", this.identifiant, this.password);
        SQLDatabaseConnection.statement = connect.createStatement();
        SQLDatabaseConnection.isOnline = true;
    }

    public static Statement getStatement() {
        return statement;
    }

    private boolean isExistTable(String _database) throws SQLException {
        boolean trouve = false;
        String sql = "USE " + _database + " IF EXIST '" + _database + "';";
        ResultSet resultSet = this.connect.getMetaData().getCatalogs();
        while (resultSet.next()) {
            String nomBD = resultSet.getString(1);
            if (nomBD.equals(_database)) {
                trouve = true;
            }
        }
        return trouve;
    }

    private void createDatabase() throws SQLException {
        String sql
                = "CREATE DATABASE neocapteurs;"
                + "USE neocapteurs;"
                + "CREATE TABLE capteurs"
                + "(capteurID VARCHAR(200) NOT NULL, "
                + "nom VARCHAR(20),"
                + "fluide VARCHAR(15),"
                + "batiment VARCHAR(5),"
                + "etage DOUBLE NOT NULL,"
                + "salle VARCHAR(20),"
                + "tresholdMin DOUBLE NOT NULL,"
                + "tresholdMax DOUBLE NOT NULL,"
                + "measureUnit VARCHAR(20),"
                + "PRIMARY KEY (capteurID));"
                + "CREATE TABLE datas_capteur"
                + "(datasID INT NOT NULL AUTO_INCREMENT,"
                + "capteurID VARCHAR(200) NOT NULL,"
                + "datas double NOT NULL,"
                + "date TIMESTAMP NOT  NULL,"
                + "PRIMARY KEY (datasID),"
                + "CONSTRAINT FK_capteurID_data FOREIGN KEY (capteurID) "
                + "REFERENCES capteurs(capteurID));"
                + "CREATE TABLE co_deco_capteur"
                + "(co_decoID INT NOT NULL AUTO_INCREMENT,"
                + "capteurID VARCHAR(200) NOT NULL,"
                + "connect TIMESTAMP NOT NULL,"
                + "deconnect TIMESTAMP NULL,"
                + "PRIMARY KEY (co_decoID),"
                + "CONSTRAINT FK_capteurID_coDeco FOREIGN KEY (capteurID) "
                + "REFERENCES capteurs(capteurID));";

        String[] percept = sql.split(";");

        for (String s : percept) {
            System.out.println(s + " : ");
            int res = SQLDatabaseConnection.statement.executeUpdate(s + ";");
            System.out.println(res);
        }
    }

}
