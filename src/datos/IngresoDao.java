package datos;


import java.sql.Connection;
import database.Conexion;
import datos.interfaces.CrudIngresoInterface;
import entidades.DetalleIngreso;
import entidades.Ingreso;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IngresoDao implements CrudIngresoInterface<Ingreso, DetalleIngreso> {

    private final Conexion CON;
    private PreparedStatement ps;
    private ResultSet rs;
    private boolean resp;

    public IngresoDao() {
        this.CON = Conexion.getInstance();
    }

    @Override
    public List<Ingreso> listar(String texto, int totalPorPagina, int numPagina) {
        List<Ingreso> ingresos = new ArrayList<>();
        String sql = "SELECT i.id , i.usuario_id, u.nombre as usuario_nombre, i.persona_id, p.nombre as persona_nombre, i.tipo_comprobante, i.serie_comprobante, i.num_comprobante, i.fecha, i.impuesto, i.total, i.estado FROM ingreso as i INNER JOIN "
                + "persona as p ON (i.persona_id=p.id) INNER JOIN usuario as u ON (i.usuario_id=u.id) WHERE i.num_comprobante LIKE ? ORDER BY i.id ASC LIMIT ?, ?;";
        try {
            ps = this.CON.conectar().prepareStatement(sql);
            ps.setString(1, "%" + texto + "%");
            ps.setInt(2, (numPagina - 1) * totalPorPagina);
            ps.setInt(3, totalPorPagina);
            rs = ps.executeQuery();
            while (rs.next()) {
                Ingreso i = new Ingreso();
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

                ingresos.add(i);
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
        return ingresos;
    }

    @Override
    public List<DetalleIngreso> listarDetalle(int id) {
        List<DetalleIngreso> registros = new ArrayList<>();
        try{
            ps = CON.conectar().prepareStatement("SELECT a.id, a.codigo, a.nombre, d.cantidad, d.precio, (d.cantidad*precio) as sub_total FROM detalle_ingreso as d INNER JOIN articulo as a ON d.articulo_id=a.id WHERE d.ingreso_id=?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while(rs.next()){
                DetalleIngreso d = new DetalleIngreso();
                d.setArticuloId(rs.getInt(1));
                d.setArticuloCodigo(rs.getString(2));
                d.setArticuloNombre(rs.getString(3));
                d.setCantidad(rs.getInt(4));
                d.setPrecio(rs.getDouble(5));
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
    public boolean insertar(Ingreso obj) {
        resp=false;
        Connection conn=null;
        try {
            conn=CON.conectar();
            conn.setAutoCommit(false);
            String sqlInsertIngreso="INSERT INTO ingreso (persona_id,usuario_id,fecha,tipo_comprobante,serie_comprobante,num_comprobante,impuesto,total,estado) VALUES (?,?,now(),?,?,?,?,?,?)";
            
            ps=conn.prepareStatement(sqlInsertIngreso,Statement.RETURN_GENERATED_KEYS);
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
                String sqlInsertDetalle="INSERT INTO detalle_ingreso (ingreso_id,articulo_id,cantidad,precio) VALUES (?,?,?,?)";
                ps=conn.prepareStatement(sqlInsertDetalle);
                for (DetalleIngreso item : obj.getDetalles()){
                    ps.setInt(1,idGenerado);
                    ps.setInt(2,item.getArticuloId());
                    ps.setInt(3, item.getCantidad());
                    ps.setDouble(4, item.getPrecio());
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
                Logger.getLogger(IngresoDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally{
            try {
                if (rs!=null) rs.close();
                if (ps!=null) ps.close();
                if (conn!=null) conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(IngresoDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return resp;
    }

    @Override
    public boolean anular(int id) {
        resp = false;
        try {
            ps = CON.conectar().prepareStatement("UPDATE ingreso SET estado='Anulado' WHERE id=?");
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

    @Override
    public int total() {
        int totalRegistros = 0;
        try {
            ps = CON.conectar().prepareStatement("SELECT COUNT(id) FROM ingreso");
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
            ps = CON.conectar().prepareStatement("SELECT id FROM ingreso WHERE serie_comprobante=? AND num_comprobante=?");
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

}
