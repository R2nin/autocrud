package entities;

import br.edu.autocrud.annotations.Column;
import br.edu.autocrud.annotations.Entity;

@Entity(label = "Cliente")
public class Cliente {

    @Column(label = "Nome completo", order = 1,
            required = true, minLength = 3, maxLength = 150,
            placeholder = "Nome completo")
    private String nomeCompleto;

    @Column(label = "E-mail", order = 2, maxLength = 100,
            required = true,
            pattern = "^[\\w.+\\-]+@[\\w\\-]+\\.[a-z]{2,}$",
            errorMsg = "Informe um e-mail válido",
            placeholder = "email@exemplo.com")
    private String email;

    @Column(label = "CPF", order = 3, length = 14,
            mask = "CPF", errorMsg = "CPF inválido")
    private String cpf;

    @Column(label = "Celular", order = 4, length = 15,
            mask = "CELULAR")
    private String celular;

    @Column(label = "CEP", order = 5, length = 9,
            mask = "CEP")
    private String cep;

    @Column(label = "Cidade", order = 6,
            placeholder = "Ex: São Paulo")
    private String cidade;

    @Column(label = "Status", order = 7, length = 20,
            required = true, placeholder = "ativo / inativo")
    private String status;
}
