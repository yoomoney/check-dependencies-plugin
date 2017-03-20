package ru.yandex.money.gradle.plugins.library.dependencies.exclusions;

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru)
 * @since 19.03.2017
 */
public final class ExclusionRule {
    private final String library;
    private final String requestedVersion;
    private final String fixedVersion;

    public ExclusionRule(String library, String requestedVersion, String fixedVersion) {
        this.library = library;
        this.requestedVersion = requestedVersion;
        this.fixedVersion = fixedVersion;
    }

    public String getLibrary() {
        return library;
    }

    public String getRequestedVersion() {
        return requestedVersion;
    }

    public String getFixedVersion() {
        return fixedVersion;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if ((obj == null) || !(obj instanceof ExclusionRule)) return false;

        ExclusionRule other = (ExclusionRule)obj;
        return library.equals(other.library) &&
               requestedVersion.equals(other.requestedVersion) &&
               fixedVersion.equals(other.fixedVersion);
    }
}
