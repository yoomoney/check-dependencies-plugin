package ru.yandex.money.gradle.plugins.library.helpers;

import org.ajoberstar.grgit.Grgit;

import java.util.regex.Pattern;

/**
 * Утилиты для работы с git, общие для нашего проекта.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */

public class GitRepositoryProperties {

    private static final String MASTER_BRANCH_NAME = "master";
    private static final String DEV_BRUNCH_NAME = "dev";
    private static final Pattern RELEASE_BRANCH_PATTERN = Pattern.compile("release/.*");

    private final Grgit grgit = Grgit.open();

    /**
     * Показывает, является ли текущая ветка master веткой или нет.
     *
     * @return true, если является, false - если нет.
     */
    public boolean isMasterBranch() {
        return getCurrentBranchName().equalsIgnoreCase(MASTER_BRANCH_NAME);
    }

    /**
     * Показывает, является ли текущая ветра dev веткой или нет.
     *
     * @return true, если является, false - если нет.
     */
    public boolean isDevBranch() {
        return getCurrentBranchName().equalsIgnoreCase(DEV_BRUNCH_NAME);
    }

    /**
     * Показывает, является ли текущая ветра релизной или нет.
     *
     * @return true, если является, false - если нет.
     */
    public boolean isReleaseBranch() {
        return RELEASE_BRANCH_PATTERN.matcher(getCurrentBranchName()).find();
    }

    /**
     * Возвращает имя текущей ветки.
     *
     * @return имя текущей ветки.
     */
    public String getCurrentBranchName() {
        return grgit.getBranch().getCurrent().getName();
    }

}
