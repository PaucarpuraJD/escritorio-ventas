package datos;

import java.sql.*;
import database.Conexion;
import datos.interfaces.CrudPaginadoInterface;

import entidades.Articulo;
import java.util.ArrayList;
import java.util.List;

public class ArticuloDao implements CrudPaginadoInterface<Articulo> {

    private final Conexion CON;
    private PreparedStatement ps;
    private ResultSet rs;
    private boolean resp;

    public ArticuloDao() {
        CON = Conexion.getInstance();
    }

    @Override
    public List<Articulo> listar(String texto, int totalPorPagina, int numeroPagina) {

        List<Articulo> articulos = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT a.id, a.categoria_id, c.nombre as categoria_nombre, "
                    + "a.codigo, a.nombre, a.precio_venta, a.stock, a.descripcion, a.imagen, a.activo "
                    + "FROM articulo as a INNER JOIN categoria as c ON (a.categoria_id=c.id) WHERE a.nombre LIKE ? ORDER BY a.id ASC LIMIT ?,?");
            ps.setString(1, "%" + texto + "%");
            ps.setInt(2, (numeroPagina - 1) * totalPorPagina);
            ps.setInt(3, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {

                Articulo c = new Articulo();
                c.setId(rs.getInt(1));
                c.setCategoriaId(rs.getInt(2));
                c.setCategoriaNombre(rs.getString(3));
                c.setCodigo(rs.getString(4));
                c.setNombre(rs.getString(5));
                c.setPrecioVenta(rs.getDouble(6));
                c.setStock(rs.getInt(7));
                c.setDescripcion(rs.getString(8));
                c.setImagen(rs.getString(9));
                c.setActivo(rs.getBoolean(10));
                articulos.add(c);
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
        return articulos;
    }
    
    
     public List<Articulo> listarArticuloVenta(String texto, int totalPorPagina, int numeroPagina) {

        List<Articulo> articulos = new ArrayList<>();
        try {
            ps = CON.conectar().prepareStatement("SELECT a.id, a.categoria_id, c.nombre as categoria_nombre, "
                    + "a.codigo, a.nombre, a.precio_venta, a.stock, a.descripcion, a.imagen, a.activo "
                    + "FROM articulo as a INNER JOIN categoria as c ON (a.categoria_id=c.id) WHERE a.nombre LIKE ? AND a.stock > 0 AND a.activo=true ORDER BY a.id ASC LIMIT ?,?");
            ps.setString(1, "%" + texto + "%");
            ps.setInt(2, (numeroPagina - 1) * totalPorPagina);
            ps.setInt(3, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {

                Articulo c = new Articulo();
                c.setId(rs.getInt(1));
                c.setCategoriaId(rs.getInt(2));
                c.setCategoriaNombre(rs.getString(3));
                c.setCodigo(rs.getString(4));
                c.setNombre(rs.getString(5));
                c.setPrecioVenta(rs.getDouble(6));
                c.setStock(rs.getInt(7));
                c.setDescripcion(rs.getString(8));
                c.setImagen(rs.getString(9));
                c.setActivo(rs.getBoolean(10));
                articulos.add(c);
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
        return articulos;
    }
    
    public Articulo obtenerArticuloCodigoIngreso(String codigo){
        Articulo art = null;
        try{
            ps = this.CON.conectar().prepareStatement("SELECT id, codigo, nombre ,precio_venta, stock FROM articulo WHERE codigo=?");
            ps.setString(1, codigo);
            rs = ps.executeQuery();
            if(rs.next()){
                art = new Articulo(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getDouble(4) ,rs.getInt(5));
            }
            ps.close();
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            ps = null;
            rs = null;
            this.CON.desconectar();
        }
        return art;
    }
    
        public Articulo obtenerArticuloCodigoVenta(String codigo){
        Articulo art = null;
        try{
            ps = this.CON.conectar().prepareStatement("SELECT id, codigo, nombre ,precio_venta, stock FROM articulo WHERE codigo=? AND stock>0 AND  activo=true");
            ps.setString(1, codigo);
            rs = ps.executeQuery();
            if(rs.next()){
                art = new Articulo(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getDouble(4) ,rs.getInt(5));
            }
            ps.close();
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            ps = null;
            rs = null;
            this.CON.desconectar();
        }
        return art;
    }

    @Override
    public boolean insertar(Articulo obj) {

        resp = false;
        try {
            ps = CON.conectar().prepareStatement("INSERT INTO articulo(categoria_id, codigo, nombre, precio_venta,stock ,descripcion, imagen)"
                    + " VALUES(?,?,?,?,?,?,?)");
            ps.setInt(1, obj.getCategoriaId());
            ps.setString(2, obj.getCodigo());
            ps.setString(3, obj.getNombre());
            ps.setDouble(4, obj.getPrecioVenta());
            ps.setInt(5, obj.getStock());
            ps.setString(6, obj.getDescripcion());
            ps.setString(7, obj.getImagen());
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
    public boolean actualizar(Articulo obj) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE articulo SET categoria_id=?, codigo=?, nombre=?, precio_venta=? ,stock=? ,descripcion=?, imagen=? WHERE id=? ");
            ps.setInt(1, obj.getCategoriaId());
            ps.setString(2, obj.getCodigo());
            ps.setString(3, obj.getNombre());
            ps.setDouble(4, obj.getPrecioVenta());
            ps.setInt(5, obj.getStock());
            ps.setString(6, obj.getDescripcion());
            ps.setString(7, obj.getImagen());
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
            ps = CON.conectar().prepareStatement("UPDATE articulo SET activo=0 WHERE id=?");
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
            ps = CON.conectar().prepareStatement("UPDATE articulo SET activo = 1 WHERE id=?");
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
            ps = CON.conectar().prepareStatement("SELECT COUNT(id) FROM articulo");
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
            ps = CON.conectar().prepareStatement("SELECT nombre FROM articulo WHERE nombre=?");
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
