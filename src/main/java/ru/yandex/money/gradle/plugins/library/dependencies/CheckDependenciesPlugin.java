package ru.yandex.money.gradle.plugins.library.dependencies;

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import ru.yandex.money.gradle.plugins.library.dependencies.checkversion.MajorVersionCheckerExtension;
import ru.yandex.money.gradle.plugins.library.dependencies.checkversion.VersionChecker;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.LibraryName;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.VersionSelectors;
import ru.yandex.money.gradle.plugins.library.dependencies.forbiddenartifacts.CheckForbiddenDependenciesTask;
import ru.yandex.money.gradle.plugins.library.dependencies.forbiddenartifacts.ForbiddenDependenciesExtension;
import ru.yandex.money.gradle.plugins.library.dependencies.showdependencies.PrintActualInnerDependenciesVersionsTask;
import ru.yandex.money.gradle.plugins.library.dependencies.showdependencies.PrintActualOuterDependenciesVersionsTask;
import ru.yandex.money.gradle.plugins.library.dependencies.showdependencies.PrintInnerDependenciesVersionsTask;
import ru.yandex.money.gradle.plugins.library.dependencies.showdependencies.PrintOuterDependenciesVersionsTask;
import ru.yandex.money.gradle.plugins.library.dependencies.snapshot.CheckSnapshotsDependenciesTask;

import javax.annotation.Nonnull;
import java.util.List;
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
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 09.01.2017
 */
public class CheckDependenciesPlugin implements Plugin<Project> {
    /**
     * Имя таски, добавляемой плагином при подключении к проекту
     */
    public static final String CHECK_DEPENDENCIES_TASK_NAME = "checkLibraryDependencies";
    private static final String PRINT_INNER_DEPENDENCIES_TASK_NAME = "printNewInnerDependenciesVersions";
    private static final String PRINT_OUTER_DEPENDENCIES_TASK_NAME = "printNewOuterDependenciesVersions";
    private static final String PRINT_ACTUAL_INNER_DEPENDENCIES_TASK_NAME = "printActualInnerDependenciesVersions";
    private static final String PRINT_ACTUAL_OUTER_DEPENDENCIES_TASK_NAME = "printActualOuterDependenciesVersions";
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

        // В момент примения плагина ни один extension еще не объявлен. Поэтому брать оттуда значения до окончания формирования
        // проекта бессмысленно. Поэтому вытаскиваем имя файла исключений после того, как проект полностью сформирован.
        // Так же стоит обратить внимание, что ConventionMapping - это список значений для свойств таски и значение из него берется
        // только, если одноименное свойство в таске имеет null значение.
        task.getConventionMapping().map("exclusionsRulesSources",
                () -> checkDependenciesExtension.exclusionsRulesSources);
        task.getConventionMapping().map("includedConfigurations",
                () -> checkDependenciesExtension.includedConfigurations);
        task.getConventionMapping().map("versionSelectors",
                () -> new VersionSelectors(checkDependenciesExtension.versionSelectors));

        MajorVersionCheckerExtension majorVersionCheckerExtension = new MajorVersionCheckerExtension();
        target.getExtensions().add(MAJOR_VERSION_CHECKER_EXTENSION_NAME, majorVersionCheckerExtension);

        ForbiddenDependenciesExtension forbiddenDependenciesExtension = new ForbiddenDependenciesExtension();
        target.getExtensions().add(FORBIDDEN_DEPENDENCIES_EXTENSION_NAME, forbiddenDependenciesExtension);

        CheckForbiddenDependenciesTask checkForbiddenDependenciesTask = createCheckForbiddenDependenciesTask(target);
        task.dependsOn(checkForbiddenDependenciesTask);

        // Запуск проверки конфликтов мажорных версий и вывода новых версий зависимостей
        target.afterEvaluate(project -> {
                    if (majorVersionCheckerExtension.enabled) {
                        VersionChecker.runCheckVersion(project, majorVersionCheckerExtension);
                    }

                    createPrintInnerDependenciesVersionsTask(target);
                    createPrintOuterDependenciesVersionsTask(target);
                    createPrintActualInnerDependenciesVersionsTask(target).dependsOn(task);
                    createPrintActualOuterDependenciesVersionsTask(target).dependsOn(task);
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
     * Создает задачу вывода новых версий внутренний (yamoney) библиотек
     *
     * @param project проект
     */
    private static void createPrintInnerDependenciesVersionsTask(@Nonnull Project project) {
        PrintInnerDependenciesVersionsTask task = project.getTasks()
                .create(PRINT_INNER_DEPENDENCIES_TASK_NAME, PrintInnerDependenciesVersionsTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints new available versions of inner dependencies");
    }

    /**
     * Создает задачу вывода актуальных версий внутренний (yamoney) библиотек
     *
     * @param project проект
     */
    private static PrintActualInnerDependenciesVersionsTask createPrintActualInnerDependenciesVersionsTask(@Nonnull Project project) {
        PrintActualInnerDependenciesVersionsTask task = project.getTasks()
                .create(PRINT_ACTUAL_INNER_DEPENDENCIES_TASK_NAME, PrintActualInnerDependenciesVersionsTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints actual versions of inner dependencies");
        return task;
    }

    /**
     * Создает задачу вывода актуальных версий внешних библиотек
     *
     * @param project проект
     */
    private static PrintActualOuterDependenciesVersionsTask createPrintActualOuterDependenciesVersionsTask(@Nonnull Project project) {
        PrintActualOuterDependenciesVersionsTask task = project.getTasks()
                .create(PRINT_ACTUAL_OUTER_DEPENDENCIES_TASK_NAME, PrintActualOuterDependenciesVersionsTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints actual versions of outer dependencies");
        return task;
    }

    /**
     * Создает задачу вывода новых версий внешних библиотек
     *
     * @param project проект
     */
    private static void createPrintOuterDependenciesVersionsTask(@Nonnull Project project) {
        PrintOuterDependenciesVersionsTask task = project.getTasks()
                .create(PRINT_OUTER_DEPENDENCIES_TASK_NAME, PrintOuterDependenciesVersionsTask.class);
        task.setGroup(PRINT_DEPENDENCIES_TASK_GROUP);
        task.setDescription("Prints new available versions of outer dependencies");
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