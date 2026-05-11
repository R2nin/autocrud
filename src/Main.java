import br.edu.autocrud.core.AppConfig;
import br.edu.autocrud.core.CrudRepository;
import br.edu.autocrud.core.CustomQueryProxy;
import br.edu.autocrud.core.Database;
import br.edu.autocrud.core.EntityMetadata;
import br.edu.autocrud.core.EntityScanner;
import br.edu.autocrud.core.EntityScanner.EntityScanException;
import br.edu.autocrud.core.MetadataReader;
import br.edu.autocrud.core.MetadataReader.MetadataException;
import br.edu.autocrud.http.AutoCrudServer;
import repositories.ProdutoRepository;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

/**
 * Ponto de entrada do AutoCrud Framework.
 *
 * <p>Execução via Maven:</p>
 * <pre>
 *   mvn exec:java   → servidor web + H2
 * </pre>
 *
 * <p>Configuração opcional em {@code src/resources/autocrud.properties}.</p>
 * <p>Entidades detectadas automaticamente de {@code src/entities/}.</p>
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        try {
            AppConfig config = new AppConfig();
            config.print();
            System.out.println();

            List<Class<?>> entityClasses = new EntityScanner().scan();
            List<EntityMetadata> entities = new MetadataReader().readAll(entityClasses);

            System.out.println("[AutoCrud] Entidades carregadas: " +
                entities.stream().map(EntityMetadata::getLabel).toList());
            System.out.println();

            runWeb(entities, config);

        } catch (EntityScanException e) {
            abort("Erro ao escanear entidades", e.getMessage());
        } catch (MetadataException e) {
            abort("Erro de configuração em uma entidade", e.getMessage());
        } catch (Exception e) {
            abort("Erro inesperado", e.getMessage());
        }
    }

    private static void runWeb(List<EntityMetadata> entities, AppConfig config) throws Exception {
        System.out.println("[Modo] Servidor Web + H2 Database");
        System.out.println();

        Database db = new Database(config);
        db.createTables(entities);

        entities.stream()
                .filter(m -> m.getEntityClass().getSimpleName().equalsIgnoreCase("Produto"))
                .findFirst()
                .ifPresent(meta -> {
                    var repo  = new CrudRepository<>(meta, db);
                    var proxy = CustomQueryProxy.create(ProdutoRepository.class, repo);
                    System.out.println("[AutoCrud] Proxy @CustomQuery criado: ProdutoRepository");
                });

        AutoCrudServer server = new AutoCrudServer(config.serverPort(), entities, db);
        server.start();

        String url = "http://localhost:" + config.serverPort();
        openBrowser(url);
        System.out.println("[AutoCrud] UI disponível em: " + url);
        System.out.println("[AutoCrud] Ctrl+C para encerrar\n");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[AutoCrud] Encerrando...");
            server.stop();
        }));
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("linux"))
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                else if (os.contains("windows"))
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
                else if (os.contains("mac"))
                    Runtime.getRuntime().exec(new String[]{"open", url});
            }
        } catch (Exception e) {
            System.out.println("[AutoCrud] Abra manualmente: " + url);
        }
    }

    private static void abort(String title, String detail) {
        System.err.println();
        System.err.println("╔══════════════════════════════════════════╗");
        System.err.println("║  ERRO — " + title);
        System.err.println("╠══════════════════════════════════════════╣");
        System.err.println("║  " + detail);
        System.err.println("╚══════════════════════════════════════════╝");
        System.err.println();
        System.exit(1);
    }

    private static void printBanner() {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║           AutoCrud Framework  v1.0               ║");
        System.out.println("╠═══════════════════════════════════════════════════╣");
        System.out.println("║  mvn exec:java   → Modo Web                      ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();
    }
}
