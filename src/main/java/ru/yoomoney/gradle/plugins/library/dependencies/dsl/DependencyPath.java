package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Представляет путь до некоторой зависимости проекта (или библиотеки).
 * Зависимость проекта (или библиотеки) может быть прямой или транзитивной.
 * Путь до прямой зависимости проекта (или библиотеки) включает в себя только эту прямую зависимость.
 * Путь до транзитивной зависимости начинается с прямой зависимости проекта (или библиотеки),
 * содержит все промежуточные зависимости (зависимости зависимостей) и саму транзитивную зависимость.
 *
 * @param <T> тип артефакта, реализующий интерфейс {@link Artifact}
 * @author Konstantin Novokreshchenov
 * @since 13.03.2017
 */
public class DependencyPath<T extends Artifact<T>> implements Iterable<T> {
    private LinkedList<T> dependencies;

    /**
     * Конструктор класса
     *
     * @param dependencies последовательность зависимостей, образующий путь от прямой зависимости до таргетной
     */
    public DependencyPath(LinkedList<T> dependencies) {
        if (dependencies.size() < 1) {
            throw new IllegalArgumentException("Unexpected dependency path length! " +
                                               "Passed list must contain at least target dependency");
        }
        this.dependencies = dependencies;
    }

    /**
     * Возвращает прямую (корневую) зависимость
     *
     * @return прямая (корневая) зависимость
     */
    public T getRoot() {
        return dependencies.getFirst();
    }

    /**
     * Возвращает целевую зависимость
     *
     * @return целевая зависимость
     */
    public T getTargetDependency() {
        return dependencies.getLast();
    }

    @Override
    public Iterator<T> iterator() {
        return dependencies.iterator();
    }
}
