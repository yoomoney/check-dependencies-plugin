package ru.yandex.money.gradle.plugins.library.dependencies.analysis;

import ru.yandex.money.gradle.plugins.library.dependencies.dsl.Artifact;
import ru.yandex.money.gradle.plugins.library.dependencies.dsl.DependencyPath;

import java.util.LinkedList;

/**
 * Последовательно строит путь до зависимости
 *
 * @param <ArtifactT> тип артефакта
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 13.03.2017
 */
class DependencyPathBuilder<ArtifactT extends Artifact<ArtifactT>> implements Cloneable {
    private final LinkedList<ArtifactT> dependencies;

    /**
     * Создает новый инстанс класса с пустым путем зависимых артефактов
     *
     * @param <T> тип артефакта
     * @return новый инстанс класса
     */
    static <T extends Artifact<T>> DependencyPathBuilder<T> create() {
        return new DependencyPathBuilder<>();
    }

    private DependencyPathBuilder() {
        this(new LinkedList<>());
    }

    private DependencyPathBuilder(LinkedList<ArtifactT> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * Добавляет в конец текущего пути данную зависимость
     *
     * @param dependency добавляемая зависимость
     * @return текущий инстанс билдера
     */
    DependencyPathBuilder<ArtifactT> add(ArtifactT dependency) {
        dependencies.add(dependency);
        return this;
    }

    /**
     * Создает новый билдер, который хранит копию текущего пути до зависимости
     *
     * @return новый экземпляр билдера
     */
    @SuppressWarnings("unchecked")
    DependencyPathBuilder<ArtifactT> copy() {
        return new DependencyPathBuilder((LinkedList<ArtifactT>) dependencies.clone());
    }

    /**
     * Возвращает построенный путь до зависимости
     *
     * @return путь до зависимости
     */
    DependencyPath<ArtifactT> build() {
        return new DependencyPath<>(dependencies);
    }

}
