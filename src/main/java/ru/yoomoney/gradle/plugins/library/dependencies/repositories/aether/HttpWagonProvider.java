package ru.yoomoney.gradle.plugins.library.dependencies.repositories.aether;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpsWagon;
import org.eclipse.aether.connector.wagon.WagonProvider;

/**
 * Создает {@link Wagon} для работы с удаленными репозиториями по http
 *
 * @author Konstantin Novokreshchenov
 * @since 16.03.2017
 */
class HttpWagonProvider implements WagonProvider {

    @Override
    public Wagon lookup(String roleHint) {
        switch (roleHint) {
            case "http":
                return new LightweightHttpWagon();

            case "https":
                return new LightweightHttpsWagon();

            default:
                throw new RuntimeException("No wagon could be retrieved for specified role hint: " + roleHint);
        }
    }

    @Override
    public void release(Wagon wagon) {

    }

}
