package ru.yoomoney.gradle.plugins.library.dependencies;

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPlugin;
import ru.yoomoney.gradle.plugins.library.dependencies.checkversion.MajorVersionCheckerExtension;
import ru.yoomoney.gradle.plugins.library.dependencies.checkversion.VersionChecker;
import ru.yoomoney.gradle.plugins.library.dependencies.forbiddenartifacts.CheckForbiddenDependenciesTask;
import ru.yoomoney.gradle.plugins.library.dependencies.forbiddenartifacts.ForbiddenDependenciesExtension;
import ru.yoomoney.gradle.plugins.library.dependencies.showdependencies.PrintActualDependenciesByGroupTask;
import ru.yoomoney.gradle.plugins.library.dependencies.showdependencies.PrintAllActualDependenciesTask;
import ru.yoomoney.gradle.plugins.library.dependencies.showdependencies.PrintDependenciesByGroupTask;
import ru.yoomoney.gradle.plugins.library.dependencies.showdependencies.PrintNewDependenciesVersionsTask;
import ru.yoomoney.gradle.plugins.library.dependencies.snapshot.CheckSnapshotsDependenciesTask;

import javax.annotation.Nonnull;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Плагин проверяет легитимность изменения версий используемых библиотек (как прямо, так и по транзитивным зависимостям) в проекте.
 * <p>
 * Зачастую проект может содержать большое количество повторно используемых библоиотек разных версий, найденных по транзитивным
 * зависимостям. Однако, при запуске приложения может быть использована только одна версия одной и той же библиотеки.
 * Чтобы гарантировать согласованность этой библиотеки с другими, Gradle имеет встроенный механизм решения конфликтов версий.
 * По умолчанию Gradle из всех версий одной и той же библиотеки выбирает самую последнюю. При таком подходе нет гарантии, что самая
 * новая версия библиотеки будет обратно совместима с предыдущей версией. А значит нельзя гарантировать, что такое повышение
 * не сломает проект.
 * <p>
 * Для того, чтобы избежать не контролируемое изменение версий, используется подход с фиксацией набор версий бибилиотек, на которых
 * гарантируется работа приложения.
 * <p>
 * Для фиксации используется сторонний плагин <b>IO Spring Dependency Management plugin</b>. Список фиксируемых библиотек с
 * версиями хранится в maven xml.pom файле. Плагин предоставляет программный доступ к этому списку.
 * <p>
 * Обратной стороной фиксации служит неконтролируемое понижение версии библиотек. Чтобы сделать этот процесс изменения версий
 * библиотек контролируемым, сделан этот плагин.
 * <p>
 * После того, как все зависимости и версии библиотек определены плагин выполняет проверку. Правила проверки следующие:
 * <ol>
 * <li>Если изменение версии библиотеки связано с фиксацией версии, плагин остановит билд с ошибкой.</li>
 * <li>Если изменение версии библиотеки не связано с фиксацией версии, то билд допускается к выполнению.</li>
 * </ol>
 * В большинстве случаев не возможно подобрать такой набор версий библиотек, который бы удовлетворил всем подключаемым библиотекам
 * прямо и по транзититивным зависимостям. Поэтому плагин поддерживает введение исключение из правил. Удостоверившись,
 * что более новая версия библиотеки полностью обратно совместима со старой версии, можно разрешить обновление с одной версии
 * библиоетки до другой.
 * <p>
 * Правила исключения описываются в property файле. По умолчанию используется файл
 * с названием <b>"library_versions_exclusions.properties"</b>, расположенный в корне проекта.
 * Однако, плагин позволяет переопределить название и место расположение такого файла.
 *
 * @author Brovin Yaroslav
 * @since 09.01.2017
 */
