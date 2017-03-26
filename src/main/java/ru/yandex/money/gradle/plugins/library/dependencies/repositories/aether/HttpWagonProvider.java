package ru.yandex.money.gradle.plugins.library.dependencies.repositories.aether;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

/**
 * Создает {@link Wagon} для работы с удаленными репозиториями по http
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 16.03.2017
 */
class HttpWagonProvider implements WagonProvider {
    @Override
    public Wagon lookup(String roleHint) throws Exception {
        return "http".equals(roleHint) ? new LightweightHttpWagon() : null;
    }

    @Override
    public void release(Wagon wagon) {

    }
}
