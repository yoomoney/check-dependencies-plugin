package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

/**
 * Общий интерфейс, который предоставляет базовую информацию об артефакте:
 * <ul>
 *     <li>имя артефакта</li>
 *     <li>прямые зависимости от других артефактов</li>
 * </ul>
 *
 * @param <T> тип артефакта
 * @author Konstantin Novokreshchenov
 * @since 15.03.2017
 */
public interface Artifact<T extends Artifact<T>> extends ArtifactDependent<T> {
    ArtifactName getName();
}
