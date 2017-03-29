package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

import java.util.Objects;

/**
 * Представляет исключение из правила соответствия требуемой и фиксированной версиями библиотеки
 * во время сборки проекта
 *
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public final class ExclusionRule {
    private final String library;
    private final String requestedVersion;
    private final String fixedVersion;

    /**
     * Конструктор класса
     *
     * @param library имя библиотеки, для которой определяется исключение
     * @param requestedVersion требуемая версия библиотеки
     * @param fixedVersion зафиксированная версия библиотеки
     */
    ExclusionRule(String library, String requestedVersion, String fixedVersion) {
        this.library = library;
        this.requestedVersion = requestedVersion;
        this.fixedVersion = fixedVersion;
    }

    /**
     * Возвращает имя библиотеки
     *
     * @return имя библиотеки
     */
    public String getLibrary() {
        return library;
    }

    /**
     * Возвращает требуемую версию библиотеки
     *
     * @return требуемая версия библиотеки
     */
    public String getRequestedVersion() {
        return requestedVersion;
    }

    /**
     * Возвращает зафиксированную версию библиотеки
     *
     * @return зафиксированная версия библиотеки
     */
    public String getFixedVersion() {
        return fixedVersion;
    }

    @Override
    public int hashCode() {
        return 7 * library.hashCode() + 5 * requestedVersion.hashCode() + fixedVersion.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExclusionRule)) {
            return false;
        }

        ExclusionRule other = (ExclusionRule)obj;
        return Objects.equals(library, other.library) &&
               Objects.equals(requestedVersion, other.requestedVersion) &&
               Objects.equals(fixedVersion, other.fixedVersion);
    }
}
