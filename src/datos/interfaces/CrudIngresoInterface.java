package datos.interfaces;

import java.util.List;

public interface CrudIngresoInterface<T,D> {
   List<T> listar(String texto, int totalPorPagina, int numPagina);
   List<D> listarDetalle (int id);
   boolean insertar (T obj);
   boolean anular (int id);
   int total();
   boolean existe(String texto, String texto2);    
}
