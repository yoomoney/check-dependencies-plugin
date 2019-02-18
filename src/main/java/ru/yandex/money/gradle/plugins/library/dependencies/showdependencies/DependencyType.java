package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

import org.gradle.api.artifacts.Dependency;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

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
    INNER(DependencyType::isInnerDependencies, "inner"),
    /**
     * Внешние библиотеки
     */
    OUTER(INNER.predicate.negate(), "outer");

    private final Predicate<Dependency> predicate;
    private final String code;

    DependencyType(@Nonnull Predicate<Dependency> predicate,
                   @Nonnull String code) {
        this.predicate = requireNonNull(predicate);
        this.code = requireNonNull(code);
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
        if (dependency.getGroup() != null) {
            return dependency.getGroup().startsWith("ru.yandex.money") || dependency.getGroup().startsWith("ru.yamoney");
        }
        return false;
    }

    @Nonnull
    public String getCode() {
        return code;
    }
}
