package ru.yandex.money.gradle.plugins.library.dependencies.forbiddenartifacts;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.util.ConfigureUtil;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactWithVersionRange;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ForbiddenArtifactInfo;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;

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
     * Сет запрещенных артефактов
     */
    private Set<LibraryName> forbiddenLibrary = new HashSet<>();

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с конкретной версией
     */
    public void eq(Closure closure) {
        ForbiddenArtifactParameter forbiddenArtifactParameter = createForbiddenArtifactInfo(closure);
        ArtifactName forbiddenArtifact = ArtifactName.parse(forbiddenArtifactParameter.forbidden);
        LibraryName libraryName = forbiddenArtifact.getLibraryName();

        checkDoubleAddRule(libraryName);
        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(libraryName, forbiddenArtifact.getVersion())));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с заданием диапазона запрещенных версий
     */
    public void range(Closure closure) {
        ForbiddenArtifactParameter forbiddenArtifactParameter = createForbiddenArtifactInfo(closure);

        LibraryName forbiddenLibraryName = LibraryName.parse(forbiddenArtifactParameter.forbidden);
        checkDoubleAddRule(forbiddenLibraryName);

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(forbiddenLibraryName,
                forbiddenArtifactParameter.startVersion, forbiddenArtifactParameter.endVersion)));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с версиями меньше указанной
     */
    public void before(Closure closure) {
        ForbiddenArtifactParameter forbiddenArtifactParameter = createForbiddenArtifactInfo(closure);

        ArtifactName forbiddenArtifactName = ArtifactName.parse(forbiddenArtifactParameter.forbidden);
        checkDoubleAddRule(forbiddenArtifactName.getLibraryName());

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(forbiddenArtifactName.getLibraryName(),
                        null, forbiddenArtifactName.getVersion())));
    }

    /**
     * Проверка содержания в подключенных зависимостях данного артефакта, с версиями больше указанной
     */
    public void after(Closure closure) {
        ForbiddenArtifactParameter forbiddenArtifactParameter = createForbiddenArtifactInfo(closure);
        ArtifactName forbiddenArtifactName = ArtifactName.parse(forbiddenArtifactParameter.forbidden);

        checkDoubleAddRule(forbiddenArtifactName.getLibraryName());

        forbiddenArtifacts.add(createForbiddenArtifact(forbiddenArtifactParameter,
                new ArtifactWithVersionRange(forbiddenArtifactName.getLibraryName(),
                        forbiddenArtifactName.getVersion(), null)));
    }

    private ForbiddenArtifactInfo createForbiddenArtifact(ForbiddenArtifactParameter forbiddenArtifactParameter,
                                                          ArtifactWithVersionRange artifactWithVersionRange) {
        ArtifactName forbiddenArtifact = ArtifactName.parse(forbiddenArtifactParameter.forbidden);

        LibraryName libraryName = forbiddenArtifact.getLibraryName();

        return ForbiddenArtifactInfo.builder()
                .withForbiddenArtifact(artifactWithVersionRange)
                .withRecommendedArtifact(new ArtifactName(libraryName,
                        forbiddenArtifactParameter.recommended))
                .withComment(forbiddenArtifactParameter.comment)
                .build();
    }

    private void checkDoubleAddRule(LibraryName forbiddenLibraryName) {
        if (!forbiddenLibrary.add(forbiddenLibraryName)) {
            throw new GradleException(String.format("Rules for forbidden artifact already added: %s. " +
                            "May be you need range() method instead?",
                    forbiddenLibraryName));
        }
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
    private static class ForbiddenArtifactParameter {
        private String forbidden;
        private String recommended;
        private String comment;
        private String startVersion;
        private String endVersion;

        public void recommended(String recommended) {
            this.recommended = recommended;
        }

        public void forbidden(String forbidden) {
            this.forbidden = forbidden;
        }

        public void comment(String comment) {
            this.comment = comment;
        }

        public void startVersion(String startVersion) {
            this.startVersion = startVersion;
        }

        public void endVersion(String endVersion) {
            this.endVersion = endVersion;
        }
    }
}
