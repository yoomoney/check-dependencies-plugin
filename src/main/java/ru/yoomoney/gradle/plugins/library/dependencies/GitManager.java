package ru.yoomoney.gradle.plugins.library.dependencies;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Умеет работать с гитом
 *
 * @author horyukova
 * @since 18.05.2020
 */
public class GitManager implements Closeable {
    private final Git git;

    public GitManager(File projectDirectory) {
        try {
            this.git = new Git(new FileRepositoryBuilder()
                    .readEnvironment()
                    .findGitDir(projectDirectory)
                    .build());
        } catch (IOException exc) {
            throw new RuntimeException("cannot clone repository", exc);
        }
    }

    /**
     * Получить remote origin url. Насторойками bitbucket запрещён доступ по https
     * поэтому здесь будут адреса вида ssh://git@{server}/libraries/test-project.git
     */
    public String getOriginUrl() {
        return git.getRepository().getConfig().getString("remote", "origin", "url");
    }

    @Override
    public void close() {
        git.close();
    }
}
