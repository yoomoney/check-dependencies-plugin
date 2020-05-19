package ru.yandex.money.gradle.plugins.library.dependencies.checkversion;

import org.gradle.api.Project;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yandex.money.monitoring.push.DefaultPushEventKeyCreator;
import ru.yandex.money.monitoring.push.PushEventKey;
import ru.yandex.money.monitoring.push.producer.impl.PushEventQueue;
import ru.yandex.money.monitoring.push.producer.impl.PushEventSender;
import ru.yandex.money.monitoring.push.producer.impl.statsd.StatsdPushEventProducerImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Отправка метрик в графит
 *
 * @author horyukova
 * @since 15.05.2020
 */
public class MetricsSender {
    private final PushEventQueue pushEventQueue;
    private final StatsdPushEventProducerImpl pushEventProducer;

    private final Project project;

    public MetricsSender(@Nonnull Project project, @Nonnull String originUrl) {
        this.project = requireNonNull(project, "project");
        this.pushEventQueue = new PushEventQueue(100);
        this.pushEventProducer = new StatsdPushEventProducerImpl(pushEventQueue,
                format("yamoney_check_dependencies_plugin.%s", originUrl));
        PushEventSender pushEventSender = new PushEventSender(pushEventQueue,
                1, "localhost", 8126);
        try {
            pushEventSender.startupAllSenders();
        } catch (IOException e) {
            throw new RuntimeException("cannot start push event sender", e);
        }
    }

    /**
     * Отправить метрику о найденном конфликте версий
     *
     * @param libraryName имя конфликтной библиотеки
     */
    public void sendMajorConflict(LibraryName libraryName) {
        if (isMonitoringEnabled()) {
            PushEventKey pushEventKey = new DefaultPushEventKeyCreator().customKey(getAppName(),
                    "major_conflict", sanitizePushEventKey(libraryName.toString()), "failed");
            pushEventProducer.increment(pushEventKey);
            waitForPushEventsToComplete();
            project.getLogger().lifecycle("Send major conflic metric: {}", pushEventKey);
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

    private void waitForPushEventsToComplete() {
        int sleepCount = 5;
        while (sleepCount > 0) {
            int queueSize = pushEventQueue.size();
            if (queueSize != 0) {
                project.getLogger().lifecycle("PushEventQueue not empty, sleep for 1 sec: queueSize={}", queueSize);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    project.getLogger().error("Can't sleep: exp={}", e.getMessage());
                }
                sleepCount--;
            } else {
                break;
            }
        }
        int queueSize = pushEventQueue.size();
        if (queueSize != 0) {
            project.getLogger().error("PushEventQueue not empty: queueSize={}", queueSize);
        }
    }

    private static String sanitizePushEventKey(String key) {
        return key.trim().toLowerCase()
                .replace('.', '_')
                .replace('/', '_')
                .replace(' ', '_');
    }
}
