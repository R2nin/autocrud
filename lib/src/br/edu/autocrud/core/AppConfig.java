package br.edu.autocrud.core;

import java.io.InputStream;
import java.util.Properties;

/**
 * Carrega as configurações do framework a partir de {@code autocrud.properties}
 * no classpath ({@code src/resources/}).
 *
 * <p>Todas as propriedades são <strong>opcionais</strong>. Se o arquivo não
 * existir ou uma chave estiver comentada/ausente, o valor padrão é usado
 * sem erros.</p>
 *
 * <p>Propriedades disponíveis:</p>
 * <pre>
 *   server.port          (padrão: 8080)
 *   db.url               (padrão: jdbc:h2:./autocrud-db;DB_CLOSE_DELAY=-1)
 *   db.user              (padrão: sa)
 *   db.password          (padrão: "")
 *   db.max.connections   (padrão: 20)
 * </pre>
 */
public class AppConfig {

    private static final String FILE = "autocrud.properties";

    private static final int    DEFAULT_PORT            = 8080;
    private static final String DEFAULT_DB_URL          = "jdbc:h2:./autocrud-db;DB_CLOSE_DELAY=-1";
    private static final String DEFAULT_DB_USER         = "sa";
    private static final String DEFAULT_DB_PASSWORD     = "";
    private static final int    DEFAULT_DB_MAX_CONN     = 20;
    private final Properties props = new Properties();
    private final boolean    loaded;

    /**
     * Cria e carrega a configuração. Silencioso se o arquivo não existir.
     */
    public AppConfig() {
        boolean ok = false;
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(FILE)) {
            if (is != null) {
                props.load(is);
                ok = true;
                System.out.println("[AutoCrud] Configuração carregada de " + FILE);
            }
        } catch (Exception e) {
            System.out.println("[AutoCrud] Aviso: não foi possível ler " + FILE + " — usando padrões.");
        }
        this.loaded = ok;
    }

    public int    serverPort()        { return getInt("server.port",        DEFAULT_PORT);         }
    public String dbUrl()             { return get("db.url",                DEFAULT_DB_URL);        }
    public String dbUser()            { return get("db.user",               DEFAULT_DB_USER);       }
    public String dbPassword()        { return get("db.password",           DEFAULT_DB_PASSWORD);   }
    public int    dbMaxConnections()  { return getInt("db.max.connections",  DEFAULT_DB_MAX_CONN);   }

    /**
     * Imprime no console as configurações ativas (mascarando a senha).
     */
    public void print() {
        System.out.println("[AutoCrud] Configurações ativas:");
        System.out.println("           server.port        = " + serverPort());
        System.out.println("           db.url             = " + dbUrl());
        System.out.println("           db.user            = " + dbUser());
        System.out.println("           db.password        = " + (dbPassword().isEmpty() ? "(vazia)" : "***"));
        System.out.println("           db.max.connections = " + dbMaxConnections());
    }

    private String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue).trim();
    }

    private int getInt(String key, int defaultValue) {
        String val = props.getProperty(key, String.valueOf(defaultValue)).trim();
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            System.out.println("[AutoCrud] Aviso: '" + key + "' tem valor inválido (\"" + val
                    + "\") — usando padrão " + defaultValue + ".");
            return defaultValue;
        }
    }
}
