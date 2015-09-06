# Введение #

LocationManager (`su.geocaching.android.controller.LocationManager.java`) предназначен для получения информации о местоположении пользователя. Когда появляются новые координаты, LocationManager сообщает об этом.


# Как с этим работать #

Для того, чтобы пользоваться LocationManager ваш класс должен реализовывать интерфейс [ILocationAware](LocationManager#ILocationAware.md).
Создавать объект LocationManager не нужно. Для того, чтобы получить LocationManager следует вызвать метод getLocationManager(). Пример:

```
Controller.getInstance().getLocationManager();
```

Для того, чтобы сообщить LocationManager о том, что вы хотите (не) получать обновления положения есть 2 метода:

```
public void addSubscriber(ILocationAware subsriber)

public boolean removeSubsriber(ILocationAware subsriber)
```

В тот момент, когда вам необходимо начать получать обновления нужно вызвать метод addSubscriber, где параметром передаётся сущность, которая будет уведомляться об обновлениях положения.
Для того, чтобы больше не получать обновления следует вызвать метод removeSubscriber, где параметром передаётся тоже самое, что и выше.
### Важно! ###
Если Вы используете LocationManager в активити, то **необходимо** вызывать removeSubscriber не позже onPause в смысле [жизненного цикла activity](http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle). В противном случае, есть шанс, что GPS не будет выключен после выхода из приложения.

# ILocationAware #
```
public interface ILocationAware {
    public void updateLocation(Location location);

    public void onStatusChanged(String provider, int status, Bundle extras);
}
```
ILocationAware - интерфейс с которым работает LocationManager. В тот момент, когда случается то или иное событие вызывается соответствующий метод. Почитать более подробно об этих методах можно [здесь](http://developer.android.com/reference/android/location/LocationListener.html).

### updateLocation ###
Случается тогда, когда получено новое положение пользователя.

### onStatusChanged ###
Вызывается когда изменяется статус провайдера (параметр String provider). Параметр status принимает значения:
  * [LocationProvider.OUT\_OF\_SERVICE](http://developer.android.com/reference/android/location/LocationProvider.html#OUT_OF_SERVICE) если провайдер недоступен и в ближайшем будущем вряд ли будет работать
  * [LocationProvider.TEMPORARILY\_UNAVAILABLE](http://developer.android.com/reference/android/location/LocationProvider.html#TEMPORARILY_UNAVAILABLE) если провайдер временно недоступен и в скором будущем должен возобновить работу
  * [GpsStatus.GPS\_EVENT\_STARTED](http://developer.android.com/reference/android/location/GpsStatus.html#GPS_EVENT_STARTED) когда система gps запускается
  * [GpsStatus.GPS\_EVENT\_STOPPED](http://developer.android.com/reference/android/location/GpsStatus.html#GPS_EVENT_STOPPED) когда система gps останавливается
  * [GpsStatus.GPS\_EVENT\_FIRST\_FIX](http://developer.android.com/reference/android/location/GpsStatus.html#GPS_EVENT_FIRST_FIX) когда система gps получает первое положение после запуска
  * [GpsStatus.GPS\_EVENT\_SATELLITE\_STATUS](http://developer.android.com/reference/android/location/GpsStatus.html#GPS_EVENT_SATELLITE_STATUS) когда изменяется статус, связанный с количеством спутников
  * EVENT\_PROVIDER\_ENABLED когда данный провайдер был включен пользователем
  * EVENT\_PROVIDER\_DISABLED когда данный провайдер был выключен пользователем

Параметр extras может содержать специальную информацию о провайдере. Например, с ключом satellites сопоставлено количество спутников, использованных в определении текущего местоположения.

# Компас по GPS #
Возможна ситуация, когда на устройстве отсутствует акселерометр и получать азимут пользователя с помощью встроенных механизмов невозможно. Если аппаратный компас недоступен и сущность, которая получает обновления положения, также реализует [ICompassAware](CompassManager#ICompassAware.md), то в этом случае при вызове [updateLocation](LocationManager#updateLocation.md) будет вызван метод updateBearing из интерфейса [ICompassAware](CompassManager#ICompassAware.md). Этот азимут определяется по направлению движения пользователя и стоит иметь в виду, что если положение определяется по GSM/WiFi сетям этот азимут скорее всего будет неопределён(и равен нулю).

# Актуальность положения #
В данный момент пользователи LocationManager могут узнать актуально ли положение, предоставляемое методом `getLastKnownLocation`. Для этого есть метод `hasPreciseLocation`, который возвращает `true`, если положение:
  1. есть (`!=null`)
  1. его точность меньше 40 метров
  1. оно было получено менее, чем 30 секунд назад
Для того, чтобы подписчикам было проще отслеживать устаревание положения, есть таймер, который через минуту посылает сообщение в систему о данном событии посредством CallbackManager.

# Одометр #
В LocationManager доступна функциональность одометра (считает путь, пройденный пользователем). Одометр представляет собой статичный внутренний класс, лежащий в LocationManager, поэтому доступ к функциональности одометра можно получить, например, так:
```
UserLocationManager.Odometer.getDistance();
```

# Как это работает #
На протяжении работы приложения LocationManager создаётся лишь один раз - в Controller'е при первом вызове Controller.getLocationManager(). Контекст, используемый для его создания, - глобальный контекст приложения. Класс LocationManager хранит в себе список объектов, реализующих [ILocationAware](LocationManager#ILocationAware.md), которые получают обновления положения пользователя (назовём их подписчики), и оперирует с ним при вызове методов LocationManager.addSubscriber(ILocationAware subsriber) и LocationManager.removeSubsriber(ILocationAware subsriber).

Как только появляется хотя бы один подписчик, делается запрос к системе для получения обновлений положения и статуса. В тот момент, когда обновилось положение или провайдер стал доступен/недоступен все подписчики уведомляются об этом событии, посредством вызова соответствующего метода интерфейса [ILocationAware](LocationManager#ILocationAware.md).

В тот момент, когда количество подписчиков становится нулевым запускается таймер на 30 секунд, по истечении которых посылается запрос системе на удаление обновлений положения. Такое решение обусловлено тем, что activity, использующие LocationManager, должны удаляться из подписчиков не позже onResume, а, следовательно, при переходе из одной активити, использующей LocationManager, в другую без таймера сначала происходило бы удаление обновлений положения, а затем добавление.