package ru.yandex.money.plugins.library.readme;

import net.slkdev.swagger.confluence.constants.PaginationMode;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import ru.yandex.money.common.publishing.DocPublisher;
import ru.yandex.money.common.publishing.DocPublisherConfig;
import ru.yandex.money.common.publishing.DocType;

import java.io.IOException;

/**
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 20.12.2016
 */
public class PublishReadmeTask extends DefaultTask {
    static final String TASK_NAME = "publishReadme";

    @TaskAction
    void publishReadme() throws IOException {
        ReadmePluginConfiguration configuration = (ReadmePluginConfiguration) getProject().getExtensions().getByName(TASK_NAME);

        DocPublisher docPublisher = new DocPublisher();
        DocPublisherConfig config = new DocPublisherConfig.Builder()
                .withAuthentication(configuration.authentication)
                .withConfluenceRestApi(configuration.confluenceRestApiUrl)
                .withPaginationMode(PaginationMode.SINGLE_PAGE)
                .withSpaceKey(configuration.confluenceSpace)
                .withAncestorId(configuration.confluenceAncestorPageId)
                .withDocType(DocType.MARKDOWN)
                .withPrefix(configuration.prefix)
                .withTitle(configuration.artifactId)
                .withPathToDocument(configuration.pathToDocument)
                .build();
        docPublisher.publish(config);
    }
}
