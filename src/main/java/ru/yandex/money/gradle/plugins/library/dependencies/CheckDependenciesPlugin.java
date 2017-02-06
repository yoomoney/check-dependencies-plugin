package ru.yandex.money.gradle.plugins.library.dependencies;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;

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
 * Правила исключения описываются в property файле. По умолчанию используется файл с названеим <c>library_versions_exclusions.properties</c>
 * расположенные в корне проекта. Однако, плагин позволяет переопределить название и место расположение такого файла. Для этого
 * используется расширение плагина:
 * <p>
 * <code>
 * checkDependencies {
 *     fileName = "Путь к файлу"
 * }
 * </code>
 *
 * @author Brovin Yaroslav (brovin@yamoney.ru)
 * @since 09.01.2017
 */
public class CheckDependenciesPlugin implements Plugin<Project> {

    public static final String CHECK_DEPENDENCIES_TASK_NAME = "checkLibraryDependencies";
    private static final String CHECK_DEPENDENCIES_TASK_GROUP = "verification";
    private static final String CHECK_DEPENDENCIES_TASK_DESCRIPTION = "Checks current used libraries versions on " +
                                                                      "conflict with platform libraries version.";
    private static final String SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID = "io.spring.dependency-management";
    private static final String ERROR_APPLYING_PLUGIN_REQUIRED = "\"%s\" plugin is required for correct working of check " +
                                                                  "dependencies plugin.\n Apply this plugin in build script.";

    @Override
    public void apply(Project target) {
        if (!target.getPluginManager().hasPlugin(SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID)) {
            throw new GradleException(String.format(ERROR_APPLYING_PLUGIN_REQUIRED, SPRING_DEPENDENCY_MANAGEMENT_PLUGIN_ID));
        }

        target.getExtensions().create(CheckDependenciesPluginExtension.EXTENSION_NAME, CheckDependenciesPluginExtension.class, target);

        Task task = createCheckDependenciesTask(target);
        target.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(task);
    }

    /**
     * Создает задачу проверки версий библиотек
     *
     * @param project проект
     * @return задача проверки зависимостей
     */
    private static Task createCheckDependenciesTask(@Nonnull Project project) {
        CheckDependenciesTask task = project.getTasks().create(CHECK_DEPENDENCIES_TASK_NAME, CheckDependenciesTask.class);
        task.setGroup(CHECK_DEPENDENCIES_TASK_GROUP);
        task.setDescription(CHECK_DEPENDENCIES_TASK_DESCRIPTION);

        return task;
    }
}