package ru.yandex.money.gradle.plugins.library.dependencies.repositories.aether;

import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;

/**
 * Создает объект {@link RepositorySystem}, используя {@link DefaultServiceLocator},
 * для работы с локальными репозиториями и удаленными репозиториями по HTTP
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 16.03.2017
 */
class ManualRepositorySystemFactory {

    /**
     * Возвращет экземпляр класса {@link RepositorySystem}
     *
     * @return экземпляр класса {@link RepositorySystem}
     */
    static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = new DefaultServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class);
        locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);
        locator.setServices(WagonProvider.class, new HttpWagonProvider());

        return locator.getService(RepositorySystem.class);
    }
}
