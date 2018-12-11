package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.artifacts.Dependency;

import java.util.function.Predicate;

/**
 * Типы зависимостей -  или внешние
 *
 * @author horyukova
 * @since 10.12.2018
 */
public enum DependencyType {
    /**
     * Внутренние библиотеки (пакеты ru.yamoney)
     */
    INNER(DependencyType::isInnerDependencies),
    /**
     * Внешние библиотеки
     */
    OUTER(INNER.predicate.negate());

    private final Predicate<Dependency> predicate;

    DependencyType(Predicate<Dependency> predicate) {
        this.predicate = predicate;
    }

    /**
     * Соответствует ли библиотека условию поиска - внутренняя библиотка при INNER поиске, внешняя при OUTER поиске
     *
     * @param dependency библиотека
     * @return true, если соответствует
     */
    boolean isCorresponds(Dependency dependency) {
        return predicate.test(dependency);
    }

    private static boolean isInnerDependencies(Dependency dependency) {
        return dependency.getGroup().startsWith("ru.yandex.money")
                || dependency.getGroup().startsWith("ru.yamoney");
    }
}
