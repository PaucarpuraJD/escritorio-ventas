package datos.interfaces;

import java.util.List;

public interface CrudPaginadoInterface<T> {
   List<T> listar(String texto, int totalPorPagina, int numPagina);
   boolean insertar (T obj);
   boolean actualizar (T obj);
   boolean desactivar (int id);
   boolean activar(int id);
   int total();
   boolean existe(String texto);    
}
