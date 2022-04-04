[![Build Status](https://travis-ci.com/yoomoney/check-dependencies-plugin.svg?branch=master)](https://travis-ci.com/yoomoney/check-dependencies-plugin)
[![codecov](https://codecov.io/gh/yoomoney/check-dependencies-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/yoomoney/check-dependencies-plugin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# check-dependencies-plugin


## Подключение
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'io.spring.gradle:dependency-management-plugin:0.6.1.RELEASE'
        classpath 'ru.yoomoney.gradle.plugins:check-dependencies-plugin:4.+'
    }
}
apply plugin: 'ru.yoomoney.gradle.plugins.check-dependencies-plugin'

```

## Функциональность

Список тасок, предоставляемых плагином:  
* ```checkLibraryDependencies```: Проверка легитимность изменения версий библиотек и конфликтов мажорных версий подключаемых библиотек  
* ```checkSnapshotsDependencies```: Проверка наличия snapshot-версий подключаемых библиотек  
* ```checkForbiddenDependencies```: Проверка наличия запрещенных артефактов в подключаемых библиотеках  
  

* ```printNewDependenciesByGroup```: Вывод новых доступных версий для библиотек для groupId из списка  
* ```printNewDependencies```: Вывод новых доступных версий для библиотек  
* ```printActualDependenciesByGroup```: Вывод актуальных версий для библиотек для groupId из списка  
* ```printActualDependencies```: Вывод актуальных версий для библиотек  

Подробности по работе тасок можно найти в одноименных разделах.

### Проверка легитимности изменения версий используемых библиотек в проекте.

Проверяются как прямые, так и транзитивные зависимости.

Зачастую проект может содержать большое количество повторно используемых библоиотек разных версий, найденных по транзитивным
зависимостям. Однако, при запуске приложения может быть использована только одна версия одной и той же библиотеки.
Чтобы гарантировать согласованность этой библиотеки с другими, Gradle имеет встроенный механизм решения конфликтов версий.
По умолчанию Gradle из всех версий одной и той же библиотеки выбирает самую последнюю. При таком подходе нет гарантии, что самая
новая версия библиотеки будет обратно совместима с предыдущей версией. А значит нельзя гарантировать, что такое повышение
не сломает проект.

Для того, чтобы избежать не контролируемое изменение версий, используется подход с фиксацией набор версий бибилиотек, на которых
гарантируется работа приложения.

Для фиксации используется сторонний плагин <b>IO Spring Dependency Management plugin</b>. Список фиксируемых библиотек с
версиями хранится в maven xml.pom файле. Плагин предоставляет программный доступ к этому списку.

### Проверка конфликтов мажорных версий подключаемых библиотек

Прямые и транзитивные зависимости библиотек проверяются на наличие конфликтов мажорных версий.
При наличии конфликтов сборка неуспешна, кроме случаев, если запускалась таска ":dependencies" - в этом случае выводится запись
о наличии конфликта в лог. 
Для определения, какие зависимости нужно проверять, существует настройка includeGroupIdPrefixes.
Например, в ней можно указать, что проверять нужно только внутренние библиотеки компании, указав префикс "ru.yoomoney".

### Вывод новых доступных версий для библиотек  

Печатает доступные новые версии зависимостей.  
Есть два режима:
1) Вывод новых версий для всех имеющихся в проекте зависимостей.  
   Для запуска этого режима необходимо вызвать вручную таску printNewDependencies.  
2) Вывод новых версий для groupId из списка. Список определяется с помощью настройки:
```
checkDependencies {
    includeGroupIdForPrintDependencies = ['ru.yoomoney']
}
```

   В настройку передаются префиксы groupId артефактов.  
   Функицональность может быть полезна для вывода новых зависимостей, относящихся к внутренним для компании, 
   тогда в настройку нужно передать префикс компании, как в примере выше.  
   Для запуска этого режима необходимо вызвать вручную таску printNewDependenciesByGroup.

### Вывод актуальных версий для библиотек

Печатает актуальные версии зависимостей.  
Для этой функциональности также есть два режима:
1) Вывод версий для всех имеющихся в проекте зависимостей.  
   Для запуска этого режима необходимо вызвать вручную таску printActualDependencies.  
   
2) Вывод версий для groupId из списка. Список определяется с помощью настройки:  
```
checkDependencies {
    includeGroupIdForPrintDependencies = ['org.apache']
}
``` 
   В настройку передаются префиксы groupId артефактов.  
   
   Для запуска этого режима необходимо вызвать вручную таску printNewDependenciesByGroup.   

Пример вывода:

```
   [
       {
           "scope": "implementation",
           "name": "json-utils",
           "version": "1.0.0",
           "group": "ru.yoomoney.common"
       },
       {
           "scope": "implementation",
           "name": "xml-utils",
           "version": "1.0.0",
           "group": "ru.yoomoney.common"
       }
   ]
```

Результат сохраняется в build/report/dependencies/ в actual_dependencies_by_group.json & actual_all_dependencies.json

### Проверка наличия snapshot-версий подключаемых библиотек

   Проверяет наличие snapshot-версий подключаемых зависимостей. Вызывается только при ручном запуске таски 
checkSnapshotsDependencies. Выбрасывает исключение при наличии зависимостей с версией, содержащей "-snapshot".
    Для того, чтобы разрешить наличие snapshot-зависимостей необходимо указать в build.gradle такое свойство:
```
    ext.allowSnapshot = "true"
```

   В этом случае проверка snapshot-зависимостей производиться не будет.

### Проверка наличия запрещенных артефактов в подключаемых библиотеках
   
   В качестве настройки принимается список запрещенных к использованию артефактов. Затем просматривает текущие
зависимости, в случае нахождения среди них запрещенных артефактов запрещает сборку.
   Также предлагает заменить найденные запрещенные версии на новейшие, доступные для данной зависимости, если она 
не совпадает с запрещенной.

#### Настройки проверки наличия запрещенных артефактов в подключаемых библиотеках

   Список запрещенных артефактов может задаваться такими способами:
```groovy
     forbiddenDependenciesChecker {
            after {             //запрещены все версии joda-time:joda-time выше 4.0.0 (включая все более поздние мажорные)
                 forbidden 'joda-time:joda-time:4.0.0'
                 recommended '4.0.7'
                 comment 'bla bla'
            }
            before {           //запрещены все версии org.apache.tomcat.embed:tomcat-embed-core ниже 4.0.0
                 forbidden 'org.apache.tomcat.embed:tomcat-embed-core:4.2.0'
                 recommended '4.2.7'
                 comment 'bla bla'
            }
            eq {               //запрещена org.apache.commons:commons-lang3 версии 2.1.4
                 forbidden 'org.apache.commons:commons-lang3:2.1.4'
                 recommended '2.1.7'
                 comment 'bla bla'
            }
            range {            //запрещены версии com.google.guava:guava от 4.0.0 до 4.0.2 включительно
                 forbidden 'com.google.guava:guava'
                 startVersion '4.0.0'
                 endVersion '4.0.2'
                 recommended '4.0.7'
                 comment 'bla bla'
            }
     }
```
   Можно указать несколько блоков для одной и той же библиотеки.
   По умолчанию список пуст.

#### Включение конфигураций в проверку

Для того, чтобы конфигурации начали проверяться на наличие конфликтов, необходимо внести их в настройку 
<b>includedConfigurations</b>:

```groovy
checkDependencies {
    includedConfigurations = ["testImplementation", "testRuntime"]
}
```

По умолчанию проверка осуществляется в конфигурациях componentTestCompileClasspath и slowTestCompileClasspath, 
которые включают в себя зависимости из всех нужных для проверок конфигураций - compile, implementation, testCompile, 
testImplementation, runtime.

#### Настройки проверки конфликтов мажорных версий 

Проверку конфликтов можно отключить, выставив данную настройку в false:
```groovy
majorVersionChecker {
   enabled = true // true является значением по умолчанию
}
```

Библиотеки, для которых будут проверяться конфликты мажорных версий можно описать с помощью префиксов, с которых начинаются 
названия групп. 
Например, для того, чтобы проверять конфликты только для библиотек, название группы которых начинается с
"com.google" или "org.apache", нужно задать настройку includeMajorVersionCheckPrefixLibraries следующим образом:

```groovy
majorVersionChecker {
   includeGroupIdPrefixes = ['com.google', 'org.apache']    // По умолчанию список пуст
}
```

Также можно исключить из проверки конкретные артефакты:
```groovy
majorVersionChecker {
   excludeDependencies = ["ru.yoomoney.common:xml-utils", 
                                        "ru.yoomoney.common:json-utils"]  // По умолчанию список пуст
}
```

Есть возможность установить, нужно ли фейлить билд при нахождении конфликта:
```
majorVersionChecker {
   failBuild = true  // По умолчанию билд фейлится
}
```