package repositories;

import br.edu.autocrud.annotations.CustomQuery;
import java.util.List;
import java.util.Map;

public interface ProdutoRepository {

    List<Map<String, Object>> findAll() throws Exception;

    @CustomQuery("SELECT * FROM PRODUTO WHERE CATEGORIA = ?")
    List<Map<String, Object>> buscarPorCategoria(String categoria) throws Exception;

    @CustomQuery("SELECT * FROM PRODUTO WHERE PRECO <= ?")
    List<Map<String, Object>> buscarAtePreco(double precoMaximo) throws Exception;

    @CustomQuery("SELECT * FROM PRODUTO WHERE NOME LIKE ?")
    List<Map<String, Object>> buscarPorNome(String nomeContendo) throws Exception;

    @CustomQuery("SELECT * FROM PRODUTO WHERE ATIVO = TRUE ORDER BY NOME")
    List<Map<String, Object>> listarAtivos() throws Exception;
}
