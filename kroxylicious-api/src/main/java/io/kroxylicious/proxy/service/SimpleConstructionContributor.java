/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.kroxylicious.proxy.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class SimpleConstructionContributor<T, S extends Context> extends BaseContributor<T, S> {
    private final Logger log = getLogger(SimpleConstructionContributor.class);

    /**
     * Constructs and configures the contributor using the supplied {@code builder}.
     *
     * @param builder builder
     */
    protected SimpleConstructionContributor(BaseContributorBuilder<T, S> builder) {
        super(builder);
    }

    @Override
    public T getInstance(String typeName, S context) {
        try {
            return super.getInstance(typeName, context);
        }
        catch (IllegalArgumentException iae) {
            //Type name is not provided
            return loadClass(typeName, context);
        }
    }

    @SuppressWarnings("unchecked")
    private T loadClass(String typeName, S context) {
        try {
            final Class<? extends T> contributedClass = (Class<? extends T>) this.getClass().getClassLoader().loadClass(typeName);
            if (contributedClass != null) {
                final Optional<Constructor<? extends T>> contextCtor = getConstructor(typeName, contributedClass, context.getClass());
                if (contextCtor.isEmpty()) {
                    return invokeNoArgsConstructor(typeName, contributedClass);
                }
                else {
                    return invokeContextConstructor(typeName, context, contextCtor.get());
                }

            }
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    private static <T, S extends Context> T invokeContextConstructor(String typeName, S context, Constructor<? extends T> contextCtor) {
        try {
            return contextCtor.newInstance(context);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("unable to create " + typeName + " using " + context.getClass());
        }
    }

    private T invokeNoArgsConstructor(String typeName, Class<? extends T> contributedClass) {
        final Optional<Constructor<? extends T>> noArgsConstructor = getNoArgsConstructor(typeName, contributedClass);
        try {
            if (noArgsConstructor.isPresent()) {
                final Constructor<? extends T> constructor = noArgsConstructor.get();
                return constructor.newInstance();
            }
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("unable to create " + typeName + " using " + contributedClass);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Optional<Constructor<? extends T>> getNoArgsConstructor(String typeName, Class<? extends T> contributedClass) {
        try {
            return Optional.of(contributedClass.getConstructor());
        }
        catch (NoSuchMethodException e) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't find a no args constructor for {} (resolved as {})", typeName, contributedClass);
            }
        }
        return Optional.empty();
    }

    private Optional<Constructor<? extends T>> getConstructor(String typeName, Class<? extends T> contributedClass, Class<?> contextClass) {
        try {
            return Optional.of(contributedClass.getConstructor(contextClass));
        }
        catch (NoSuchMethodException e) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't find a no constructor for {} (resolved as {}) with {} argument", typeName, contributedClass, contextClass);
            }
        }
        return Optional.empty();
    }
}