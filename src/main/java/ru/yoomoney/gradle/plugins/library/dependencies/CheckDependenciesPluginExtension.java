package ru.yoomoney.gradle.plugins.library.dependencies;

import org.gradle.api.tasks.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Класс, позволяющий настраивать CheckDependenciesPlugin.
 *
 * @author Brovin Yaroslav
 * @since 05.02.2017
 */
@SuppressWarnings("WeakerAccess")
public class CheckDependenciesPluginExtension {
    /**
     * Список конфигурация для которых не требуется выполнять проверку версий библиотек.
     */
    @Input
    @Deprecated
    public List<String> excludedConfigurations = new ArrayList<>();

    /**
     * Список конфигурация для которых требуется выполнять проверку версий библиотек.
     * Включенные по умолчанию конфигурацию являются наследниками, т.е. включают в себя все нужные для проверок конфигураций -
     * compile, implementation, testCompile, testImplementation, runtime
     */
    @Input
    public List<String> includedConfigurations = Arrays.asList("componentTestCompileClasspath", "slowTestCompileClasspath",
            "testCompileClasspath", "default");

    /**
     * Список префиксов groupId библиотек, для которых требуется вывести новые доступные версии зависимостей
     * в таске printNewDependenciesVersionsByIncludeList
     */
    @Input
    public Set<String> includeGroupIdForPrintDependencies = new HashSet<>();
}
