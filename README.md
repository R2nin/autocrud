# AutoCrud Framework v1.0

Framework Java que usa **Reflection API**, **Annotations customizadas** e **Dynamic Proxies**
para gerar automaticamente CRUD completo com UI web embutida.

---

## Estrutura do projeto

```
autocrud/
│
├── lib/                          ← FRAMEWORK (não editar)
│   ├── src/br/edu/autocrud/
│   │   ├── annotations/          → @Entity, @Column, @CustomQuery
│   │   ├── core/                 → MetadataReader, CrudRepository, Database, CustomQueryProxy
│   │   ├── http/                 → AutoCrudServer, CrudHttpHandler, UiHandler
│   │   └── ui/                   → UiGenerator (SPA embutida)
│   └── jars/                     → H2, Jackson (dependências)
│
├── src/                          ← SEU CÓDIGO (edite aqui)
│   ├── entities/                 → Suas entidades @Entity
│   ├── repositories/             → Suas interfaces @CustomQuery
│   └── Main.java                 → Ponto de entrada
│
└── pom.xml
```

---

## Como usar

### 1. Criar uma entidade (manual)

Crie em `src/entities/`:

```java
@Entity(label = "Produto")
public class Produto {

    @Column(label = "Nome",     required = true, minLength = 2, maxLength = 120)
    private String nome;

    @Column(label = "Preço",    sqlType = "DECIMAL(19,2)", required = true,
            mask = "DINHEIRO",  min = "0.01")
    private BigDecimal preco;

    @Column(label = "CPF",      mask = "CPF",      length = 14)
    private String cpf;

    @Column(label = "Celular",  mask = "CELULAR",  length = 15)
    private String celular;

    @Column(label = "CEP",      mask = "CEP",      length = 9)
    private String cep;

    @Column(label = "E-mail",
            pattern = "^[\\w.+\\-]+@[\\w\\-]+\\.[a-z]{2,}$",
            errorMsg = "E-mail inválido")
    private String email;
}
```

### 2. Criar uma entidade pela UI (automático)

Com o servidor rodando, clique em **"Nova Entidade"** no menu lateral.
Configure os campos e clique em **"Gerar arquivo .java"**.
O arquivo é salvo em `src/entities/` automaticamente.

Após gerar:
1. Recompile: `mvn compile`
2. Reinicie: `mvn exec:java`
3. A entidade aparece automaticamente no menu.

### 3. Adicionar query customizada (opcional)

```java
// src/repositories/ProdutoRepository.java
public interface ProdutoRepository {
    @CustomQuery("SELECT * FROM PRODUTO WHERE CATEGORIA = ?")
    List<Map<String, Object>> buscarPorCategoria(String categoria) throws Exception;
}
```

---

## Máscaras disponíveis em @Column(mask = ...)

| Alias       | Formato                       |
|-------------|-------------------------------|
| `CPF`       | `000.000.000-00`              |
| `CNPJ`      | `00.000.000/0000-00`          |
| `TELEFONE`  | `(00) 0000-0000`              |
| `CELULAR`   | `(00) 00000-0000`             |
| `CEP`       | `00000-000`                   |
| `DATA`      | `00/00/0000`                  |
| `HORA`      | `00:00`                       |
| `CARTAO`    | `0000 0000 0000 0000`         |
| `DINHEIRO`  | Formatação monetária          |
| Customizada | ex: `"AAA-0000"`, `"0000/00"` |

---

## Tipos Java suportados em @Column

| Tipo Java       | SQL gerado       |
|-----------------|------------------|
| `String`        | `VARCHAR(255)`   |
| `Integer`/`int` | `INT`            |
| `Long`/`long`   | `BIGINT`         |
| `Double`/`double` | `DOUBLE`       |
| `Float`/`float` | `FLOAT`          |
| `BigDecimal`    | `DECIMAL(19,2)`  |
| `Boolean`/`boolean` | `BOOLEAN`    |
| `LocalDate`     | `DATE`           |
| `LocalDateTime` | `TIMESTAMP`      |

> Use `sqlType` para sobrescrever o tipo inferido, ex: `@Column(sqlType = "DECIMAL(10,2)")`

---

## Execução

```bash
mvn exec:java
```

Sobe o servidor em `http://localhost:8080` com:
- UI de gerenciamento completa (CRUD, máscaras, validações, modal de detalhes)
- Gerador de entidades Java via interface web
- Banco H2 embutido (dados persistem em `autocrud-db.mv.db`)
- API REST: `GET/POST/PUT/DELETE /api/{entidade}`

---

## Tecnologias do framework

| Conceito Java           | Onde é usado                                          |
|-------------------------|-------------------------------------------------------|
| **Reflection API**      | `MetadataReader` lê `@Entity`/`@Column` em runtime    |
| **Annotations**         | `@Entity`, `@Column`, `@CustomQuery` customizadas     |
| **Dynamic Proxy (AOP)** | `CustomQueryProxy` intercepta `@CustomQuery` métodos  |
| **HttpServer**          | `AutoCrudServer` serve REST + SPA sem dependências    |
| **H2 Embedded**         | `Database` cria tabelas dinamicamente via SQL gerado  |
