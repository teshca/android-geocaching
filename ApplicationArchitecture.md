# Архитектура приложения #
![http://android-geocaching.googlecode.com/svn/trunk/files/wiki/images/Android-GeoCachingAppModel.png](http://android-geocaching.googlecode.com/svn/trunk/files/wiki/images/Android-GeoCachingAppModel.png)

Структурно можно разделить весь код приложения на три компонента:
  * _Model_ - представляет данные и реагирует на запросы от компонента _Controller_
  * _Controller_ - интерпретирует полученные данные и сообщает другим компонентам о различных событиях
  * _View_ - отвечает за графическое представление данных пользователю

## Model ##
Описывает ключевой объект приложения - GeoCache, а также сюда можно отнести базу данных, которая хранит отмеченные пользователем тайники, сайт, содержащий все тайники данного сервиса, а также ресурсы и настройки приложения.


## Controller ##
Сюда относятся 2 сущности - класс **Controller** и набор различных **менеджеров**. Класс Controller предоставляет доступ компонента _View_ к менеджерам, а также выполняет некоторые функции интепретации полученных данных. **Менеджеры** отвечают за обработку различных данных, полученных извне. На данном этапе разработки имеются 10 различных менеджеров:
  * [Database manager](DbManager.md)
  * [Location manager](LocationManager.md)
  * [Compass manager](CompassManager.md)
  * [API manager](ApiManager.md)
  * [Connection & Ping manager](ConnectionManager.md)
  * [Analytics manager](ApplicationArchitecture#Analytics_manager.md)
  * [Log manager](ApplicationArchitecture#Log_manager.md)
  * [Resource manager](ApplicationArchitecture#Resource_manager.md)
  * [Preferences manager](ApplicationArchitecture#Preferences_manager.md)

### [Database manager](DbManager.md) ###
Отвечает за соединение с базой данных, запись и чтение тайников.

### [Location manager](LocationManager.md) ###
Хранит последнее известное местоположение пользователя, полученное с помощью GPS, GSM/Wi-Fi и других провайдеров. Сообщает об изменении положения, а также определяет направление пользователя по изменению его положения.

### [Compass manager](CompassManager.md) ###
Определяет азимут пользователя и сообщает об его изменении.

### [API manager](ApiManager.md) ###
Отвечает за взаимодействие приложения с сервисом geocaching.su

### [Connection & Ping manager](ConnectionManager.md) ###
Следит за состоянием соединения с internet, а именно с geocaching.su, и оповещает, когда связь исчезает или появляется.

### Analytics manager ###
Отвечает за работу приложения с сервисом Google Analytics.

### Log manager ###
Организует работу системы логирования приложения.

### Resource manager ###
Предоставляет доступ к ресурсам приложения таким как, рисунки, строки, константы.

### Preferences manager ###
Работает с настройками приложения, пишет и читает их.

## View ##
Содержит в себе логику пользовательского интерфейса. Состоит из набора Activity, представляющих тот или иной функционал приложения. На данном этапе есть 6 типов Activity:
  * **Select activity** - отвечает за выбор тайника
  * **Info activity** - предоставляет информацию о тайнике для пользователя
  * **Favorites activity** - представляет список избранных тайников
  * **Search activity** - выполняют функцию поиска тайника
  * **Settings activity** - позволяют настраивать приложение
  * **About activity** - информация о программе, а также обратная связь
  * **Dashboard activity** - главное меню приложения