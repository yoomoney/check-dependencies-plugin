package ru.yandex.money.gradle.plugins.library.dependencies.repositories.aether;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Фасад библиотеки aether для работы с локальными репозиториями и удаленными репозиториями по http
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 16.03.2017
 */
class AetherService {
    private static final String TEMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    private AetherService() { }

    static RepositorySystem newRepositorySystem() {
        return ManualRepositorySystemFactory.newRepositorySystem();
    }

    /**
     * Инициализирует сессию для работы с {@link RepositorySystem}
     *
     * @param system экземпляр класса {@link RepositorySystem}
     * @return объект сессии, используемый для работы с {@link RepositorySystem}
     */
    static RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(generateLocalCachePath());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        session.setTransferListener(new LoggedTransferListener());
        session.setRepositoryListener(new LoggedRepositoryListener());

        return session;
    }

    /**
     * Создает для каждого переданного URL-адреса соответствующий объект {@link RemoteRepository}
     *
     * @param repositoryUrls список URL-адресов репозиториев
     * @return список репозиториев
     */
    static List<RemoteRepository> createRemoteRepositories(List<String> repositoryUrls) {
        final int[] id = {0};
        return repositoryUrls.stream()
                .map(url -> new RemoteRepository(Integer.toString(id[0]++), "default", url))
                .collect(Collectors.toList());
    }

    private static String generateLocalCachePath() {
        return Paths.get(TEMP_DIRECTORY, UUID.randomUUID().toString()).toString();
    }
}
