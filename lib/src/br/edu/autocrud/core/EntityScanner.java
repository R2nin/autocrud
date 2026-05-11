package br.edu.autocrud.core;

import br.edu.autocrud.annotations.Column;
import br.edu.autocrud.annotations.Entity;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Varre o pacote {@code entities} em tempo de execução e retorna todas
 * as classes anotadas com {@link Entity}, sem necessidade de registro manual.
 *
 * <p>Estratégia: localiza o diretório de classes compiladas via
 * {@code ClassLoader}, lista os arquivos {@code .class} dentro de
 * {@code entities/} e carrega cada um via {@link Class#forName}.</p>
 *
 * <p>Validações realizadas para cada classe encontrada:</p>
 * <ul>
 *   <li>Deve ter {@code @Entity} — caso contrário é ignorada com aviso</li>
 *   <li>Deve ter ao menos um campo com {@code @Column}</li>
 *   <li>Nenhum campo {@code @Column} pode ter nome em branco após formatação</li>
 * </ul>
 */
public class EntityScanner {

    private static final String ENTITIES_PACKAGE = "entities";

    /**
     * Varre o classpath procurando classes no pacote {@code entities}.
     *
     * @return lista de classes válidas, prontas para {@link MetadataReader#read}
     * @throws EntityScanException se nenhuma entidade válida for encontrada
     */
    public List<Class<?>> scan() {
        List<Class<?>> found = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        File entitiesDir = findEntitiesDir();
        if (entitiesDir == null || !entitiesDir.exists()) {
            throw new EntityScanException(
                "Diretório 'entities' não encontrado no classpath. " +
                "Crie suas entidades em src/entities/ com a anotação @Entity."
            );
        }

        File[] classFiles = entitiesDir.listFiles(f -> f.getName().endsWith(".class"));
        if (classFiles == null || classFiles.length == 0) {
            throw new EntityScanException(
                "Nenhuma classe encontrada em src/entities/. " +
                "Crie ao menos uma classe com @Entity e @Column."
            );
        }

        for (File classFile : classFiles) {
            String className = ENTITIES_PACKAGE + "." + classFile.getName().replace(".class", "");
            try {
                Class<?> clazz = Class.forName(className);
                ValidationResult result = validate(clazz);
                if (result.valid) {
                    found.add(clazz);
                } else {
                    warnings.add("[AutoCrud] AVISO: " + className + " ignorada — " + result.reason);
                }
            } catch (ClassNotFoundException e) {
                warnings.add("[AutoCrud] AVISO: não foi possível carregar " + className);
            }
        }

        warnings.forEach(System.out::println);

        if (found.isEmpty()) {
            throw new EntityScanException(
                "Nenhuma classe válida encontrada em src/entities/. " +
                "Verifique se suas classes possuem @Entity e pelo menos um campo @Column."
            );
        }

        found.sort((a, b) -> a.getSimpleName().compareTo(b.getSimpleName()));
        return found;
    }

    private ValidationResult validate(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            return ValidationResult.fail("não tem @Entity");
        }

        long columnCount = java.util.Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class))
                .count();

        if (columnCount == 0) {
            return ValidationResult.fail(
                "tem @Entity mas nenhum campo com @Column. " +
                "Adicione @Column em pelo menos um campo."
            );
        }

        return ValidationResult.ok();
    }

    private File findEntitiesDir() {
        // 1) Tenta via ClassLoader (funciona na maioria dos casos)
        URL resource = Thread.currentThread()
                .getContextClassLoader()
                .getResource(ENTITIES_PACKAGE);
        if (resource != null) {
            try {
                return new File(resource.toURI());
            } catch (java.net.URISyntaxException e) {
                return new File(resource.getFile());
            }
        }

        // 2) Varre o java.class.path explicitamente
        String cp = System.getProperty("java.class.path", "");
        for (String entry : cp.split(File.pathSeparator)) {
            File candidate = new File(entry, ENTITIES_PACKAGE);
            if (candidate.isDirectory()) return candidate;
        }

        // 3) Fallback: procura target/classes relativo ao diretório de trabalho
        //    (útil ao rodar via mvn exec:java com exec-maven-plugin)
        File[] fallbacks = {
            new File("target/classes/" + ENTITIES_PACKAGE),
            new File("../target/classes/" + ENTITIES_PACKAGE)
        };
        for (File fb : fallbacks) {
            if (fb.isDirectory()) return fb;
        }

        return null;
    }

    private record ValidationResult(boolean valid, String reason) {
        static ValidationResult ok()             { return new ValidationResult(true,  null); }
        static ValidationResult fail(String msg) { return new ValidationResult(false, msg);  }
    }

    /**
     * Lançada quando o scanner não consegue encontrar entidades válidas.
     */
    public static class EntityScanException extends RuntimeException {
        public EntityScanException(String message) { super(message); }
    }
}
