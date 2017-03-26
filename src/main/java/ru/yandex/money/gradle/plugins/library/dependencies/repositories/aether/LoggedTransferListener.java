package ru.yandex.money.gradle.plugins.library.dependencies.repositories.aether;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferEvent;

/**
 * Логирует этапы процесса скачивания артефактв с репозитория и загрузки артефакта в репозиторий
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 16.03.2017
 */
class LoggedTransferListener extends AbstractTransferListener {
    private final Logger log = Logging.getLogger(LoggedTransferListener.class);

    @Override
    public void transferInitiated(TransferEvent event) {
        String message = event.getRequestType() == TransferEvent.RequestType.PUT
                         ? "Uploading"
                         : "Downloading";

        log.info("{}: {}", message, getResource(event));
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        log.info("Transfer succeeded: {}", getResource(event));
    }

    @Override
    public void transferFailed(TransferEvent event) {
        log.info("Transfer failed: {}, error={}", getResource(event), getErrorMessage(event));
    }

    @Override
    public void transferCorrupted(TransferEvent event) {
        log.info("Transfer corrupted: {}, error={}", getResource(event), getErrorMessage(event));
    }

    private static String getResource(TransferEvent event) {
        return event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
    }

    private static String getErrorMessage(TransferEvent event) {
        return event.getException().getMessage();
    }
}
