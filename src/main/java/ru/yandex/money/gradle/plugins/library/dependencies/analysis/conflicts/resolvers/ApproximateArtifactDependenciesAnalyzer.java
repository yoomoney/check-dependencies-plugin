package ru.yandex.money.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.Artifact;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.ArtifactName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.DependencyPath;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yandex.money.gradle.plugins.library.dependencies.repositories.Repository;
import ru.yandex.money.gradle.plugins.library.dependencies.repositories.RepositoryArtifact;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Позволяет определить наличие у артефакта зависимости от библиотеки с версией, отличной от фиксированной,
 * на основании переданного пути зависимостей того же артефакта, но с другой версией.
 *
 * @param <T> тип артефакта
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 26.03.2017
 */
public class ApproximateArtifactDependenciesAnalyzer<T extends Artifact<T>> implements ArtifactDependenciesAnalyzer {
    private final Repository repository;
    private final DependencyPath<T> dependencyPathHint;

    /**
     * Конструктор класса
     *
     * @param repository репозиторий, используемый для определения версий артефактов и их зависимостей
     * @param dependencyPathHint путь зависимостей артефакта, используемый для определения наличия
     *                           у артефактов с другими версиями зависимости от таргетной библиотеки с другой версией
     */
    public ApproximateArtifactDependenciesAnalyzer(Repository repository, DependencyPath<T> dependencyPathHint) {
        this.repository = repository;
        this.dependencyPathHint = dependencyPathHint;
    }

    /**
     * <p>Определяет наличие у данного артефакта зависимости от версии библиотеки, отличной от фиксированной.
     * Возвращает true при одновременном выполнении следующих условаий:</p>
     * <ul>
     *     <li>имя переданного артефакта совпадает с именем артефакта,
     *         являющийся корневым в зарегистрированном пути зависимостей в {@link #dependencyPathHint}
     *     </li>
     *     <li>дерево зависимостей переданного артефакта содержит путь,
     *         совпадающий с зарегистрированным путем зависимостей {@link #dependencyPathHint} с точностью до имен библиотек
     *         и заканчивающийся версией библиотеки-зависимости, отличной от фиксированной
     *     </li>
     * </ul>
     *
     * Во всех остальных случаях возвращает false, поэтому возможен false negative.
     *
     * @param artifactName имя проверяемого артефакта
     * @param fixedDependency имя библиотеки-зависимости
     * @param fixedVersion фиксированная версия библиотеки
     * @return true, если данный артефакт содержит зависимость от версии библиотеки, отличной от фиксированной,
     *         false - иначе
     */
    @Override
    public boolean hasAnotherDependencyVersions(ArtifactName artifactName, LibraryName fixedDependency, String fixedVersion) {
        if (!getDirectDepenendencyHint().getLibraryName().equals(artifactName.getLibraryName())) {
            return false;
        }

        List<T> transitiveDependencies = StreamSupport.stream(dependencyPathHint.spliterator(), false)
                                                      .skip(1)
                                                      .collect(Collectors.toList());

        RepositoryArtifact currentArtifact = new RepositoryArtifact(repository, artifactName);
        for (T hintDependency: transitiveDependencies) {
            RepositoryArtifact currentDependency = currentArtifact.getDependencies().stream()
                                                                  .filter(dependency -> {
                                                                      LibraryName library = dependency.getName().getLibraryName();
                                                                      LibraryName hintLibrary = hintDependency.getName().getLibraryName();
                                                                      return library.equals(hintLibrary);
                                                                  })
                                                                  .findFirst().orElse(null);
            if (currentDependency  == null) {
                break;
            }

            ArtifactName currentDependencyName = currentDependency.getName();
            if (currentDependencyName.getLibraryName().equals(fixedDependency)) {
                return !currentDependencyName.getVersion().equals(fixedVersion);
            }

            currentArtifact = currentDependency;
        }

        return false;
    }

    private ArtifactName getDirectDepenendencyHint() {
        return dependencyPathHint.getRoot().getName();
    }
}
