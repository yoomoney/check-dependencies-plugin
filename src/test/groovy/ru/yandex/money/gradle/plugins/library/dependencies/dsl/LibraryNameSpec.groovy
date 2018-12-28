package ru.yandex.money.gradle.plugins.library.dependencies.dsl

import spock.lang.Specification

/**
 * @author Konstantin Novokreshchenov (knovokresch@yamoney.ru) 
 * @since 29.03.2017
 */
class LibraryNameSpec extends Specification {
    def 'parse correctly formatted name successfully'() {
        given:
        def library = 'org.springframework.boot:spring-boot'

        when:
        def libraryName = LibraryName.parse(library)

        then:
        libraryName.group == 'org.springframework.boot' && libraryName.name == 'spring-boot'
    }

    def 'fail to parse incorrectly formatted name'() {
        given:
        def library = 'incorrect-name'

        when:
        LibraryName.parse(library)

        then:
        thrown IllegalArgumentException
    }
}
