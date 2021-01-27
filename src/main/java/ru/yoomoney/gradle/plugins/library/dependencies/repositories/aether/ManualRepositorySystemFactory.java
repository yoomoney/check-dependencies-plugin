package ru.yoomoney.gradle.plugins.library.dependencies.repositories.aether;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;

/**
 * Создает объект {@link RepositorySystem}, используя {@link DefaultServiceLocator},
 * для работы с локальными репозиториями и удаленными репозиториями по HTTP
 *
 * @author Konstantin Novokreshchenov
 * @since 16.03.2017
 */
class ManualRepositorySystemFactory {

    private ManualRepositorySystemFactory() {}

    /**
     * Возвращет экземпляр класса {@link RepositorySystem}
     *
     * @return экземпляр класса {@link RepositorySystem}
     */
    static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.setServices(WagonProvider.class, new HttpWagonProvider());

        return locator.getService(RepositorySystem.class);
    }
}
