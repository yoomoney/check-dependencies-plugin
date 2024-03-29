### NEXT_VERSION_TYPE=MAJOR|MINOR|PATCH
### NEXT_VERSION_DESCRIPTION_BEGIN
### NEXT_VERSION_DESCRIPTION_END
## [9.0.1](https://github.com/yoomoney/check-dependencies-plugin/pull/13) (07-04-2022)

* Перевыпуск релиза из-за проблем с синхронизацией maven-central

## [9.0.0](https://github.com/yoomoney/check-dependencies-plugin/pull/12) (06-04-2022)

* ***breaking changes*** Обновлена версия gradle `6.4.1` -> `7.4.1`

## [8.1.1](https://github.com/yoomoney/check-dependencies-plugin/pull/11) (28-03-2022)

* Фильтруем пустые версии в выдаче зависимостей с конфликтом

## [8.1.0](https://github.com/yoomoney/check-dependencies-plugin/pull/10) (16-03-2022)

* Добавлен вывод в лог в случае, если в build.gradle определена версия зафиксированной зависимости

## [8.0.0](https://github.com/yoomoney/check-dependencies-plugin/pull/9) (02-03-2022)

* Удалена проверка исключения конфликта в файле из настройки exclusionsRulesSources. Теперь не происходит падения при конфликте фиксированных версий.
* Удалена функциональность подбора подходящих версий библиотек.
* **breaking changes** Удалена настройка exclusionsRulesSources
* **breaking changes** Удалена настройка versionSelectors

## [7.4.0](https://github.com/yoomoney/check-dependencies-plugin/pull/8) (27-01-2022)

* В задачи вывода новых и текущих зависимостей добавлены зависимости buildscript конфигураций.

## [7.3.0](https://github.com/yoomoney/check-dependencies-plugin/pull/7) (26-08-2021)

* Переезд организации yoomoney-gradle-plugins -> yoomoney

## [7.2.1](https://github.com/yoomoney/check-dependencies-plugin/pull/6) (19-05-2021)

* Добавлена информация о сборке, покрытии, лицензии в README.md.

## [7.2.0](https://github.com/yoomoney/check-dependencies-plugin/pull/5) (19-03-2021)

* Сборка проекта переведена на gradle-project-plugin.

## [7.1.2](https://github.com/yoomoney/check-dependencies-plugin/pull/4) (11-03-2021)

* Исправлен баг в ArtifactVersionResolver - теперь при поиске в списке репозиториев пропускаем репозитории, получение версий из
которого завершилось ошибкой.

## [7.1.1](https://github.com/yoomoney/check-dependencies-plugin/pull/3) (16-02-2021)

* Добавлена фильтрация репозиториев MavenArtifactRepository при составлении списка возможных репозиториев для поиска зависимостей.
Это исправляет проблему при подключении репозиториев типа "flatDir"

## [7.1.0](https://github.com/yoomoney/check-dependencies-plugin/pull/2) (04-02-2021)

* Удален ключ git_key.enc в связи с изменением логики работы с git_key.
Подробности см. https://github.com/yoomoney/travis-shared-configuration/pull/8
* Поднята версия artifact-release-plugin

## [7.0.0](https://api.github.com/repos/yoomoney/check-dependencies-plugin/pulls/1) (29-01-2021)

* Внесены изменения в связи с переходом в GitHub:
* Переименованы пакеты
* Плагин собирается без использования project-plugin, сборка полностью описывается в build.gradle
* Подключен artifact-release-plugin для автоматического выпуска релиза.
* Сборка переведена на travis (ранее использовался jenkins)
* ***breaking_changes*** Переименованы таски:
* printNewInnerDependenciesVersions -> printNewDependenciesByGroup
* printNewOuterDependenciesVersions -> printNewDependencies
* printActualInnerDependenciesVersions -> printActualDependenciesByGroup
* printActualOuterDependenciesVersions -> printActualDependencies
* Таски printNewDependencies и printActualDependencies теперь выводят информацию по всем зависимостям проекта
(раньше только для внешних)
* Регулирование префиксов пакетов, по которым нужно выводить информацию в тасках printNewInnerDependenciesVersions и
printActualInnerDependenciesVersions вынесено в настройку includeGroupIdForPrintDependencies.
* Удалена настройка pushMetrics и отправка метрик.

