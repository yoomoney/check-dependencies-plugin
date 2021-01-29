package ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

/**
 * Резолвер конфликтов версий.
 * Разрешение конфликта версий происходит с помощью анализа пути зависимостей, приводящего к конфликте,
 * и поиска для данной прямой зависимости в пути зависимостей альтернативных версий, не приводяющих к данному конфликту
 *
 * @author Konstantin Novokreshchenov
 * @since 12.04.2017
 */
@FunctionalInterface
public interface VersionConflictResolver {
    ConflictPathResolutionResult resolveConflict(VersionConflictInfo conflictInfo);
}