public class CheckDependenciesPlugin implements Plugin<Project> {
    /**
     * Имя таски, добавляемой плагином при подключении к проекту
     */
    public static final String CHECK_DEPENDENCIES_TASK_NAME = "checkLibraryDependencies";
    private static final String PRINT_NEW_DEPENDENCIES_TASK_NAME_BY_GROUP = "printNewDependenciesByGroup";
    private static final String PRINT_NEW_DEPENDENCIES_TASK_NAME = "printNewDependencies";
    private static final String PRINT_ACTUAL_DEPENDENCIES_TASK_NAME_BY_GROUP = "printActualDependenciesByGroup";
    private static final String PRINT_ACTUAL_DEPENDENCIES_TASK_NAME = "printActualDependencies";
    private static final String SNAPSHOT_CHECK_TASK_NAME = "checkSnapshotsDependencies";
    private static final String FORBIDDEN_DEPENDENCIES_CHECK_TASK_NAME = "checkForbiddenDependencies";

    private static final String PRINT_DEPENDENCIES_TASK_GROUP = "printDependenciesVersions";

    private static final String VERIFICATION_TASK_GROUP = "verification";
    private static final String CHECK_DEPENDENCIES_TASK_DESCRIPTION = "Checks current used libraries versions on " +
            "conflict with platform libraries version.";
    private static final String CHECK_DEPENDENCIES_EXTENSION_NAME = "checkDependencies";

    private static final String MAJOR_VERSION_CHECKER_EXTENSION_NAME = "majorVersionChecker";
    private static final String FORBIDDEN_DEPENDENCIES_EXTENSION_NAME = "forbiddenDependenciesChecker";

    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(DependencyManagementPlugin.class);

        CheckDependenciesPluginExtension checkDependenciesExtension = new CheckDependenciesPluginExtension();
        target.getExtensions().add(CHECK_DEPENDENCIES_EXTENSION_NAME, checkDependenciesExtension);

        CheckDependenciesTask task = createCheckDependenciesTask(target);
        target.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(task);

        task.getConventionMapping().map("includedConfigurations",
                () -> checkDependenciesExtension.includedConfigurations);

        MajorVersionCheckerExtension majorVersionCheckerExtension = new MajorVersionCheckerExtension();
        target.getExtensions().add(MAJOR_VERSION_CHECKER_EXTENSION_NAME, majorVersionCheckerExtension);

        ForbiddenDependenciesExtension forbiddenDependenciesExtension = new ForbiddenDependenciesExtension();
        target.getExtensions().add(FORBIDDEN_DEPENDENCIES_EXTENSION_NAME, forbiddenDependenciesExtension);

        CheckForbiddenDependenciesTask checkForbiddenDependenciesTask = createCheckForbiddenDependenciesTask(target);
        task.dependsOn(checkForbiddenDependenciesTask);