## [6.2.1]() (30-11-2020)

* Обновлена версия kotlin 1.3.71 -> 1.3.50

## [6.2.0]() (25-11-2020)

* Добавлена проверка Snapshot зависимостей и репозитория в секции buildScript

## [6.1.0]() (03-07-2020)

* Поднята версия gradle: 6.0.1 -> 6.4.1.

## [6.0.2]() (20-05-2020)

* Замена символов в отправляемой метрике

## [6.0.1]() (19-05-2020)

* Убрано определение названия репозитория по appName

## [6.0.0]() (19-05-2020)

* Проверка конфликтов мажорных версий перенесена обратно на этап конфигурации. Таска majorVersionCheckerTask удалена.

## [5.2.0]() (12-04-2020)

* Проверка конфликтов мажорных версий вынесена в отдельную таску "majorVersionCheckerTask" (раннее осуществлялась на этапе конфигурации).
* Добавлена настройка includedConfigurations. Теперь проверка конфликтов осуществляется только для конфигураций, объявленных в
данной настройке.
Изменения внесены для ускорения прохождения тасок.

## [5.1.0]() (05-02-2020)

* Сборка на java 11

## [5.0.1]() (30-01-2020)

* Удален snapshots репозиторий.

## [5.0.0]() (29-01-2020)

* Обновлена версия gradle `4.10.2` -> `6.0.1`
* Обновлены версии зависимостей
* Исправлены warnings и checkstyle проблемы

## [4.5.3]() (26-11-2019)

* Задачи ```printNewInnerDependenciesVersions``` и ```printNewOuterDependenciesVersions``` больше не падают если произошли проблемы
с определением новых версий

## [4.5.2]() (18-09-2019)

* Поправила баг в VersionChecker, теперь возможно подключить разные мажорные версии в разные конфигурации.

## [4.5.1]() (05-06-2019)

* Поправила баг с объявлением range в checkForbiddenDependencies

## [4.5.0]() (04-06-2019)

* Добавлена таска checkForbiddenDependencies. Принимает в настройках список запрещенных для использования
зависимостей, проверяет их наличие в текущих зависимостях.

## [4.4.9]() (22-05-2019)

* Сборка переведена на yamoney-gradle-project-plugin=5.+

## [4.4.8]() (22-05-2019)

* Поправлена бага в `majorVersionChecker` из-за которой, если в `excludeDependencies` было несколько записей, то эти исключения не работали
* Так-же это проявлялось если в проекте есть модули, и в одном из них эти исключения переопределены

## [4.4.7]() (16-05-2019)

* Исправленл определение локального файла и maven-артефакта с исключениями (libraries-versions-exclusions.properties) в windows

## [4.4.6]() (14-05-2019)

* Добавлен репозиторий с Gradle плагинами

## [4.4.5]() (05-05-2019)

* Восстановил работу расширения `versionSelectors` для подбора подходящих версий библиотек,
которые нужно использовать для устранения конфликтов зависимостей.

## [4.4.4]() (20-03-2019)

* Исправлен `IndexOutOfBoundException` при синхронизации проекта IDEA.
В `CheckVersionAction` добавлена проверка, что список задач для запуска сборки был непуст.

## [4.4.3]() (11-03-2019)

* Исправление java.lang.IllegalStateException: Duplicate key 3.0.1 при выводе списка новых зависимостей

## [4.4.2]() (07-03-2019)

* Изменила имя таски с "checkSnapshotDependencies" на "checkSnapshotsDependencies"

## [4.4.1]() (06-03-2019)

* Правка формата CHANGELOG.md


## [4.4.0]() (01-03-2019)

* Добавлен функционал проверки snapshot-зависимостей

## [4.3.0]() (01-03-2019)

* Переход на platformGradleProjectVersion 4 версии

## [4.2.0]() (25-02-2019)
* Переход на platformGradleProjectVersion 4 версии

