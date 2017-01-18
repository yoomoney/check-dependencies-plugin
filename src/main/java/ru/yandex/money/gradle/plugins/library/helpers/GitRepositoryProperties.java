package ru.yandex.money.gradle.plugins.library.helpers;

import org.ajoberstar.grgit.Grgit;
import org.ajoberstar.grgit.operation.OpenOp;

import java.util.regex.Pattern;

/**
 * Утилитный класс для получения свойств git-репозитория.
 *
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @author Konstantin Rashev (rashev@yamoney.ru)
 * @since 22.12.2016
 */
public class GitRepositoryProperties {

    private static final String MASTER_BRANCH_NAME = "master";
    private static final String DEV_BRANCH_NAME = "dev";
    private static final Pattern RELEASE_BRANCH_PATTERN = Pattern.compile("release/.*");

    private final Grgit grgit;

    /**
     * Конструктор класса. Инициализирует работу с git-репозиторием.
     * Поиск git-репозитория начинается с директории, указанной в baseDir.
     *
     * @param baseDir - директория, начиная с которой идет поиск git-репозитория.
     */
    public GitRepositoryProperties(String baseDir) {
        OpenOp grgitOpenOperation = new OpenOp();
        grgitOpenOperation.setCurrentDir(baseDir);
        grgit = grgitOpenOperation.call();
    }

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
        return getCurrentBranchName().equalsIgnoreCase(DEV_BRANCH_NAME);
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
