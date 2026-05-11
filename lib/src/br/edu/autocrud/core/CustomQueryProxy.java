package br.edu.autocrud.core;

import br.edu.autocrud.annotations.CustomQuery;

import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CustomQueryProxy implements InvocationHandler {

    private final CrudRepository<?>      delegate;

    public CustomQueryProxy(CrudRepository<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CustomQuery cq = method.getAnnotation(CustomQuery.class);

        if (cq != null) {

            try {
                List<Map<String, Object>> result =
                        delegate.executeCustomQuery(cq.value(), args != null ? args : new Object[0]);

                if (method.getReturnType() == java.util.Optional.class)
                    return result.isEmpty() ? java.util.Optional.empty()
                                            : java.util.Optional.of(result.get(0));
                return result;
            } catch (SQLException e) {
                throw new RuntimeException("Erro em @CustomQuery: " + e.getMessage(), e);
            }
        }

        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> repositoryInterface,
                                CrudRepository<?> base) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{ repositoryInterface },
                new CustomQueryProxy(base)
        );
    }
}
