package datos;

import java.sql.*;
import database.Conexion;
import datos.interfaces.CrudPaginadoInterface;

import entidades.Usuario;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao implements CrudPaginadoInterface<Usuario> {

    private final Conexion CON;
    private PreparedStatement ps;
    private ResultSet rs;
    private boolean resp;

    public UsuarioDao() {
        CON = Conexion.getInstance();
    }

    @Override
    public List<Usuario> listar(String texto, int totalPorPagina, int numeroPagina) {

        List<Usuario> usuarios = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT u.id, u.rol_id, r.nombre as rol_nombre, u.nombre, u.tipo_documento, u.num_documento, u.direccion, "
                    + " u.telefono, u.email, u.clave, u.activo "
                    + " FROM usuario as u INNER JOIN rol as r ON (u.rol_id=r.id) WHERE u.nombre LIKE ? ORDER BY u.id ASC LIMIT ?,?");
            ps.setString(1, "%" + texto + "%");
            ps.setInt(2, (numeroPagina - 1) * totalPorPagina);
            ps.setInt(3, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {

                Usuario u = new Usuario();
                u.setId(rs.getInt(1));
                u.setRolId(rs.getInt(2));
                u.setRolNombre(rs.getString(3));
                u.setNombre(rs.getString(4));
                u.setTipoDocumento(rs.getString(5));
                u.setNunDocumento(rs.getString(6));
                u.setDireccion(rs.getString(7));
                u.setTelefono(rs.getString(8));
                u.setEmail(rs.getString(9));
                u.setClave(rs.getString(10));
                u.setActivo(rs.getBoolean(11));
                usuarios.add(u);
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
        return usuarios;
    }

    public Usuario login(String email, String clave) {
        Usuario u = null;

        try {
            ps = CON.conectar().prepareStatement("SELECT u.id, u.rol_id, r.nombre as rol_nombre, u.nombre, u.tipo_documento, u.num_documento, u.direccion, "
                    + " u.telefono, u.email, u.activo "
                    + " FROM usuario as u INNER JOIN rol as r ON (u.rol_id=r.id) WHERE U.email=? AND clave=?");
            ps.setString(1, email);
            ps.setString(2, clave);
            rs = ps.executeQuery();

            if (rs.next()) {
                u = new Usuario();
                u.setId(rs.getInt(1));
                u.setRolId(rs.getInt(2));
                u.setRolNombre(rs.getString(3));
                u.setNombre(rs.getString(4));
                u.setTipoDocumento(rs.getString(5));
                u.setNunDocumento(rs.getString(6));
                u.setDireccion(rs.getString(7));
                u.setTelefono(rs.getString(8));
                u.setEmail(rs.getString(9));
                u.setActivo(rs.getBoolean(10));
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

        return u;
    }

    @Override
    public boolean insertar(Usuario obj) {

        resp = false;
        try {
            ps = CON.conectar().prepareStatement("INSERT INTO usuario(rol_id, nombre, tipo_documento, num_documento, direccion, telefono, email, clave)"
                    + " VALUES(?,?,?,?,?,?,?,?)");

            ps.setInt(1, obj.getRolId());
            ps.setString(2, obj.getNombre());
            ps.setString(3, obj.getTipoDocumento());
            ps.setString(4, obj.getNunDocumento());
            ps.setString(5, obj.getDireccion());
            ps.setString(6, obj.getTelefono());
            ps.setString(7, obj.getEmail());
            ps.setString(8, obj.getClave());
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
    public boolean actualizar(Usuario obj) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE usuario SET rol_id=?, nombre=?, tipo_documento=?, num_documento=?, direccion=?, telefono=?, email=?, clave=? WHERE id=? ");

            ps.setInt(1, obj.getRolId());
            ps.setString(2, obj.getNombre());
            ps.setString(3, obj.getTipoDocumento());
            ps.setString(4, obj.getNunDocumento());
            ps.setString(5, obj.getDireccion());
            ps.setString(6, obj.getTelefono());
            ps.setString(7, obj.getEmail());
            ps.setString(8, obj.getClave());
            ps.setInt(9, obj.getId());
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
            ps = CON.conectar().prepareStatement("UPDATE usuario SET activo=0 WHERE id=?");
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
            ps = CON.conectar().prepareStatement("UPDATE usuario SET activo = 1 WHERE id=?");
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
            ps = CON.conectar().prepareStatement("SELECT COUNT(id) FROM usuario");
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
            ps = CON.conectar().prepareStatement("SELECT email FROM usuario WHERE email=?");
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
