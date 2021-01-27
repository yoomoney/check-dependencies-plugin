package ru.yoomoney.gradle.plugins.library.dependencies.dsl;

import javax.annotation.Nonnull;

/**
 * Класс, хранящий информацию о запрещенных артефактов
 *
 * @author horyukova
 * @since 31.05.2019
 */
public class ForbiddenArtifactInfo {
    private final ArtifactWithVersionRange forbiddenArtifact;
    private final ArtifactName recommendedArtifact;
    private final String comment;

    private ForbiddenArtifactInfo(ArtifactWithVersionRange forbiddenArtifact,
                                  ArtifactName recommendedArtifact,
                                  String comment) {
        this.forbiddenArtifact = forbiddenArtifact;
        this.recommendedArtifact = recommendedArtifact;
        this.comment = comment;
    }

    /**
     * Создает новый объект билдера для {@link ForbiddenArtifactInfo}
     *
     * @return new Builder()
     */
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public ArtifactWithVersionRange getForbiddenArtifact() {
        return forbiddenArtifact;
    }

    @Nonnull
    public ArtifactName getRecommendedArtifact() {
        return recommendedArtifact;
    }

    @Nonnull
    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "ForbiddenArtifactInfo{" +
                "forbiddenArtifact=" + forbiddenArtifact +
                ", recommendedArtifact=" + recommendedArtifact +
                ", comment='" + comment + '\'' +
                '}';
    }

    /**
     * Билдер для {@link ForbiddenArtifactInfo}
     */
    public static final class Builder {
        private ArtifactWithVersionRange forbiddenArtifact;
        private ArtifactName recommendedArtifact;
        private String comment;

        private Builder() {
        }

        public Builder withForbiddenArtifact(ArtifactWithVersionRange forbiddenArtifact) {
            this.forbiddenArtifact = forbiddenArtifact;
            return this;
        }

        public Builder withRecommendedArtifact(ArtifactName recommendedArtifact) {
            this.recommendedArtifact = recommendedArtifact;
            return this;
        }

        public Builder withComment(String comment) {
            this.comment = comment;
            return this;
        }

        public ForbiddenArtifactInfo build() {
            return new ForbiddenArtifactInfo(forbiddenArtifact, recommendedArtifact, comment);
        }
    }
}
