package ru.yandex.money.gradle.plugins.library.readme;

import org.gradle.api.Project;

/**
 * Класс, позволяющий настраивать ReadmePlugin.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 20.12.2016
 * @see <a href="https://wiki.yamoney.ru/display/WebPortal/PublishingDocToConfluence">Описание процесса публикации</a>
 */
@SuppressWarnings("WeakerAccess")
public class ReadmePluginExtension {
    /** Имя extension'а, под которым он регистрируется в проекте. */
    static final String EXTENSION_NAME = "readme";

    /**
     * Аутентификационные данные пользователя confluence от имени которого будут совершаться действия в confluence.
     * Формат: username:password - encoded with base64, сейчас используется пользователь ovr-test-user
     */
    public String authentication;

    /** Url Confluence rest api, через который будет происходить публикация. */
    public String confluenceRestApiUrl;

    /** Space в Confluence, в котором будет расположена документация. */
    public String confluenceSpace;

    /** PageId страницы в Confluence, к которой будет прикреплена страница с созданной документацией. */
    public int confluenceAncestorPageId;

    /** Префикс для имени страницы с документацией. */
    public String prefix;

    /** Путь к файлу с документацией. */
    public String pathToDocument;

    /** Заголовок будущей страницы. */
    public String pageTitle;

    public ReadmePluginExtension(Project project) {
        authentication = System.getenv("CONFLUENCE_AUTH");
        confluenceRestApiUrl = "https://wiki.yamoney.ru:443/rest/api/";
        confluenceSpace = "WebPortal";
        confluenceAncestorPageId = 128657081; //https://wiki.yamoney.ru/display/WebPortal/Libraries
        prefix = "lib.";
        pathToDocument = project.getProjectDir().getPath() + "/README.md";
        pageTitle = project.getName();
    }
}
