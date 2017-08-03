package ru.yandex.money.gradle.plugins.library.dependencies;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.VersionSelectors;

import javax.annotation.Nonnull;

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

    private static final String CHECK_DEPENDENCIES_TASK_GROUP = "verification";
    private static final String CHECK_DEPENDENCIES_TASK_DESCRIPTION = "Checks current used libraries versions on " +
                                                                      "conflict with platform libraries version.";
    private static final String CHECK_DEPENDENCIES_EXTENSION_NAME = "checkDependencies";
    private static final String SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID = "io.spring.dependency-management";
    private static final String ERROR_APPLYING_PLUGIN_REQUIRED = "\"%s\" plugin is required for correct working of check " +
                                                                  "dependencies plugin.%n Apply this plugin in build script.";

    @Override
    public void apply(Project target) {
        if (!target.getPluginManager().hasPlugin(SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID)) {
            throw new GradleException(String.format(ERROR_APPLYING_PLUGIN_REQUIRED, SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID));
        }

        CheckDependenciesPluginExtension extension = new CheckDependenciesPluginExtension();
        target.getExtensions().add(CHECK_DEPENDENCIES_EXTENSION_NAME, extension);

        CheckDependenciesTask task = createCheckDependenciesTask(target);
        target.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(task);

        // В момент примения плагина ни один extension еще не объявлен. Поэтому брать оттуда значения до окончания формирования
        // проекта бессмысленно. Поэтому вытаскиваем имя файла исключений после того, как проект полностью сформирован.
        // Так же стоит обратить внимание, что ConventionMapping - это список значений для свойств таски и значение из него берется
        // только, если одноименное свойство в таске имеет null значение.
        task.getConventionMapping().map("exclusionsRulesSources", () -> extension.exclusionsRulesSources);
        task.getConventionMapping().map("excludedConfigurations", () -> extension.excludedConfigurations);
        task.getConventionMapping().map("versionSelectors", () -> new VersionSelectors(extension.versionSelectors));
    }

    /**
     * Создает задачу проверки версий библиотек
     *
     * @param project проект
     * @return задача проверки зависимостей
     */
    private static CheckDependenciesTask createCheckDependenciesTask(@Nonnull Project project) {
        CheckDependenciesTask task = project.getTasks().create(CHECK_DEPENDENCIES_TASK_NAME, CheckDependenciesTask.class);
        task.setGroup(CHECK_DEPENDENCIES_TASK_GROUP);
        task.setDescription(CHECK_DEPENDENCIES_TASK_DESCRIPTION);

        return task;
    }
}