package datos;

import database.Conexion;
import entidades.Rol;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDao {

    private final Conexion CON;
    private PreparedStatement ps;
    private ResultSet rs;
    private boolean resp;

    public RolDao() {
        this.CON =Conexion.getInstance();
    }
    
    

    public List<Rol> listar() {

        List<Rol> roles = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT * FROM rol");
            rs = ps.executeQuery();
            while (rs.next()) {

               roles.add(new Rol(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            rs = null;
            CON.desconectar();
        }
        return roles;
    }

    public List<Rol> seleccionar() {

         List<Rol> roles = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT id, nombre FROM rol ORDER BY nombre ASC");
            rs = ps.executeQuery();
            while (rs.next()) {

                roles.add(new Rol(rs.getInt(1), rs.getString(2)));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            rs = null;
            CON.desconectar();
        }
        return roles;
    }

    public int total() {
        int totalResgistro = 0;
        try {
            ps = CON.conectar().prepareStatement("SELECT COUNT(id) FROM rol");
            rs = ps.executeQuery();
            while (rs.next()) {
                totalResgistro = rs.getInt("COUNT(id)");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            rs = null;
            CON.desconectar();
        }
        return totalResgistro;
    }
}
