package ru.yoomoney.gradle.plugins.library

/**
 * @author Konstantin Novokreshchenov
 * @since 13.03.2017
 */
class TestRepositories {
    private TestRepositories() { }

    static String MAVEN_REPO_1 = repoUrlByName('maven-repo-1')
    static String MAVEN_REPO_2 = repoUrlByName('maven-repo-2')

    public static String repoUrlByName(String repoName) {
        new File("src/test/resources/repositories/${repoName}").toURI().toURL().toString()
    }
}
