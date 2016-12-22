package ru.yandex.money.plugins.library.readme;

import org.gradle.api.Project;

/**
 * Класс, позволяющий настраивать ReadmePlugin.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 20.12.2016
 */
@SuppressWarnings("WeakerAccess")
public class ReadmePluginExtension {
    static final String EXTENSION_NAME = "readme";

    public String authentication;
    public String confluenceRestApiUrl;
    public String confluenceSpace;
    public int confluenceAncestorPageId;
    public String prefix;
    public String pathToDocument;
    public String artifactId;

    public ReadmePluginExtension(Project project) {
        authentication = System.getenv("CONFLUENCE_AUTH");
        confluenceRestApiUrl = "https://wiki.yamoney.ru:443/rest/api/";
        confluenceSpace = "WebPortal";
        confluenceAncestorPageId = 128657081; //https://wiki.yamoney.ru/display/WebPortal/Libraries
        prefix = "lib.";
        pathToDocument = project.getProjectDir().getPath() + "/README.md";
        artifactId = project.getName();
    }
}
