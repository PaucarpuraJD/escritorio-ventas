package datos;

import java.sql.*;
import database.Conexion;
import datos.interfaces.CrudPaginadoInterface;

import entidades.Persona;
import java.util.ArrayList;
import java.util.List;

public class PersonaDao implements CrudPaginadoInterface<Persona> {

    private final Conexion CON;
    private PreparedStatement ps;
    private ResultSet rs;
    private boolean resp;

    public PersonaDao() {
        CON = Conexion.getInstance();
    }

    @Override
    public List<Persona> listar(String texto, int totalPorPagina, int numeroPagina) {

        List<Persona> personas = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT * FROM persona WHERE nombre LIKE ? ORDER BY id ASC LIMIT ?,?");
            ps.setString(1, "%" + texto + "%");
            ps.setInt(2, (numeroPagina - 1) * totalPorPagina);
            ps.setInt(3, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {

                Persona p = new Persona();
                p.setId(rs.getInt(1));
                p.setTipoPersona(rs.getString(2));
                p.setNombre(rs.getString(3));
                p.setTipoDocumento(rs.getString(4));
                p.setNumDocumento(rs.getString(5));
                p.setDireccion(rs.getString(6));
                p.setTelefono(rs.getString(7));
                p.setEmail(rs.getString(8));
                p.setActivo(rs.getBoolean(9));
                personas.add(p);
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
        return personas;
    }

        public List<Persona> listarTipo(String texto, int totalPorPagina, int numeroPagina, String tipoPersona) {

        List<Persona> personas = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT * FROM persona WHERE nombre LIKE ?  AND tipo_persona=? ORDER BY id ASC LIMIT ?,?");
            ps.setString(1, "%" + texto + "%");
            ps.setString(2, tipoPersona);
            ps.setInt(3, (numeroPagina - 1) * totalPorPagina);
            ps.setInt(4, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {

                Persona p = new Persona();
                p.setId(rs.getInt(1));
                p.setTipoPersona(rs.getString(2));
                p.setNombre(rs.getString(3));
                p.setTipoDocumento(rs.getString(4));
                p.setNumDocumento(rs.getString(5));
                p.setDireccion(rs.getString(6));
                p.setTelefono(rs.getString(7));
                p.setEmail(rs.getString(8));
                p.setActivo(rs.getBoolean(9));
                personas.add(p);
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
        return personas;
    }
    
    @Override
    public boolean insertar(Persona obj) {

        resp = false;
        try {
            ps = CON.conectar().prepareStatement("INSERT INTO persona(tipo_persona, nombre, tipo_documento, num_documento, direccion, telefono, email)"
                    + " VALUES(?,?,?,?,?,?,?)");
    
            ps.setString(1, obj.getTipoPersona());
            ps.setString(2, obj.getNombre());
            ps.setString(3, obj.getTipoDocumento());
            ps.setString(4, obj.getNumDocumento());
            ps.setString(5, obj.getDireccion());
            ps.setString(6, obj.getTelefono());
            ps.setString(7, obj.getEmail());
            if (ps.executeUpdate() > 0) {
                resp = true;
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            CON.desconectar();
        }
        return resp;
    }

    @Override
    public boolean actualizar(Persona obj) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE persona SET tipo_persona=?, nombre=?, tipo_documento=?, num_documento=?, direccion=?, telefono=?, email=? WHERE id=? ");
            
            ps.setString(1, obj.getTipoPersona());
            ps.setString(2, obj.getNombre());
            ps.setString(3, obj.getTipoDocumento());
            ps.setString(4, obj.getNumDocumento());
            ps.setString(5, obj.getDireccion());
            ps.setString(6, obj.getTelefono());
            ps.setString(7, obj.getEmail());
            ps.setInt(8, obj.getId());
            if (ps.executeUpdate() > 0) {
                resp = true;
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            CON.desconectar();
        }

        return resp;
    }

    @Override
    public boolean desactivar(int id) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE persona SET activo=0 WHERE id=?");
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) {
                resp = true;
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            ps = null;
            CON.desconectar();
        }
        return resp;
    }

    @Override
    public boolean activar(int id) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE persona SET activo = 1 WHERE id=?");
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) {
                resp = true;
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            ps = null;
            CON.desconectar();
        }
        return resp;
    }

    @Override
    public int total() {
        int totalResgistro = 0;
        try {
            ps = CON.conectar().prepareStatement("SELECT COUNT(id) FROM persona");
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

    @Override
    public boolean existe(String texto) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("SELECT nombre FROM persona WHERE nombre=?");
            ps.setString(1, texto);
            rs = ps.executeQuery();

            if (rs.next()) {
                resp = true;
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
        return resp;
    }

}
