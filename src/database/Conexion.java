package database;

import java.sql.*;

public class Conexion {

    private static Connection conexion;
    private static Conexion instancia;

    private static final String URL = "jdbc:mysql://localhost:3306/db_sistema?characterEncoding=utf8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1234";
    
    private Conexion(){};

    public Connection conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Conexion exitosa");
            return conexion;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("No se pudo conectar");
        }

        return conexion;
    }

    public void desconectar() {
        try {
            conexion.close();
            System.out.println("Cerro Conexion");
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
    
    public synchronized static Conexion getInstance(){
        if(instancia == null){
            instancia = new Conexion();
        }
        
        return instancia;
    }

}
