# Введение #
ConnectionManager (su.geocaching.android.ui.geocachemap.ConnectionManager.java) предназначен для получения информации о состоянии подключения к сети. Если появляется или пропадает соединение с интернетом - все объекты, использующие ConnectionManager, уведомляются.

# Как с этим работать #
Для того, чтобы пользоваться ConnectionManager ваш класс должен реализовывать интерфейс [IInternetAware](ConnectionManager#IInternetAware.md). Создавать объект ConnectionManager не нужно. Для того, чтобы получить ConnectionManager следует вызвать метод Controller.getConnectionManager(). Пример:
```
Controller.getInstance().getConnectionManager();
```

Для того, чтобы сообщить ConnectionManager о том, что вы хотите (не) получать обновления о состоянии соединения с интернетом есть 2 метода:
```
public void addSubscriber(IInternetAware subsriber)

public boolean removeSubsriber(IInternetAware subsriber)
```

В тот момент, когда вам необходимо начать получать обновления, нужно вызвать метод addSubscriber, где параметром передаётся сущность, которая будет уведомляться об обновлениях состояния соединения. Для того, чтобы больше не получать обновления следует вызвать метод removeSubscriber, где параметром передаётся тоже самое, что и выше.

# IInternetAware #
```
public interface IInternetAware {

    public void onInternetLost();

    public void onInternetFound();
}
```

IInternetAware - интерфейс с которым работает ConnectionManager. В тот момент, когда случается то или иное событие вызывается соответствующий метод.

## onInternetLost ##
Вызывается, когда пропадает активное соединение с сетью интернет.

## onInternetFound ##
Вызывается, когда соединение с сетью интернет установлено.

# Как это работает #
Имеется ещё один класс, отвечающий за работу ConnectionManager - ConnectionStateReciever (su.geocaching.android.ui.geocachemap.ConnectionStateReciever.java). Этот класс является слушателем BroadcastReciever. Он получается уведомления о изменении состояния соединения. Когда появляется или исчезает интернет, то этот класс вызывает соответствующий метод ConnectionManager в контроллере, который в свою очередь рассылает это сообщение своим слушателям.