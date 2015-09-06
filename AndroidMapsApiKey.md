# Google maps api key #
  1. Каждый разработчик использует свой собственный api key - полученный им самостоятельно
  1. Для того чтобы код проекта выложенного в svn у вас работал, вам необходимо создать файл dev.xml в папке res/values со следующим содержимым
```
<?xml version="1.0" encoding="UTF-8"?>
<resources>
   <string name="google_map_api_key">{Ваш персональный ключ}</string>
</resources>
```