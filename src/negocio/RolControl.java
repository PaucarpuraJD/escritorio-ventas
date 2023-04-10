package negocio;

import datos.RolDao;
import entidades.Rol;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class RolControl {

    private final RolDao DATOS;
    private Rol obj;
    private DefaultTableModel modeloTabla;
    public int registrosMostrados = 0;

    public RolControl() {
        this.DATOS = new RolDao();
        this.obj = new Rol();
        this.registrosMostrados = 0;
    }

    public DefaultTableModel listar() {
        List<Rol> lista = new ArrayList<>();
        lista.addAll(DATOS.listar());

        String[] titulos = {"Id", "Nombre", "Descripcion"};
        this.modeloTabla = new DefaultTableModel(null, titulos);

        String[] registro = new String[3];

        this.registrosMostrados = 0;
        for (Rol items : lista) {

            registro[0] = Integer.toString(items.getId());
            registro[1] = items.getNombre();
            registro[2] = items.getDescripcion();
            this.modeloTabla.addRow(registro);
            this.registrosMostrados = this.registrosMostrados + 1;
        }
        return this.modeloTabla;
    }

    public int total() {
        return DATOS.total();
    }

    public int totalMostrados() {

        return this.registrosMostrados;
    }

}
