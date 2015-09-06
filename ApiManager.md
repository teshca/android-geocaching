## Введение ##
ApiManager (su.geocaching.android.controller.apimanager.ApiManager) - класс, отвечающий за получение тайников с сайта (geocaching.su).
Класс реализует интерфейс IApiManager

## Устройство ##

ApiManager получает набор тайников с сайта и, если получены тайники из не запрашиваемой области - фильтрует их. ApiManager хранит в себе полученные ранее тайники. Для получения тайников с сайта генерируется (при создании ApiManager'a) уникальный id сессии, по которому сайт определяет какие тайники уже были отправлены, а какие нет. Данные о тайниках представляют собой xml вида
```
<c> 
    <id>8901</id> 
    <cn>10</cn>
    <a>47</a> 
    <n>Geocache name</n>
    <la>59.6952333333</la> 
    <ln>29.3968666667</ln> 
    <ct>3</ct> 
    <st>1</st>
</c>
```
и парсятся GeoCacheSaxHandler'ом. Запросы к сайту имеют вид
```
http://www.geocaching.su/pages/1031.ajax.php?lngmax=29,983873&lngmin=29,819079&latmax=59,915986&latmin=59,824648&id=737297&geocaching=5767e405a17c4b0e1cbaecffdb93475d
```

## Методы ##
ApiManager содержит единственный метод
```
List<GeoCache> getGeoCacheList(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude)
```
возвращающий набор тайников для заданной местности.


## Как с этим работать ##
Класс ApiManager работает синхронно. Асинхронное получение тайников инкапсулировано в методе Controller'a
```
public void updateSelectedGeoCaches(SelectGeoCacheMap map, GeoPoint upperLeftCorner, GeoPoint lowerRightCorner) {
		GeoPoint[] d = { upperLeftCorner, lowerRightCorner };
		new DownloadGeoCacheTask(apiManager, map).execute(d);
	}
```
В классе DownloadGeoCacheTask (extends AsyncTask<GeoPoint, Integer, List<GeoCache>> ) происходит получение тайников и вызовется метод добавления тайников на карту