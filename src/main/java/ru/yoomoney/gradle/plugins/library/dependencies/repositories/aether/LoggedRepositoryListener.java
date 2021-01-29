package ru.yoomoney.gradle.plugins.library.dependencies.repositories.aether;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Логирует события, возникающие при взаимодействии с репозиторий,
 * при попытке резолва и загрузки артефакта
 *
 * @author Konstantin Novokreshchenov
 * @since 16.03.2017
 */
class LoggedRepositoryListener extends AbstractRepositoryListener {
    private final Logger log = Logging.getLogger(LoggedRepositoryListener.class);

    @Override
    public void artifactDescriptorInvalid(RepositoryEvent event) {
        log.info("Invalid artifact descriptor for {}: {}", event.getArtifact(), event.getException().getMessage());
    }

    @Override
    public void artifactDescriptorMissing(RepositoryEvent event) {
        log.info("Missing artifact descriptor for {}", event.getArtifact());
    }

    @Override
    public void artifactResolved(RepositoryEvent event) {
        log.info("Resolved artifact {} from {}", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactDownloading(RepositoryEvent event) {
        log.info("Downloading artifact {} from {}", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactDownloaded(RepositoryEvent event) {
        log.info("Downloaded artifact {} from {}", event.getArtifact(), event.getRepository());
    }

    @Override
    public void artifactResolving(RepositoryEvent event) {
        log.info("Resolving artifact {}", event.getArtifact());
    }

    @Override
    public void metadataInvalid(RepositoryEvent event) {
        log.info("Invalid metadata {}", event.getMetadata());
    }

    @Override
    public void metadataResolved(RepositoryEvent event) {
        log.info("Resolved metadata {} from {}", event.getMetadata(), event.getRepository());
    }

    @Override
    public void metadataResolving(RepositoryEvent event) {
        log.info("Resolving metadata {} from {}", event.getMetadata(), event.getRepository());
    }

}
