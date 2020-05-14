package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Project;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yandex.money.monitoring.push.DefaultPushEventKeyCreator;
import ru.yandex.money.monitoring.push.producer.impl.PushEventQueue;
import ru.yandex.money.monitoring.push.producer.impl.PushEventSender;
import ru.yandex.money.monitoring.push.producer.impl.statsd.StatsdPushEventProducerImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Отправка метрик в графит
 *
 * @author horyukova
 * @since 15.05.2020
 */
public class MetricsSender {
    private final PushEventQueue pushEventQueue = new PushEventQueue(100);
    private final PushEventSender pushEventSender = new PushEventSender(pushEventQueue,
            1, "localhost", 8126);
    private final StatsdPushEventProducerImpl pushEventProducer = new StatsdPushEventProducerImpl(pushEventQueue,
            "yamoney-check-dependencies-plugin");

    private final Project project;

    public MetricsSender(@Nonnull Project project) {
        try {
            pushEventSender.startupAllSenders();
        } catch (IOException e) {
            throw new RuntimeException("cannot start push event sender", e);
        }
        this.project = requireNonNull(project, "project");
    }

    /**
     * Отправить метрику о найденном конфликте версий
     *
     * @param libraryName имя конфликтной библиотеки
     */
    public void sendMajorConflict(LibraryName libraryName) {
        if (isMonitoringEnabled()) {
            pushEventProducer.increment(new DefaultPushEventKeyCreator().customKey(getAppName(),
                    "major_conflict", libraryName.toString(), "failed"));
        }
    }

    private boolean isMonitoringEnabled() {
        return Optional.ofNullable(project.getProperties().get("ci"))
                .map(it -> Boolean.valueOf((String) it))
                .orElse(false);
    }

    private String getAppName() {
        String appName = (String) project.getExtensions().getExtraProperties().get("appName");
        return appName == null ? "unknown" : appName;
    }
}
