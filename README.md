# library-plugin
Плагин создан для упрощения сборки существующих yamoney библиотек и разработки новых.

## Подключение
Для подключения в проект библиотеки этого плагина, нужно добавить в `build.gradle` библиотеки следующее:
```groovy
buildscript {
    repositories {
        maven { url 'http://nexus.yamoney.ru/content/repositories/thirdparty/' }
        maven { url 'http://nexus.yamoney.ru/content/repositories/central/' }
        maven { url 'http://nexus.yamoney.ru/content/repositories/releases/' }
        maven { url 'http://nexus.yamoney.ru/content/repositories/jcenter.bintray.com/' }
        mavenLocal()
    }
    dependencies {
        classpath 'ru.yandex.money.gradle.plugins:yamoney-library-project-plugin:0.1.0'
    }
}
```
Если блок buildscript уже есть, нужно просто добавить необходимые записи в repositories и dependencies.

После добавления зависимостей, нужно применить сам плагин, добавив строчку в `build.gradle` после обновлённого `buildscript`: 
```groovy
apply plugin: 'yamoney-library-project-plugin'
```

## Конфигурация
Задачи плагина можно настраивать.
Для этого, в `build.gradle` нужно указать имя задачи и параметры, которые вы хотите поменять. Для каждого плагина нужно указывать свою,
отдельную конфигурацию в build.gradle

Возможные конфигурации со значениями по умолчанию приведены в секции описания плагинов.

## Устройство library-plugin
В качестве агрегатора функционала выступает LibraryPlugin - в нём делаются все настройки, необходимые для нормальной работы проекта
и подключаются все дополнительные модули.

Дополнительные модули - тоже плагины, которые не видны посторонним. 
Их реализация в виде плагинов сделана для удобства, поскольку каждый модуль может настраивать корневой проект, добавляя задачи (gradle tasks) 
и возможность конфигурировать модуль из `build.gradle`

Для того, чтобы понять, как пишутся плагины, можно посмотреть [официальную документацию](https://docs.gradle.org/current/userguide/custom_plugins.html)
или книгу Gradle Beyond The Basics.

Несколько правил и рекомензаций для написания плагинов:
1. Для удобства чтения и поддержки, все плагины пишутся на Java.
1. Плагины конфигурируются с помощью `*Extension` классов - все поля этих классов должны быть `public` и не `final`.
Это позволяет переопределять эти поля в `build.gradle` так, как показано в секции выше.
1. Все классы, которые должны использоваться в gradle (`*Task`, `*Extension`, `*Plugin`) должны быть `public`, иначе возникнут ошибки на этапе выполнения задач в проекте.
1. Как можно большее число настроек должно быть со значениями по умолчанию, чтобы не заставлять пользователей плагинов указывать эти настройки вручную.

## Известные проблемы
На данный момент, для плагинов используется версия gradle 2.10, из-за чего тесты могут не проходить, если они запущены из Идеи.
Подробности:
* [https://discuss.gradle.org/t/nosuchmethoderror-in-testkit-after-2-9-2-10-transition/13505](https://discuss.gradle.org/t/nosuchmethoderror-in-testkit-after-2-9-2-10-transition/13505)
* [https://github.com/palantir/gradle-idea-test-fix](https://github.com/palantir/gradle-idea-test-fix)

# Описание плагинов

## LibraryPlugin
Плагин для объединения всех остальных плагинов, не нуждается в конфигурировании.
Единственный плагин из всех в этом проекте, который виден внешним компонентам, используется для подключения всех остальных плагинов.

## ReadmePlugin
Плагин для работы с readme файлами библиотек. На данный момент плагин отвечает за публикацию readme файла на confluence.

Конфигурация и значения по умолчанию: 
```groovy
readme {
    authentication = System.getenv("CONFLUENCE_AUTH")
    confluenceRestApiUrl = "https://wiki.yamoney.ru:443/rest/api/"
    confluenceSpace = "WebPortal"
    confluenceAncestorPageId = 128657081 //https://wiki.yamoney.ru/display/WebPortal/Libraries
    prefix = "lib."
    pathToDocument = project.getProjectDir().getPath() + "/README.md"
    pageTitle = project.getName()
}
```
Описание параметров конфигурации можно найти в javadoc'ах класса ReadmePluginExtension.

## CheckChangelogPlugin
Плагин проверяет наличие описания изменений в текущем релизе в файле CHANGELOG.md.
При отсутствии вышеназванного файла проверка пропускается.

Никаких дополнительных настроек данный плагин не добавляет.
