package datos.interfaces;

import java.util.List;

public interface CrudSimpleInterface<T> {
    
   List<T> listar(String texto);
   boolean insertar (T obj);
   boolean actualizar (T obj);
   boolean desactivar (int id);
   boolean activar(int id);
   int total();
   boolean existe(String texto);
}
