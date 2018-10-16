/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.microprofile.arquillian;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;

/**
 * An arquillian LoadableExtension defining the {@link HelidonDeployableContainer}.
 */
class HelidonContainerExtension implements LoadableExtension {

    /**
     * We provide our own CDI enricher because the one in Arquillian
     * isn't properly initialized due to Helidon starting the CDI
     * container much later in time. Our enricher will grab the
     * current {@code BeanManager} and rely on the base class to
     * do the actual enrichment work.
     */
    static class HelidonCDIInjectionEnricher extends CDIInjectionEnricher {

        @Override
        public BeanManager getBeanManager() {
            CDI<Object> cdi = CDI.current();
            return cdi != null ? cdi.getBeanManager() : null;
        }

        @Override
        public CreationalContext<Object> getCreationalContext() {
            BeanManager beanManager = getBeanManager();
            return beanManager != null ? beanManager.createCreationalContext(null) : null;
        }
    }

    /**
     * The Helidon extension provides a container, a new protocol (even though
     * we run in embedded mode) and a new test enricher.
     *
     * @param builder Extension builder.
     */
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, HelidonDeployableContainer.class);
        builder.service(Protocol.class, HelidonLocalProtocol.class);
        builder.service(TestEnricher.class, HelidonCDIInjectionEnricher.class);
    }
}
