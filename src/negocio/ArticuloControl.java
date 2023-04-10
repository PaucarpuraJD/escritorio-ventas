package negocio;

import database.Conexion;
import datos.ArticuloDao;
import datos.CategoriaDao;
import entidades.Articulo;
import entidades.Categoria;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

public class ArticuloControl {

    private final ArticuloDao DATOS;
    private final CategoriaDao DATOSCAT;
    private Articulo obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public ArticuloControl() {
        this.DATOS = new ArticuloDao();
        this.DATOSCAT = new CategoriaDao();
        this.obj = new Articulo();
    }

    public DefaultTableModel listar(String texto, int totalPorPagina, int numeroPagina) {
        List<Articulo> lista = new ArrayList<>();
        lista.addAll(DATOS.listar(texto, totalPorPagina, numeroPagina));

        String[] titulos = {"ID", "Categoria ID", "Categoria", "Codigo", "Nombre", "Precio Venta", "Stock", "Descripcion", "Imagen", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String estado;
        String[] registro = new String[10];

        this.registrosMostrados = 0;
        for (Articulo items : lista) {

            if (items.isActivo()) {
                estado = "Activo";

            } else {
                estado = "Inactivo";
            }
            registro[0] = Integer.toString(items.getId());
            registro[1] = Integer.toString(items.getCategoriaId());
            registro[2] = items.getCategoriaNombre();
            registro[3] = items.getCodigo();
            registro[4] = items.getNombre();
            registro[5] = Double.toString(items.getPrecioVenta());
            registro[6] = Integer.toString(items.getStock());
            registro[7] = items.getDescripcion();
            registro[8] = items.getImagen();
            registro[9] = estado;
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }

    public DefaultTableModel listarArticuloVenta(String texto, int totalPorPagina, int numeroPagina) {
        List<Articulo> lista = new ArrayList<>();
        lista.addAll(DATOS.listarArticuloVenta(texto, totalPorPagina, numeroPagina));

        String[] titulos = {"ID", "Categoria ID", "Categoria", "Codigo", "Nombre", "Precio Venta", "Stock", "Descripcion", "Imagen", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String estado;
        String[] registro = new String[10];

        this.registrosMostrados = 0;
        for (Articulo items : lista) {

            if (items.isActivo()) {
                estado = "Activo";

            } else {
                estado = "Inactivo";
            }
            registro[0] = Integer.toString(items.getId());
            registro[1] = Integer.toString(items.getCategoriaId());
            registro[2] = items.getCategoriaNombre();
            registro[3] = items.getCodigo();
            registro[4] = items.getNombre();
            registro[5] = Double.toString(items.getPrecioVenta());
            registro[6] = Integer.toString(items.getStock());
            registro[7] = items.getDescripcion();
            registro[8] = items.getImagen();
            registro[9] = estado;
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }

    public DefaultComboBoxModel seleccionar() {
        DefaultComboBoxModel items = new DefaultComboBoxModel();
        List<Categoria> lista = new ArrayList<>();
        lista = DATOSCAT.seleccionar();
        for (Categoria item : lista) {
            Categoria c = new Categoria();
            c.setId(item.getId());
            c.setNombre(item.getNombre());
            items.addElement(c);
        }
        return items;
    }

    public String insertar(int categoriaId, String codigo, String nombre, double precioVenta, int stock, String descripcion, String imagen) {

        if (DATOS.existe(nombre)) {
            return "La categoria ya esxite";
        } else {
            obj.setCategoriaId(categoriaId);
            obj.setCodigo(codigo);
            obj.setNombre(nombre);
            obj.setPrecioVenta(precioVenta);
            obj.setStock(stock);
            obj.setDescripcion(descripcion);
            obj.setImagen(imagen);
            if (DATOS.insertar(obj)) {
                return "OK";
            } else {
                return "Error, no se pudo registrar";
            }
        }
    }

    public String actualizar(int id, int categoriaId, String codigo, String nombre, String nombreAnt, double precioVenta, int stock, String descripcion, String imagen) {
        if (nombre.equals(nombreAnt)) {
            obj.setId(id);
            obj.setCategoriaId(categoriaId);
            obj.setCodigo(codigo);
            obj.setNombre(nombre);
            obj.setPrecioVenta(precioVenta);
            obj.setStock(stock);
            obj.setDescripcion(descripcion);
            obj.setImagen(imagen);
            if (DATOS.actualizar(obj)) {
                return "OK";
            } else {
                return "Error, no se puedo actualizar";
            }
        } else {
            if (DATOS.existe(nombre)) {
                return "El registro ya existe";
            } else {
                obj.setId(id);
                obj.setCategoriaId(categoriaId);
                obj.setCodigo(codigo);
                obj.setNombre(nombre);
                obj.setPrecioVenta(precioVenta);
                obj.setStock(stock);
                obj.setDescripcion(descripcion);
                obj.setImagen(imagen);
                if (DATOS.actualizar(obj)) {
                    return "OK";
                } else {
                    return "Error, no se puedo actualizar";
                }
            }
        }
    }

    public String desactivar(int id) {
        if (DATOS.desactivar(id)) {
            return "OK";
        } else {
            return "No se puede desactivar";
        }
    }

    public String activar(int id) {
        if (DATOS.activar(id)) {
            return "OK";
        } else {
            return "No se puede activar";
        }
    }

    public int total() {
        return DATOS.total();
    }

    public int totalMostrados() {

        return this.registrosMostrados;
    }

    public void reporteArticulos() {
        Map p = new HashMap();
        JasperReport report;
        JasperPrint print;
        Conexion cnn = Conexion.getInstance();
        try {
            report = JasperCompileManager.compileReport(new File("").getAbsolutePath()
                    + "/src/reportes/RptArticulos.jrxml");
            print = JasperFillManager.fillReport(report, p, cnn.conectar());
            JasperViewer view = new JasperViewer(print, false);
            view.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace(System.err);
        }
    }

    public void reporteArticulosBarras() {
        Map p = new HashMap();
        JasperReport report;
        JasperPrint print;
        Conexion cnn = Conexion.getInstance();
        try {
            report = JasperCompileManager.compileReport(new File("").getAbsolutePath()
                    + "/src/reportes/RptArticulosBarras.jrxml");
            print = JasperFillManager.fillReport(report, p, cnn.conectar());
            JasperViewer view = new JasperViewer(print, false);
            view.setVisible(true);
        } catch (JRException e) {
            e.printStackTrace(System.err);
        }
    }
}
