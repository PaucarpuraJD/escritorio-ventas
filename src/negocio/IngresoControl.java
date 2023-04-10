package negocio;

import datos.ArticuloDao;
import datos.IngresoDao;
import entidades.Articulo;
import entidades.Ingreso;
import entidades.DetalleIngreso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class IngresoControl {

    private final IngresoDao DATOS;
    private final ArticuloDao DATOSART;
    private Ingreso obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public IngresoControl() {
        this.DATOS = new IngresoDao();
        this.DATOSART = new ArticuloDao();
        this.obj = new Ingreso();
    }

    public DefaultTableModel listar(String texto, int totalPorPagina, int numeroPagina) {
        List<Ingreso> lista = new ArrayList<>();
        lista.addAll(DATOS.listar(texto, totalPorPagina, numeroPagina));

        String[] titulos = {"ID", "Usuario ID", "Usuario", "Proveedor ID", "Proveedor", "Comprobante", "Serie", "Numero", "Fecha", "Impuesto", "Total", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String[] registro = new String[12];

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-YYYY");

        this.registrosMostrados = 0;
        for (Ingreso items : lista) {

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
        List<DetalleIngreso> lista = new ArrayList<>();
        lista.addAll(this.DATOS.listarDetalle(id));
        
        String[] titulos = {"ID", "CODIGO", "NOMBRE", "CANTIDAD", "PRECIO", "SUBTOTAL"};
        this.modeloTabla = new DefaultTableModel(null, titulos);
        
        String[] registro = new String[6];

        for(DetalleIngreso item : lista){
            registro[0] = Integer.toString(item.getArticuloId());
            registro[1] = item.getArticuloCodigo();
            registro[2] = item.getArticuloNombre();
            registro[3] = Integer.toString(item.getCantidad());
            registro[4] = Double.toString(item.getPrecio());
            registro[5] = Double.toString(item.getSubTotal());
            
            this.modeloTabla.addRow(registro);
            
            
           }
        return this.modeloTabla;
    }

    public Articulo obtenerAticuloCodigoIngreso(String codigo) {

        Articulo art = DATOSART.obtenerArticuloCodigoIngreso(codigo);
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

            List<DetalleIngreso> detalles = new ArrayList();
            int articuloId;
            int cantidad;
            double precio;

            for (int i = 0; i < modeloDetalles.getRowCount(); i++) {
                articuloId = Integer.parseInt(String.valueOf(modeloDetalles.getValueAt(i, 0)));
                cantidad = Integer.parseInt(String.valueOf(modeloDetalles.getValueAt(i, 3)));
                precio = Double.parseDouble(String.valueOf(modeloDetalles.getValueAt(i, 4)));
                detalles.add(new DetalleIngreso(articuloId, cantidad, precio));
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

    public int total() {
        return DATOS.total();
    }

    public int totalMostrados() {

        return this.registrosMostrados;
    }
}
