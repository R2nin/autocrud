package br.edu.autocrud.core;

import br.edu.autocrud.annotations.Column;
import br.edu.autocrud.annotations.Entity;
import br.edu.autocrud.core.EntityMetadata.ColumnMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

/**
 * Lê uma classe Java via Reflection API e produz um {@link EntityMetadata}.
 *
 * <p>Lança {@link MetadataException} com mensagem clara para qualquer
 * problema de configuração nas anotações da entidade.</p>
 */
public class MetadataReader {

    /**
     * Lê os metadados de uma única classe.
     *
     * @param clazz classe a inspecionar
     * @return metadados prontos para uso
     * @throws MetadataException se a classe estiver malconfigurada
     */
    public EntityMetadata read(Class<?> clazz) {
        if (clazz == null) {
            throw new MetadataException("Classe nula passada para MetadataReader.");
        }
        if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation()) {
            throw new MetadataException(
                clazz.getName() + " não é uma classe concreta. " +
                "@Entity só pode ser aplicada em classes."
            );
        }

        Entity entityAnn = clazz.getAnnotation(Entity.class);
        if (entityAnn == null) {
            throw new MetadataException(
                "A classe " + clazz.getSimpleName() + " não tem @Entity. " +
                "Adicione @Entity(label = \"Nome\") acima da declaração da classe."
            );
        }

        String tableName = entityAnn.table().isBlank()
                ? clazz.getSimpleName().toUpperCase()
                : entityAnn.table().toUpperCase();

        EntityMetadata meta = new EntityMetadata(clazz, tableName, entityAnn.label());

        List<ColumnMetadata> columns = Arrays.stream(clazz.getDeclaredFields())
                .peek(f -> f.setAccessible(true))
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(f -> readColumn(f, clazz))
                .toList();

        if (columns.isEmpty()) {
            throw new MetadataException(
                "A entidade " + clazz.getSimpleName() + " não tem nenhum campo @Column. " +
                "Adicione @Column em pelo menos um campo da classe."
            );
        }

        columns.forEach(meta::addColumn);
        return meta;
    }

    /**
     * Lê os metadados de múltiplas classes.
     *
     * @param classes lista de classes a processar
     * @return lista de metadados, uma por classe
     * @throws MetadataException se qualquer classe estiver malconfigurada
     */
    public List<EntityMetadata> readAll(List<Class<?>> classes) {
        if (classes == null || classes.isEmpty()) {
            throw new MetadataException(
                "Nenhuma classe fornecida para o MetadataReader. " +
                "Verifique se há entidades em src/entities/."
            );
        }
        return classes.stream().map(this::read).toList();
    }

    private ColumnMetadata readColumn(Field field, Class<?> ownerClass) {
        Column ann = field.getAnnotation(Column.class);

        if (ann.minLength() < 0) {
            throw new MetadataException(
                ownerClass.getSimpleName() + "." + field.getName() +
                ": minLength não pode ser negativo."
            );
        }
        if (ann.maxLength() > 0 && ann.minLength() > ann.maxLength()) {
            throw new MetadataException(
                ownerClass.getSimpleName() + "." + field.getName() +
                ": minLength (" + ann.minLength() + ") é maior que maxLength (" + ann.maxLength() + ")."
            );
        }
        if (!ann.min().isBlank() && !ann.max().isBlank()) {
            try {
                double min = Double.parseDouble(ann.min());
                double max = Double.parseDouble(ann.max());
                if (min > max) {
                    throw new MetadataException(
                        ownerClass.getSimpleName() + "." + field.getName() +
                        ": min (" + ann.min() + ") é maior que max (" + ann.max() + ")."
                    );
                }
            } catch (NumberFormatException e) {
                throw new MetadataException(
                    ownerClass.getSimpleName() + "." + field.getName() +
                    ": min ou max não é um número válido. Use formato como \"0.01\" ou \"100\"."
                );
            }
        }
        if (!ann.pattern().isBlank()) {
            try {
                java.util.regex.Pattern.compile(ann.pattern());
            } catch (java.util.regex.PatternSyntaxException e) {
                throw new MetadataException(
                    ownerClass.getSimpleName() + "." + field.getName() +
                    ": pattern inválido \"" + ann.pattern() + "\" — " + e.getMessage()
                );
            }
        }

        return ColumnMetadata.from(field, ann);
    }

    /**
     * Lançada quando uma entidade está mal configurada.
     */
    public static class MetadataException extends RuntimeException {
        public MetadataException(String message) { super(message); }
    }
}
