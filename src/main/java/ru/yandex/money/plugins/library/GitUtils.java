package ru.yandex.money.plugins.library;

import org.ajoberstar.grgit.Grgit;

/**
 * @author Kirill Bulatov (mail4score@gmail.com)
 * @since 22.12.2016
 */
public class GitUtils {
    private static final String MASTER_BRANCH_NAME = "master";

    public static boolean isMasterBranch() {
        return Grgit.open().getBranch().getCurrent().getName().equalsIgnoreCase(MASTER_BRANCH_NAME);
    }
}
