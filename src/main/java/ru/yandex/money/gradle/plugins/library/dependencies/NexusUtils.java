package ru.yandex.money.gradle.plugins.library.dependencies;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Методы для работы с nexus
 *
 * @author horyukova
 * @since 10.12.2018
 */
public final class NexusUtils {

    private NexusUtils() {
    }

    /**
     * Метод возвращает последнюю версию библиотеки, найденную в nexus
     * @param depGroup группа артифакта
     * @param depName имя артифакта
     * @return последнюю версию библиотеки
     */
    @SuppressFBWarnings("XXE_DOCUMENT")
    public static String getArtifactLatestVersion(String depGroup, String depName) {
        List<String> repoUrls = new ArrayList<>();

        if (depGroup.startsWith("ru.yandex.money") || depGroup.startsWith("ru.yamoney")) {
            repoUrls.add("releases");
            repoUrls.add("spp-releases");
        } else {
            repoUrls.add("central");
            repoUrls.add("thirdparty");
        }

        String path = depGroup.replace('.', '/');
        for (String repoUrl : repoUrls) {
            Document doc;

            try {
                String url = String.format("https://nexus.yamoney.ru/content/repositories/%s/%s/%s/maven-metadata.xml",
                        repoUrl, path, depName);
                String content = IOUtils.toString(new URL(url).openStream(), StandardCharsets.UTF_8.name());

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
                doc = documentBuilder.parse(IOUtils.toInputStream(content));

            } catch (Exception e) {
                throw new RuntimeException("Could not get latest artifact version", e);
            }

            doc.getDocumentElement().normalize();

            NodeList versionsNodeList = doc.getElementsByTagName("version");

            return IntStream.range(0, versionsNodeList.getLength())
                    .mapToObj(index -> versionsNodeList.item(index).getFirstChild().getNodeValue())
                    .filter(NexusUtils::isValidVersion)
                    .max(String::compareTo)
                    .<GradleException>orElseThrow(() -> {
                        throw new GradleException("Not found version: dependencyName=" + depName);
                    });
        }
        throw new GradleException("Not found version: dependencyName=" + depName);
    }

    private static boolean isValidVersion(String version) {
        String lowerName = version.toLowerCase();
        return !(lowerName.contains("alpha")
                || lowerName.contains("beta")
                || lowerName.contains("rc")
                || lowerName.matches(".*r\\d.*")
                || lowerName.matches(".*-b\\d.*")
                || lowerName.contains("sec"));
    }
}
