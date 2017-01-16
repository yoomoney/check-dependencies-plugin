package ru.yandex.money.gradle.plugins.library.readme;

import net.slkdev.swagger.confluence.constants.PaginationMode;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import ru.yandex.money.common.publishing.DocPublisher;
import ru.yandex.money.common.publishing.DocPublisherConfig;
import ru.yandex.money.common.publishing.DocType;

import java.io.IOException;

/**
 * Задача для публикации readme файла на confluence.
 * Настройки берутся из {@link ReadmePluginExtension}.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 20.12.2016
 */
public class PublishReadmeTask extends DefaultTask {
    public static final String TASK_NAME = "publishReadme";

    @TaskAction
    void publishReadme() throws IOException {
        ReadmePluginExtension configuration = getProject().getExtensions().getByType(ReadmePluginExtension.class);

        DocPublisher docPublisher = new DocPublisher();
        DocPublisherConfig config = new DocPublisherConfig.Builder()
                .withAuthentication(configuration.authentication)
                .withConfluenceRestApi(configuration.confluenceRestApiUrl)
                .withPaginationMode(PaginationMode.SINGLE_PAGE)
                .withSpaceKey(configuration.confluenceSpace)
                .withAncestorId(configuration.confluenceAncestorPageId)
                .withDocType(DocType.MARKDOWN)
                .withPrefix(configuration.prefix)
                .withTitle(configuration.pageTitle)
                .withPathToDocument(configuration.pathToDocument)
                .build();
        docPublisher.publish(config);
    }
}
