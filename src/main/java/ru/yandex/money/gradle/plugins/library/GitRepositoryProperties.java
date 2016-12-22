package ru.yandex.money.gradle.plugins.library;

import org.ajoberstar.grgit.Grgit;

/**
 * Утилиты для работы с git, общие для нашего проекта.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
public class GitRepositoryProperties {
    private static final String MASTER_BRANCH_NAME = "master";

    /**
     * Показывает, является ли текущая ветка master веткой или нет.
     *
     * @return true, если является, false - если нет.
     */
    public static boolean isMasterBranch() {
        return Grgit.open().getBranch().getCurrent().getName().equalsIgnoreCase(MASTER_BRANCH_NAME);
    }
}
