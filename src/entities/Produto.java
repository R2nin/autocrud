package entities;

import br.edu.autocrud.annotations.Column;
import br.edu.autocrud.annotations.Entity;
import java.math.BigDecimal;

@Entity(label = "Produto")
public class Produto {

    @Column(label = "Nome", order = 1,
            required = true, minLength = 2, maxLength = 120,
            placeholder = "Ex: Notebook Dell XPS")
    private String nome;

    @Column(label = "Descrição", order = 2, maxLength = 500,
            placeholder = "Descrição detalhada...")
    private String descricao;

    @Column(label = "Preço", order = 3, sqlType = "DECIMAL(19,2)",
            required = true, min = "0.01",
            mask = "DINHEIRO", placeholder = "0,00")
    private BigDecimal preco;

    @Column(label = "Estoque", order = 4, sqlType = "INT",
            min = "0", placeholder = "0")
    private Integer estoque;

    @Column(label = "Categoria", order = 5,
            required = true, placeholder = "Ex: Eletrônicos")
    private String categoria;

    @Column(label = "Ativo", order = 6, sqlType = "BOOLEAN")
    private Boolean ativo;
}
