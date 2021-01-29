package ru.yoomoney.gradle.plugins.library.dependencies.forbiddenartifacts;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ArtifactWithVersionRange;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.ForbiddenArtifactInfo;
import ru.yoomoney.gradle.plugins.library.dependencies.dsl.LibraryName;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс, позволяющий настраивать проверку запрещенных артефактов
 *
 * @author horyukova
 * @since 26.05.2019
 */
public class ForbiddenDependenciesExtension {
    /**
     * Сет запрещенных артефактов
     */
    public Set<ForbiddenArtifactInfo> forbiddenArtifacts = new HashSet<>();

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с конкретной версией
     */
    public void eq(Closure closure) {
        eq(createForbiddenArtifactInfo(closure));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с конкретной версией
     */
    public void eq(ForbiddenArtifactParameter forbiddenArtifactParameter) {
        ArtifactName forbiddenArtifact = ArtifactName.parse(forbiddenArtifactParameter.forbidden);
        LibraryName libraryName = forbiddenArtifact.getLibraryName();

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(libraryName, forbiddenArtifact.getVersion())));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с заданием диапазона запрещенных версий
     */
    public void range(Closure closure) {
        range(createForbiddenArtifactInfo(closure));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с заданием диапазона запрещенных версий
     */
    public void range(ForbiddenArtifactParameter forbiddenArtifactParameter) {
        LibraryName forbiddenLibraryName = LibraryName.parse(forbiddenArtifactParameter.forbidden);

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(forbiddenLibraryName,
                        forbiddenArtifactParameter.startVersion, forbiddenArtifactParameter.endVersion)));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с версиями меньше указанной
     */
    public void before(Closure closure) {
        before(createForbiddenArtifactInfo(closure));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с версиями меньше указанной
     */
    public void before(ForbiddenArtifactParameter forbiddenArtifactParameter) {
        ArtifactName forbiddenArtifactName = ArtifactName.parse(forbiddenArtifactParameter.forbidden);

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(forbiddenArtifactName.getLibraryName(),
                        null, forbiddenArtifactName.getVersion())));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с версиями больше указанной
     */
    public void after(Closure closure) {
        after(createForbiddenArtifactInfo(closure));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с версиями больше указанной
     */
    public void after(ForbiddenArtifactParameter forbiddenArtifactParameter) {
        ArtifactName forbiddenArtifactName = ArtifactName.parse(forbiddenArtifactParameter.forbidden);

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(forbiddenArtifactName.getLibraryName(),
                        forbiddenArtifactName.getVersion(), null)));
    }

    private ForbiddenArtifactInfo createForbiddenArtifact(ForbiddenArtifactParameter forbiddenArtifactParameter,
                                                          ArtifactWithVersionRange artifactWithVersionRange) {

        return ForbiddenArtifactInfo.builder()
                .withForbiddenArtifact(artifactWithVersionRange)
                .withRecommendedArtifact(new ArtifactName(artifactWithVersionRange.getLibraryName(),
                        forbiddenArtifactParameter.recommended))
                .withComment(forbiddenArtifactParameter.comment)
                .build();
    }

    private ForbiddenArtifactParameter createForbiddenArtifactInfo(Closure closure) {
        Action<? super ForbiddenArtifactParameter> action = ConfigureUtil.configureUsing(closure);
        ForbiddenArtifactParameter forbiddenArtifactParameter = new ForbiddenArtifactParameter();
        action.execute(forbiddenArtifactParameter);
        return forbiddenArtifactParameter;
    }

    /**
     * Для удобной передачи параметров из gradle
     */
    public static class ForbiddenArtifactParameter {
        private String forbidden;
        private String recommended;
        private String comment;
        private String startVersion;
        private String endVersion;

        public ForbiddenArtifactParameter recommended(String recommended) {
            this.recommended = recommended;
            return this;
        }

        public ForbiddenArtifactParameter forbidden(String forbidden) {
            this.forbidden = forbidden;
            return this;
        }

        public ForbiddenArtifactParameter comment(String comment) {
            this.comment = comment;
            return this;
        }

        public ForbiddenArtifactParameter startVersion(String startVersion) {
            this.startVersion = startVersion;
            return this;
        }

        public ForbiddenArtifactParameter endVersion(String endVersion) {
            this.endVersion = endVersion;
            return this;
        }
    }
}
