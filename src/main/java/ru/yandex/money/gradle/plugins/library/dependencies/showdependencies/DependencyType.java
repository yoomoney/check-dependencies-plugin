package ru.yandex.money.gradle.plugins.library.dependencies.showdependencies;

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
    INNER,
    /**
     * Внешние библиотеки
     */
    OUTER
}
