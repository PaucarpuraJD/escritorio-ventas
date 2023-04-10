package negocio;

import datos.UsuarioDao;
import datos.RolDao;
import entidades.Rol;
import entidades.Usuario;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;

public class UsuarioControl {

    private final UsuarioDao DATOS;
    private final RolDao DATOSROL;
    private Usuario obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public UsuarioControl() {
        this.DATOS = new UsuarioDao();
        this.DATOSROL = new RolDao();
        this.obj = new Usuario();
    }

    public DefaultTableModel listar(String texto, int totalPorPagina, int numeroPagina) {
        List<Usuario> lista = new ArrayList<>();
        lista.addAll(DATOS.listar(texto, totalPorPagina, numeroPagina));

        String[] titulos = {"ID", "Rol ID", "Rol", "Nombre", "Documento.", "Numero Doc.", "Direccion", "Telefono", "Email", "Clave", "Estado"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String estado;
        String[] registro = new String[11];

        this.registrosMostrados = 0;
        for (Usuario items : lista) {

            if (items.isActivo()) {
                estado = "Activo";

            } else {
                estado = "Inactivo";
            }
            registro[0] = Integer.toString(items.getId());
            registro[1] = Integer.toString(items.getRolId());
            registro[2] = items.getRolNombre();
            registro[3] = items.getNombre();
            registro[4] = items.getTipoDocumento();
            registro[5] = items.getNunDocumento();
            registro[6] = items.getDireccion();
            registro[7] = items.getTelefono();
            registro[8] = items.getEmail();
            registro[9] = items.getClave();
            registro[10] = estado;
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }
    
    public String login (String email, String clave){
    
        String resp = "0";
        Usuario u = DATOS.login(email, this.encriptar(clave));
        if(u !=null){
            if(u.isActivo()){
                Variables.usuarioId = u.getId();
                Variables.rolId = u.getRolId();
                Variables.rolNombre = u.getRolNombre();
                Variables.usuarioNombre = u.getNombre();
                Variables.usuarioEmail = u.getEmail();
                resp = "1";
            }else{
                resp = "2";
            }
        }
        
        return resp;
    }

    public DefaultComboBoxModel seleccionar() {
        DefaultComboBoxModel items = new DefaultComboBoxModel();
        List<Rol> lista = new ArrayList<>();
        lista = DATOSROL.seleccionar();
        for (Rol item : lista) {
            Rol c = new Rol();
            c.setId(item.getId());
            c.setNombre(item.getNombre());
            items.addElement(c);
        }
        return items;
    }

    private static String encriptar(String valor) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        byte[] hash = md.digest(valor.getBytes());
        StringBuilder sb = new StringBuilder();

        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String insertar(int rolId, String nombre, String tipoDocumento, String numDocumento, String direccion, String telefono, String email, String clave) {

        if (DATOS.existe(email)) {
            return "El email ya esxite";
        } else {
            obj.setRolId(rolId);
            obj.setNombre(nombre);
            obj.setTipoDocumento(tipoDocumento);
            obj.setNunDocumento(numDocumento);
            obj.setDireccion(direccion);
            obj.setTelefono(telefono);
            obj.setEmail(email);
            obj.setClave(this.encriptar(clave));
            if (DATOS.insertar(obj)) {
                return "OK";
            } else {
                return "Error, no se pudo registrar";
            }
        }
    }

    public String actualizar(int id, int rolId, String nombre, String tipoDocumento, String numDocumento, String direccion, String telefono, String email, String emailAnt, String clave) {
        if (email.equals(emailAnt)) {
            obj.setId(id);
            obj.setRolId(rolId);
            obj.setNombre(nombre);
            obj.setTipoDocumento(tipoDocumento);
            obj.setNunDocumento(numDocumento);
            obj.setDireccion(direccion);
            obj.setTelefono(telefono);
            obj.setEmail(email);

            String encriptado;
            if (clave.length() == 64) {
                encriptado = clave;
            } else {
                encriptado = encriptar(clave);
            }
            obj.setClave(encriptado);
            if (DATOS.actualizar(obj)) {
                return "OK";
            } else {
                return "Error, no se puedo actualizar";
            }
        } else {
            if (DATOS.existe(email)) {
                return "El registro ya existe";
            } else {
                obj.setId(id);
                obj.setRolId(rolId);
                obj.setNombre(nombre);
                obj.setTipoDocumento(tipoDocumento);
                obj.setNunDocumento(numDocumento);
                obj.setDireccion(direccion);
                obj.setTelefono(telefono);
                obj.setEmail(email);

                String encriptado;
                if (clave.length() == 64) {
                    encriptado = clave;
                } else {
                    encriptado = encriptar(clave);
                }
                obj.setClave(encriptado);

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