        // Запуск проверки конфликтов мажорных версий и вывода новых версий зависимостей
        target.afterEvaluate(project -> {
            Set<String> urls = project.getRepositories().stream()
                    .filter(repo -> repo instanceof MavenArtifactRepository)
                    .map(repo -> ((MavenArtifactRepository) repo).getUrl().toString())
                    .collect(Collectors.toSet());

            ArtifactVersionResolver artifactVersionResolver = new ArtifactVersionResolver(urls);

            if (majorVersionCheckerExtension.enabled) {
                        VersionChecker.runCheckVersion(project, majorVersionCheckerExtension, artifactVersionResolver);
                    }

                    createPrintNewDependenciesGroupTask(target, checkDependenciesExtension.includeGroupIdForPrintDependencies,
                                                              artifactVersionResolver);
                    createPrintNewDependenciesTask(target, artifactVersionResolver);

                    createPrintActualDependenciesByGroupTask(target, checkDependenciesExtension.includeGroupIdForPrintDependencies)
                            .dependsOn(task);
                    createPrintActualDependenciesTask(target)
                            .dependsOn(task);
                    createCheckSnapshotTask(target);

                    checkForbiddenDependenciesTask
                            .setForbiddenArtifacts(forbiddenDependenciesExtension.forbiddenArtifacts);
                }
        );
    }

    /**
     * Создает задачу проверки версий библиотек
     *
     * @param project проект
     * @return задача проверки зависимостей
     */
    private static CheckDependenciesTask createCheckDependenciesTask(@Nonnull Project project) {
        CheckDependenciesTask task = project.getTasks().create(CHECK_DEPENDENCIES_TASK_NAME, CheckDependenciesTask.class);
        task.setGroup(VERIFICATION_TASK_GROUP);
        task.setDescription(CHECK_DEPENDENCIES_TASK_DESCRIPTION);

        return task;
    }

    /**
     * Создает задачу вывода новых версий библиотек по переданному списку необходмых префиксов
     * CheckDependenciesPluginExtension.includeGroupIdPrefixes
     *
     * @param project проект
     */
    private static void createPrintNewDependenciesGroupTask(@Nonnull Project project,
                                                            @Nonnull Set<String> includeGroupIdPrefixes,
                                                            @Nonnull ArtifactVersionResolver artifactVersionResolver) {
        PrintDependenciesByGroupTask task = project.getTasks()
                .create(PRINT_NEW_DEPENDENCIES_TASK_NAME_BY_GROUP, PrintDependenciesByGroupTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints new available versions of dependencies by group list");
        task.setIncludeGroupIdPrefixes(includeGroupIdPrefixes);
        task.setArtifactVersionResolver(artifactVersionResolver);
    }

    /**
     * Создает задачу вывода актуальных версий библиотек по переданному списку необходмых префиксов
     * CheckDependenciesPluginExtension.includeGroupIdForPrintDependencies
     *
     * @param project проект
     */
    private static PrintActualDependenciesByGroupTask createPrintActualDependenciesByGroupTask(
            @Nonnull Project project,
            @Nonnull Set<String> includeGroupIdPrefixes
    ) {
        PrintActualDependenciesByGroupTask task = project.getTasks()
                .create(PRINT_ACTUAL_DEPENDENCIES_TASK_NAME_BY_GROUP, PrintActualDependenciesByGroupTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints actual versions of dependencies by list");
        task.setIncludeGroupIdPrefixes(includeGroupIdPrefixes);
        return task;
    }

    /**
     * Создает задачу вывода актуальных версий всех библиотек
     *
     * @param project проект
     */
    private static PrintAllActualDependenciesTask createPrintActualDependenciesTask(@Nonnull Project project) {
        PrintAllActualDependenciesTask task = project.getTasks()
                .create(PRINT_ACTUAL_DEPENDENCIES_TASK_NAME, PrintAllActualDependenciesTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints actual versions of all dependencies");
        return task;
    }

    /**
     * Создает задачу вывода новых версий всех библиотек
     *
     * @param project проект
     */
    private static void createPrintNewDependenciesTask(@Nonnull Project project,
                                                       @Nonnull ArtifactVersionResolver artifactVersionResolver) {
        PrintNewDependenciesVersionsTask task = project.getTasks()
                .create(PRINT_NEW_DEPENDENCIES_TASK_NAME, PrintNewDependenciesVersionsTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints new available versions of dependencies");
        task.setArtifactVersionResolver(artifactVersionResolver);

    }


    /**
     * Создает задачу по проверке snapshot-зависимостей
     *
     * @param project проект
     */
    private static void createCheckSnapshotTask(@Nonnull Project project) {
        CheckSnapshotsDependenciesTask task = project.getTasks()
                .create(SNAPSHOT_CHECK_TASK_NAME, CheckSnapshotsDependenciesTask.class);

        task.setGroup(VERIFICATION_TASK_GROUP);
        task.setDescription("Check snapshot dependencies");
    }

    /**
     * Создает задачу по проверке наличия запрещенных зависимостей
     *
     * @param project проект
     */
    private static CheckForbiddenDependenciesTask createCheckForbiddenDependenciesTask(@Nonnull Project project) {
        CheckForbiddenDependenciesTask task = project.getTasks()
                .create(FORBIDDEN_DEPENDENCIES_CHECK_TASK_NAME, CheckForbiddenDependenciesTask.class);

        task.setGroup(VERIFICATION_TASK_GROUP);
        task.setDescription("Check forbidden dependencies");
        return task;
    }
}