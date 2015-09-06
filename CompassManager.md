# Введение #
CompassManager (su.geocaching.android.controller.CompassManager.java) предназначен для получения информации об азимуте пользователя. Когда меняются значения сенсоров устройства, азимут пересчитывается и все объекты, использующие CompassManager уведомляются.

# Как с этим работать #
Для того, чтобы пользоваться CompassManager ваш класс должен реализовывать интерфейс [ICompassAware](CompassManager#ICompassAware.md). Создавать объект CompassManager не нужно. Для того, чтобы получить CompassManager следует вызвать метод Controller.getCompassManager(). Пример:
```
Controller.getInstance().getCompassManager();
```

Для того, чтобы сообщить CompassManager о том, что вы хотите (не) получать обновления положения есть 2 метода:
```
public void addSubscriber(ICompassAware subsriber)

public boolean removeSubsriber(ICompassAware subsriber)
```

В тот момент, когда вам необходимо начать получать обновления нужно вызвать метод addSubscriber, где параметром передаётся сущность, которая будет уведомляться об обновлениях положения. Для того, чтобы больше не получать обновления следует вызвать метод removeSubscriber, где параметром передаётся тоже самое, что и выше.

# ICompassAware #
```
public interface ICompassAware {

    public void updateBearing(int bearing);

}
```

ICompassAware - интерфейс с которым работает CompassManager. В тот момент, когда случается то или иное событие вызывается соответствующий метод.

## updateBearing ##
Вызывается, когда обновляются данные от сенсоров. Параметром передаётся новый азимут в градусах.