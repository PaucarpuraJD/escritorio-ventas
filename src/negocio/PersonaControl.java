package negocio;

import datos.PersonaDao;
import entidades.Persona;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class PersonaControl {

    private final PersonaDao DATOS;
    private Persona obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public PersonaControl() {
        this.DATOS = new PersonaDao();
        this.obj = new Persona();
    }

    public DefaultTableModel listar(String texto, int totalPorPagina, int numeroPagina) {
        List<Persona> lista = new ArrayList<>();
        lista.addAll(DATOS.listar(texto, totalPorPagina, numeroPagina));

        String[] titulos = {"ID", "Tipo Persona", "Persona", "Documento", "Numero Doc.", "Direccion", "Telefono", "Email", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String estado;
        String[] registro = new String[9];

        this.registrosMostrados = 0;
        for (Persona items : lista) {

            if (items.isActivo()) {
                estado = "Activo";

            } else {
                estado = "Inactivo";
            }
            registro[0] = Integer.toString(items.getId());
            registro[1] = items.getTipoPersona();
            registro[2] = items.getNombre();
            registro[3] = items.getTipoDocumento();
            registro[4] = items.getNumDocumento();
            registro[5] = items.getDireccion();
            registro[6] = items.getTelefono();
            registro[7] = items.getEmail();
            registro[8] = estado;
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }
    
        public DefaultTableModel listarTipo(String texto, int totalPorPagina, int numeroPagina, String tipoPersona) {
        List<Persona> lista = new ArrayList<>();
        lista.addAll(DATOS.listarTipo(texto, totalPorPagina, numeroPagina, tipoPersona));

        String[] titulos = {"ID", "Tipo Persona", "Persona", "Documento", "Numero Doc.", "Direccion", "Telefono", "Email", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String estado;
        String[] registro = new String[9];

        this.registrosMostrados = 0;
        for (Persona items : lista) {

            if (items.isActivo()) {
                estado = "Activo";

            } else {
                estado = "Inactivo";
            }
            registro[0] = Integer.toString(items.getId());
            registro[1] = items.getTipoPersona();
            registro[2] = items.getNombre();
            registro[3] = items.getTipoDocumento();
            registro[4] = items.getNumDocumento();
            registro[5] = items.getDireccion();
            registro[6] = items.getTelefono();
            registro[7] = items.getEmail();
            registro[8] = estado;
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }
    

    public String insertar(String tipoPersona, String nombre, String tipoDocumento, String numDocumento, String direccion, String telefono, String email) {

        if (DATOS.existe(nombre)) {
            return "El nombre ya esxite";
        } else {
            obj.setTipoPersona(tipoPersona);
            obj.setNombre(nombre);
            obj.setTipoDocumento(tipoDocumento);
            obj.setNumDocumento(numDocumento);
            obj.setDireccion(direccion);
            obj.setTelefono(telefono);
            obj.setEmail(email);
            if (DATOS.insertar(obj)) {
                return "OK";
            } else {
                return "Error, no se pudo registrar";
            }
        }
    }

    public String actualizar(int id, String tipoPersona, String nombre, String nombreAnt, String tipoDocumento, String numDocumento, String direccion, String telefono, String email) {
        if (nombre.equals(nombreAnt)) {
            obj.setId(id);
            obj.setTipoPersona(tipoPersona);
            obj.setNombre(nombre);
            obj.setTipoDocumento(tipoDocumento);
            obj.setNumDocumento(numDocumento);
            obj.setDireccion(direccion);
            obj.setTelefono(telefono);
            obj.setEmail(email);

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
                obj.setTipoPersona(tipoPersona);
                obj.setNombre(nombre);
                obj.setTipoDocumento(tipoDocumento);
                obj.setNumDocumento(numDocumento);
                obj.setDireccion(direccion);
                obj.setTelefono(telefono);
                obj.setEmail(email);

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
}
