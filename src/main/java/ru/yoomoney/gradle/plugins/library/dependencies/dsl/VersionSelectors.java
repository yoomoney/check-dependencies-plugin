package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

import groovy.lang.Closure;
import ru.yoomoney.gradle.plugins.library.dependencies.reporters.NameFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Представляет собой совокупность зарегистрированных селекторов версий для библиотек
 *
 * @author Konstantin Novokreshchenov
 * @since 12.04.2017
 */
public class VersionSelectors {
    private static final Predicate<String> NO_VERSION_SELECTOR = version -> false;
    private final Map<String, Predicate<String>> libraryVersionSelectors = new HashMap<>();

    public VersionSelectors(Map<String, Closure<Boolean>> libraryVersionSelectors) {
        libraryVersionSelectors.entrySet().forEach(entry -> {
            Closure<Boolean> versionSelector = entry.getValue();
            this.libraryVersionSelectors.put(entry.getKey(), versionSelector::call);
        });
    }

    /**
     * Возвращает для библиотеки зарегистрированный селектор версий.
     * Если для данной библиотеки селектора версий нет,
     * то возвращает селектор версий, возращающий для всех версий false
     *
     * @param library имя библиотеки
     * @return зарегистрированный селектор версий или селектор версий по умолчанию
     */
    public Predicate<String> forLibrary(LibraryName library) {
        String libraryName = NameFormatter.format(library);
        return libraryVersionSelectors.containsKey(libraryName)
                ? libraryVersionSelectors.get(libraryName)
                : NO_VERSION_SELECTOR;
    }

    public int count() {
        return libraryVersionSelectors.size();
    }
}
