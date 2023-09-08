/*
 * Copyright Kroxylicious Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.kroxylicious.proxy.service;

import io.kroxylicious.proxy.config.BaseConfig;

/**
 * Support loading an Instance of a service, optionally providing it with configuration obtained
 * from the Kroxylicious configuration file.
 *
 * @param <T> the service type
 */
public interface Contributor<T> {

    /**
     * Gets the concrete type of the configuration required by this service instance.
     *
     * @param shortName service short name
     * @return class of a concrete type, or null if this contributor does not offer this short name.
     */
    Class<? extends BaseConfig> getConfigType(String shortName);

    /**
     * Used to determine if a configuration object is required for this filter.
     * By default, it is assumed that if a custom config type is specified it is required.
     * To make custom config classes entirely optional implementors should override this method.
     *
     * @param shortName service short name
     * @return <code>true</code> if the configuration must be specified.
     */
    Boolean requiresConfig(String shortName);

    /**
     * Creates an instance of the service.
     *
     * @param shortName service short name
     * @param config    service configuration which may be null if the service instance does not accept configuration.
     * @return the service instance, or null if this contributor does not offer this short name.
     */
    T getInstance(String shortName, BaseConfig config);
}