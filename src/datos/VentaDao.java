package datos;


import java.sql.Connection;
import database.Conexion;
import datos.interfaces.CrudVentaInterface;
import entidades.DetalleVenta;
import entidades.Venta;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VentaDao implements CrudVentaInterface<Venta, DetalleVenta> {

    private final Conexion CON;
    private PreparedStatement ps;
    private ResultSet rs;
    private boolean resp;

    public VentaDao() {
        this.CON = Conexion.getInstance();
    }

    @Override
    public List<Venta> listar(String texto, int totalPorPagina, int numPagina) {
        List<Venta> registros = new ArrayList<>();
        String sql = "SELECT v.id , v.usuario_id, u.nombre as usuario_nombre, v.persona_id, p.nombre as persona_nombre, v.tipo_comprobante, v.serie_comprobante, v.num_comprobante, v.fecha, v.impuesto, v.total, v.estado FROM venta as v INNER JOIN "
                + "persona as p ON (v.persona_id=p.id) INNER JOIN usuario as u ON (v.usuario_id=u.id) WHERE v.num_comprobante LIKE ? ORDER BY v.id ASC LIMIT ?, ?;";
        try {
            ps = this.CON.conectar().prepareStatement(sql);
            ps.setString(1, "%" + texto + "%");
            ps.setInt(2, (numPagina - 1) * totalPorPagina);
            ps.setInt(3, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {
                Venta i = new Venta();
                i.setId(rs.getInt(1));
                i.setUsuarioId(rs.getInt(2));
                i.setUsuarioNombre(rs.getString(3));
                i.setPersonaId(rs.getInt(4));
                i.setPersonaNombre(rs.getString(5));
                i.setTipoComprobante(rs.getString(6));
                i.setSerieComprobante(rs.getString(7));
                i.setNumComprobante(rs.getString(8));
                i.setFecha(rs.getDate(9));
                i.setImpuesto(rs.getDouble(10));
                i.setTotal(rs.getDouble(11));
                i.setEstado(rs.getString(12));
                

                registros.add(i);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            rs = null;
            this.CON.desconectar();
        }
        return registros;
    }

    @Override
    public List<DetalleVenta> listarDetalle(int id) {
        List<DetalleVenta> registros = new ArrayList<>();
        try{
            ps = CON.conectar().prepareStatement("SELECT a.id, a.codigo, a.nombre, a.stock, d.cantidad, d.precio, d.descuento,((d.cantidad*precio)-d.descuento) as sub_total FROM detalle_venta as d INNER JOIN articulo as a ON d.articulo_id=a.id WHERE d.venta_id=?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while(rs.next()){
                DetalleVenta d = new DetalleVenta();
                d.setArticuloId(rs.getInt(1));
                d.setArticuloCodigo(rs.getString(2));
                d.setArticuloNombre(rs.getString(3));
                d.setArticuloStock(rs.getInt(4));
                d.setCantidad(rs.getInt(5));
                d.setPrecio(rs.getDouble(6));
                d.setDescuento(rs.getDouble(7));
                d.setSubTotal(rs.getDouble(6));
                registros.add(d);
                
            }
            ps.close();
            rs.close();
        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            ps = null;
            rs = null;
            CON.desconectar();
        }
        return registros;
    }

    @Override
    public boolean insertar(Venta obj) {
        resp=false;
        Connection conn=null;
        try {
            conn=CON.conectar();
            conn.setAutoCommit(false);
            String sqlInsertVenta="INSERT INTO venta (persona_id,usuario_id,fecha,tipo_comprobante,serie_comprobante,num_comprobante,impuesto,total,estado) VALUES (?,?,now(),?,?,?,?,?,?)";
            
            ps=conn.prepareStatement(sqlInsertVenta,Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1,obj.getPersonaId());
            ps.setInt(2, obj.getUsuarioId());
            ps.setString(3,obj.getTipoComprobante());
            ps.setString(4, obj.getSerieComprobante());
            ps.setString(5, obj.getNumComprobante());
            ps.setDouble(6, obj.getImpuesto());
            ps.setDouble(7, obj.getTotal());
            ps.setString(8, "Aceptado");
            
            int filasAfectadas=ps.executeUpdate();
            rs=ps.getGeneratedKeys();
            int idGenerado=0;
            if (rs.next()){
                idGenerado=rs.getInt(1);
            }
            
            if (filasAfectadas==1){
                String sqlInsertDetalle="INSERT INTO detalle_venta (venta_id,articulo_id,cantidad,precio,descuento) VALUES (?,?,?,?,?)";
                ps=conn.prepareStatement(sqlInsertDetalle);
                for (DetalleVenta item : obj.getDetalles()){
                    ps.setInt(1,idGenerado);
                    ps.setInt(2,item.getArticuloId());
                    ps.setInt(3, item.getCantidad());
                    ps.setDouble(4, item.getPrecio());
                    ps.setDouble(5, item.getDescuento());
                    resp=ps.executeUpdate()>0;
                }
                conn.commit();                
            }else{
                conn.rollback();
            }
        }  catch (SQLException e) {
            try {
                if (conn!=null){
                    conn.rollback();
                }
                e.printStackTrace();
            } catch (SQLException ex) {
                Logger.getLogger(VentaDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally{
            try {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                if (conn!=null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(VentaDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resp;
    }

    @Override
    public boolean anular(int id) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE venta SET estado='Anulado' WHERE id=?");
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0) {
                resp = true;
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            CON.conectar();
        }
        return resp;
    }
    
    public String ultimoSerie(String tipoComprobante) {
        String serieComprobante="";
        try {
            ps=CON.conectar().prepareStatement("SELECT serie_comprobante FROM venta where tipo_comprobante=? order by serie_comprobante desc limit 1");            
            ps.setString(1, tipoComprobante);
            rs=ps.executeQuery();
            
            while(rs.next()){
                serieComprobante=rs.getString("serie_comprobante");
            }            
            ps.close();
            rs.close();
        }  catch (SQLException e) {
            e.printStackTrace();
        } finally{
            ps=null;
            rs=null;
            CON.desconectar();
        }
        return serieComprobante;
    }
    
    public String ultimoNumero(String tipoComprobante,String serieComprobante) {
        String numComprobante="";
        try {
            ps=CON.conectar().prepareStatement("SELECT num_comprobante FROM venta WHERE tipo_comprobante=? AND serie_comprobante=? order by num_comprobante desc limit 1");            
            ps.setString(1, tipoComprobante);
            ps.setString(2, serieComprobante);
            rs=ps.executeQuery();
            
            while(rs.next()){
                numComprobante=rs.getString("num_comprobante");
            }            
            ps.close();
            rs.close();
        }  catch (SQLException e) {
            e.printStackTrace();
        } finally{
            ps=null;
            rs=null;
            CON.desconectar();
        }
        return numComprobante;
    }

    @Override
    public int total() {
        int totalRegistros = 0;
        try {
            ps = CON.conectar().prepareStatement("SELECT COUNT(id) FROM venta");
            rs = ps.executeQuery();
            while (rs.next()) {
                totalRegistros = rs.getInt("COUNT(id)");
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ps = null;
            CON.desconectar();
        }

        return totalRegistros;
    }

    @Override
    public boolean existe(String texto, String texto2) {
        resp = false;

        try {
            ps = CON.conectar().prepareStatement("SELECT id FROM venta WHERE serie_comprobante=? AND num_comprobante=?");
            ps.setString(1, texto);
            ps.setString(2, texto2);
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
    
    public List<Venta> consultaFechas(Date fechaInicio, Date fechaFin) {
        List<Venta> registros=new ArrayList<>();
        try {
            ps=CON.conectar().prepareStatement("SELECT v.id,v.usuario_id,u.nombre as usuario_nombre,v.persona_id,p.nombre as persona_nombre,v.tipo_comprobante,v.serie_comprobante,v.num_comprobante,v.fecha,v.impuesto,v.total,v.estado FROM venta v INNER JOIN persona p ON v.persona_id=p.id INNER JOIN usuario u ON v.usuario_id=u.id WHERE v.fecha>=? AND v.fecha<=?");
            ps.setDate(1,fechaInicio);            
            ps.setDate(2,fechaFin);
            rs=ps.executeQuery();
            while(rs.next()){
                registros.add(new Venta(rs.getInt(1),rs.getInt(2),rs.getString(3),rs.getInt(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getDate(9),rs.getDouble(10),rs.getDouble(11),rs.getString(12)));
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        } finally{
            ps=null;
            rs=null;
            CON.desconectar();
        }
        return registros;
    }

}