## [4.1.0]() (20-02-2019)
* Вывод актуальных зависимости в тасках printActualInnerDependenciesVersions, printActualOuterDependenciesVersions в JSON формате

## [4.0.4]() (15-02-2019)
* Исправлены даты в CHANGELOG.md

## [4.0.3]() (17-01-2019)
* Исправлен NPE при детекте внешних зависимостей

## [4.0.2]() (16-01-2019)
* Исправлен NPE при детекте внутренних зависимостей

## [4.0.1]() (15-01-2019)
* Исправлена сортировка зависимостей по semver

## [4.0.0]() (11-01-2019)

* Обновился gradle до версии 4.10.2
* Исправлены проблемы совместимости с aether от eclipse

## [3.0.0]() (09-12-2018)

Добавлено 
    * Проверка конфликтов мажорных версий подключаемых библиотек
    * Вывод новых доступных версий для внешних и внутренних библиотек

## [2.3.2]() (19-11-2018)

Добавил guava в зависимости

## [2.3.1]() (14-11-2018)

Сборка при помощи gradle-project-plugin

## [2.3.0]() (19-04-2017)

Реализован эвристический метод по поиску возможного решения конфликта версий

## [2.2.3]() (27-07-2017)

Добавлен перехват ошибки получения конфигураций для поддержки gradle 3.3+

## [2.2.2]() (19-04-2017)

Реализована жадная загрузка managed-зависимостей до первого резолва зависимостей проекта

## [2.2.1]() (10-04-2017)

Добавлена возможность указания в файле исключений имени библиотеки в формате <group>:<name>

## [2.2.0]() (17-03-2017)

Добавлен вывод полного пути до зависимости, для которой обнаружен конфликт

## [2.1.0]() (17-03-2017)

Добавлена проверка локального файла исключений на наличие неиспользуемых исключений

## [2.0.1]() (03-03-2017)

Обновлены тесты для проверки работы с плагином io.spring.dependency-management версии 1.0.1.RELEASE

## [2.0.0]() (03-03-2017)

Переход на io.spring.dependency-management 1.0.1.RELEASE

## [1.0.0]() (03-03-2017)

Вынес плагин в отдельный репозиторий

## [0.4.3]() (21-02-2017)

Изменено умолчательное имя файла с правилами изменений версий для слуая, когда правла находятся в зип артефакте

## [0.4.2]() (14-02-2017)

Изменено умолчательное имя файла с правилами изменений версий

## [0.4.1]() (14-02-2017)

Исправлена ошибка проверки CheckChangelog, в результате которой неправильно определелялась версия проекта

## [0.4.0]() (10-02-2017)

Улучшена поддержка указания правил исключений проверки изменений версий библиотек в плагине CheckDependenciesPlugin. 
  * Добавлена поддержка чтения файла с правилами из Maven артефакта.
  * Добавлена настройка "excludedConfigurations" позволяющая исключить из проверки указанные конфигурации
  * Теперь указывается список источников, содержащих файлы с правилами. В списке можно указывать: либо путь к файлу локальной 
    системы, либо название мавен артефакта, содержащего файл "libraries_versions_exclusions.properties". При этом допускается 
    указание нескольких файлов с правилами и артефактами. В этом случае правила будут объединяться.

## [0.3.1]() (04-02-2017)

Добавлена поддержка правил исключений проверки изменений версий библиотек в плагине CheckDependenciesPlugin. Теперь опционально 
можно указать для каких библиотек разрешается изменение версий. При этом жестко прописывается: с какой версии на какую разрешено 
изменение.

## [0.3.0]() (01-02-2017)

Добавлена функциональность проверки согласованности версий используемых библиотек с версиями библиотек, зафиксированными в 
стороннем плагине "IO Spring Dependency Management Plugin"

## [0.2.0]() (18-01-2017)

Добавлена функциональность проверки CHANGELOG.md файла на наличие записи о текущей версии библиотеки.

## [0.1.0]() (22-12-2016)

Создана черновая версия общего плагина для библиотек.
Добавлена первая функциональность: публикация readme файла.