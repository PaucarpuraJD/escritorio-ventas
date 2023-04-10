package negocio;

import database.Conexion;
import datos.ArticuloDao;
import datos.VentaDao;
import entidades.Articulo;
import entidades.Venta;
import entidades.DetalleVenta;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class VentaControl {

    private final VentaDao DATOS;
    private final ArticuloDao DATOSART;
    private Venta obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public VentaControl() {
        this.DATOS = new VentaDao();
        this.DATOSART = new ArticuloDao();
        this.obj = new Venta();
    }

    public DefaultTableModel listar(String texto, int totalPorPagina, int numeroPagina) {
        List<Venta> lista = new ArrayList<>();
        lista.addAll(DATOS.listar(texto, totalPorPagina, numeroPagina));

        String[] titulos = {"ID", "Usuario ID", "Usuario", "Cliente ID", "Cliente", "Comprobante", "Serie", "Numero", "Fecha", "Impuesto", "Total", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String[] registro = new String[12];

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-YYYY");

        this.registrosMostrados = 0;
        for (Venta items : lista) {

            registro[0] = Integer.toString(items.getId());
            registro[1] = Integer.toString(items.getUsuarioId());
            registro[2] = items.getUsuarioNombre();
            registro[3] = Integer.toString(items.getPersonaId());
            registro[4] = items.getPersonaNombre();
            registro[5] = items.getTipoComprobante();
            registro[6] = items.getSerieComprobante();
            registro[7] = items.getNumComprobante();
            registro[8] = df.format(items.getFecha());
            registro[9] = Double.toString(items.getImpuesto());
            registro[10] = Double.toString(items.getTotal());
            registro[11] = items.getEstado();

            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }
    
    public DefaultTableModel listarDetalle(int id){
        List<DetalleVenta> lista = new ArrayList<>();
        lista.addAll(this.DATOS.listarDetalle(id));
        
        String[] titulos = {"ID", "CODIGO", "ARTICULO", "STOCK", "CANTIDAD", "PRECIO","DESCUENTO", "SUBTOTAL"};
        this.modeloTabla = new DefaultTableModel(null, titulos);
        
        String[] registro = new String[8];

        for(DetalleVenta item : lista){
            registro[0] = Integer.toString(item.getArticuloId());
            registro[1] = item.getArticuloCodigo();
            registro[2] = item.getArticuloNombre();
            registro[3] = Integer.toString(item.getArticuloStock());
            registro[4] = Integer.toString(item.getCantidad());
            registro[5] = Double.toString(item.getPrecio());
            registro[6] = Double.toString(item.getDescuento());
            registro[7] = Double.toString(item.getSubTotal());
            
            this.modeloTabla.addRow(registro);
            
            
           }
        return this.modeloTabla;
    }

    public Articulo obtenerAticuloCodigoVenta(String codigo) {

        Articulo art = DATOSART.obtenerArticuloCodigoVenta(codigo);
        return art;
    }

    public String insertar(int personaId, String tipoComprobante, String serieComprobante, String numComprobante, double impuesto, double total, DefaultTableModel modeloDetalles) {
        if (DATOS.existe(serieComprobante, numComprobante)) {
            return "El registro ya existe.";
        } else {
            obj.setUsuarioId(Variables.usuarioId);
            obj.setPersonaId(personaId);
            obj.setTipoComprobante(tipoComprobante);
            obj.setSerieComprobante(serieComprobante);
            obj.setNumComprobante(numComprobante);
            obj.setImpuesto(impuesto);
            obj.setTotal(total);

            List<DetalleVenta> detalles = new ArrayList();
            int articuloId;
            int cantidad;
            double precio;
            double descuento;

            for (int i = 0; i < modeloDetalles.getRowCount(); i++) {
                articuloId = Integer.parseInt(String.valueOf(modeloDetalles.getValueAt(i, 0)));
                cantidad = Integer.parseInt(String.valueOf(modeloDetalles.getValueAt(i, 4)));
                precio = Double.parseDouble(String.valueOf(modeloDetalles.getValueAt(i, 5)));
                descuento = Double.parseDouble(String.valueOf(modeloDetalles.getValueAt(i, 6)));
                detalles.add(new DetalleVenta(articuloId, cantidad, precio,descuento));
            }

            obj.setDetalles(detalles);

            if (DATOS.insertar(obj)) {
                return "OK";
            } else {
                return "Error en el registro.";
            }
        }
    }

    public String Anular(int id) {
        if (DATOS.anular(id)) {
            return "OK";
        } else {
            return "No se puede anular el registro";
        }
    }
    
    public String ultimoSerie(String tipoComprobante) {
        return this.DATOS.ultimoSerie(tipoComprobante);
    }
    
    public String ultimoNumero(String tipoComprobante,String serieComprobante) {
        return this.DATOS.ultimoNumero(tipoComprobante, serieComprobante);
    }

    public int total() {
        return DATOS.total();
    }

    public int totalMostrados() {

        return this.registrosMostrados;
    }
    
    
    public void reporteComprobante(String idVenta) {
        Map p = new HashMap();
        p.put("idVenta", idVenta);
        JasperReport report;
        JasperPrint print;
        Conexion cnn = Conexion.getInstance();
        try {
            report = JasperCompileManager.compileReport(new File("").getAbsolutePath()
                    + "/src/reportes/RptComprobante.jrxml");
            print = JasperFillManager.fillReport(report, p, cnn.conectar());
            JasperViewer view = new JasperViewer(print, false);
            view.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace(System.err);
        }
    }
    
    public DefaultTableModel consultaFechas(Date fechaInicio, Date fechaFin){
        List<Venta> lista=new ArrayList<>();
        lista.addAll(DATOS.consultaFechas(fechaInicio,fechaFin));
        
        String[] titulos={"Id","Usuario ID","Usuario","Cliente ID","Cliente","Tipo Comprobante","Serie","Número","Fecha","Impuesto","Total","Estado"};
        this.modeloTabla=new DefaultTableModel(null,titulos);        
        
        String[] registro = new String[12];
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy");
        
        this.registrosMostrados=0;
        for (Venta item:lista){
            registro[0]=Integer.toString(item.getId());
            registro[1]=Integer.toString(item.getUsuarioId());
            registro[2]=item.getUsuarioNombre();
            registro[3]=Integer.toString(item.getPersonaId());
            registro[4]=item.getPersonaNombre();
            registro[5]=item.getTipoComprobante();
            registro[6]=item.getSerieComprobante();
            registro[7]=item.getNumComprobante();
            registro[8]=sdf.format(item.getFecha());
            registro[9]=Double.toString(item.getImpuesto());
            registro[10]=Double.toString(item.getTotal());
            registro[11]=item.getEstado();
            
            this.modeloTabla.addRow(registro);
            this.registrosMostrados=this.registrosMostrados+1;
        }
        return this.modeloTabla;
    }
}
