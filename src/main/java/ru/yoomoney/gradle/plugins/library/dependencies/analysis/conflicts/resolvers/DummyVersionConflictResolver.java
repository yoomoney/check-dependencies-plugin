package ru.yoomoney.gradle.plugins.library.dependencies.analysis.conflicts.resolvers;

import java.util.Collections;

/**
 * Резолвер конфликтов, возвращающий для всех конфликтов пустой результат
 *
 * @author Konstantin Novokreshchenov
 * @since 12.04.2017
 */
public class DummyVersionConflictResolver implements VersionConflictResolver {
    @Override
    public ConflictPathResolutionResult resolveConflict(VersionConflictInfo conflictInfo) {
        return new ConflictPathResolutionResult(conflictInfo.getDirectDependency(), Collections.emptySet(), Collections.emptySet());
    }
}
