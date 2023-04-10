package negocio;

import datos.CategoriaDao;
import entidades.Categoria;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class CategoriaControl {

    private final CategoriaDao DATOS;
    private Categoria obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public CategoriaControl() {
        this.DATOS = new CategoriaDao();
        this.obj = new Categoria();
    }

    public DefaultTableModel listar(String texto) {
        List<Categoria> lista = new ArrayList<>();
        lista.addAll(DATOS.listar(texto));

        String[] titulos = {"Id", "Nombre", "Descripcion", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String estado;
        String[] registro = new String[4];
        
        this.registrosMostrados = 0;
        for (Categoria items : lista) {

            if (items.isActivo()) {
                estado = "Activo";

            } else {
                estado = "Inactivo";
            }
            registro[0] = Integer.toString(items.getId());
            registro[1] = items.getNombre();
            registro[2] = items.getDescripcion();
            registro[3] = estado;
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados +1;
        }
        return this.modeloTabla;
    }

    public String insertar(String nombre, String descripcion) {

        if (DATOS.existe(nombre)) {
            return "La categoria ya esxite";
        } 
        
        else {
            obj.setNombre(nombre);
            obj.setDescripcion(descripcion);
            if (DATOS.insertar(obj)) {
                return "OK";
            } else {
                return "Error, no se pudo registrar";
            }
        }
    }

    public String actualizar(int id, String nombre, String nombreAnt, String descripcion) {
        if (nombre.equals(nombreAnt)) {
            obj.setId(id);
            obj.setNombre(nombre);
            obj.setDescripcion(descripcion);
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
                obj.setNombre(nombre);
                obj.setDescripcion(descripcion);
                if(DATOS.actualizar(obj)){
                    return "OK";
                }else{
                    return "Error, no se puedo actualizar";
                }
            }
        }
    }

    public String desactivar(int id) {
        if(DATOS.desactivar(id)){
            return "OK";
        }else{
            return "No se puede desactivar";
        }
    }

    public String activar(int id) {
         if(DATOS.activar(id)){
            return "OK";
        }else{
            return "No se puede activar";
        }
    }

    public int total() {
        return DATOS.total();
    }

    public int totalMostrados() {

        return this.registrosMostrados;
    }
}
