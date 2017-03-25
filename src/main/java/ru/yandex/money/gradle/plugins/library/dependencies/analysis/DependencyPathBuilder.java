package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.Artifact;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.DependencyPath;

import java.util.LinkedList;

/**
 * Хранит текущий построенный путь до зависимости
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
public class DependencyPathBuilder<T extends Artifact<T>> implements Cloneable {
    private final LinkedList<T> dependencies;

    public static <T extends Artifact<T>> DependencyPathBuilder<T> create() {
        return new DependencyPathBuilder<>();
    }

    private DependencyPathBuilder() {
        this(new LinkedList<>());
    }

    private DependencyPathBuilder(LinkedList<T> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Добавляет в конец текущего пути данную зависимость
     *
     * @param dependency добавляемая зависимость
     * @return текущий инстанс билдера
     */
    public DependencyPathBuilder<T> add(T dependency) {
        dependencies.add(dependency);
        return this;
    }

    /**
     * Создает новый билдер, который хранит копию текущего пути до зависимости
     *
     * @return новый экземпляр билдера
     */
    @SuppressWarnings("unchecked")
    public DependencyPathBuilder<T> copy() {
        return new DependencyPathBuilder((LinkedList<T>) dependencies.clone());
    }

    /**
     * Возвращает построенный путь до зависимости
     *
     * @return путь до зависимости
     */
    public DependencyPath<T> build() {
        return new DependencyPath<>(dependencies);
    }

}
