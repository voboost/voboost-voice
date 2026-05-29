# QGBus Contacts Guide - Voboost Voice Assistant

## Overview

В системе BluetoothPhone контакты синхронизируются через **PBAP (Phone Book Access Profile)** и хранятся в локальной SQLite базе, которая экспортируется через **ContentProvider**.

---

## 📱 Как работает система контактов

### Архитектура:
```
Bluetooth Phone App
├── PBAP Sync (Bluetooth)
│   └──contactsinfo] + calllog]
│       └── ContactsProvider (content://com.qinggan.bluetoothphone/)
│           ├── content://com.qinggan.bluetoothphone/contactsinfo/[MAC]
│           └── content://com.qinggan.bluetoothphone/calllog/[MAC]
└── QGBus Events
    ├── WeChat contacts (через IWeChatService)
    └── Sync events (PbapEvents)
```

---

## 🗃️ ContentProvider API

### URI для контактов:

| Тип | URI | Описание |
|-----|-----|----------|
| **Множественные контакты** | `content://com.qinggan.bluetoothphone/contactsinfo/[MAC]` | Получить все контакты для конкретного MAC адреса |
| **Один контакт** | `content://com.qinggan.bluetoothphone/contactsinfo/#` | Не используется (требуется ID) |
| **Множественные звонки** | `content://com.qinggan.bluetoothphone/calllog/[MAC]` | Получить последние 200 звонков |
| **Один звонок** | `content://com.qinggan.bluetoothphone/calllog/#` | Не используется |

### Код для получения контактов (из PhoneCardView.java):

```java
// Загрузка контактов из базы BluetoothPhone
Cursor cursorQuery = mContext.getContentResolver().query(
    Uri.parse(People.CONTENT_URI.toString() + "/" + mac),
    new String[]{"name", "number"},
    null, null, null
);

if (cursorQuery != null && cursorQuery.getCount() != 0) {
    if (cursorQuery.moveToFirst()) {
        do {
            String name = cursorQuery.getString(cursorQuery.getColumnIndex("name"));
            String number = cursorQuery.getString(cursorQuery.getColumnIndex("number"));
            // Обработка контакта
        } while (cursorQuery.moveToNext());
    }
    cursorQuery.close();
}
```

### Структура таблицы contactsinfo:
```sql
CREATE TABLE contactsinfo (
    name TEXT NOT NULL,
    mac TEXT NOT NULL,
    number TEXT NOT NULL
);
```

---

## 📡 QGBus Events для контактов

### PBAP Sync Events (из PbapEvents.java):

| Событие | Назначение |
|---------|-----------|
| `PbapEvents.SyncStartEvent` | Начало синхронизации |
| `PbapEvents.SyncContactPhasedEvent` | Прогресс синхронизации (каждые 100 контактов) |
| `PbapEvents.SyncContactNotEmptyEvent` | Загружены первые контакты (>1 шт.) |
| `PbapEvents.SyncEndEvent` | Конец синхронизации |

### Использование:

```java
// Подписка на события синхронизации
EventBus.getDefault().register(this);

@Subscribe(threadMode = ThreadMode.MAIN)
public void onSyncStart(PbapEvents.SyncStartEvent event) {
    Log.d("Contacts", "Sync started");
}

@Subscribe(threadMode = ThreadMode.MAIN)
public void onSyncPhased(PbapEvents.SyncContactPhasedEvent event) {
    Log.d("Contacts", "Sync progress: " + cacheSize);
}

@Subscribe(threadMode = ThreadMode.MAIN)
public void onSyncEnd(PbapEvents.SyncEndEvent event) {
    Log.d("Contacts", "Sync completed");
}
```

---

## 🔄 Синхронизация контактов

### Поток синхронизации:

1. **Запуск**: `PbapProfileManager.syncContactAuthor()`
2. **Подключение к Bluetooth**: PBAP profile
3. **Событие onReady**: Готовность к синхронизации
4. **Вызов** `startContactsSync()`:
   - Сначала: `onCallLogItemCountUpdated` — количество звонков
   - Затем: `onContactItemCountUpdated` — количество контактов
5. **Загрузка**: `onContactItemFetched(BTContactItem)` — по одному контакту
6. **Сохранение в DB**: `CacheCallDataManager.updateContactList()`
7. **Отправка в ContentProvider**: `bulkInsert()` → уведомление через `notifyChange()`

### Ключевые классы:

| Класс | Роль |
|-------|------|
| `PbapProfileManager` | Управление PBAP синхронизацией |
| `CacheCallDataManager` | Кэширование и обработка контактов |
| `ContactsProvider` | ContentProvider для экспорта в систему |

---

## 💡 Как Launcher получает контакты (PhoneCardView.java):

```java
// Запуск синхронизации при открытии
private class updateDbDataTask extends AsyncTask {
    @Override
    protected Object doInBackground(Object[] objArr) {
        loadCallLogFromDb(mac);
        loadContactsFromDb(mac);  // ← Загрузка через ContentProvider
        return null;
    }
}

// Метод загрузки контактов
public void loadContactsFromDb(String mac) {
    Cursor cursorQuery = mContext.getContentResolver().query(
        Uri.parse(People.CONTENT_URI.toString() + "/" + mac),
        new String[]{"name", "number"},
        null, null, null
    );
    
    if (cursorQuery != null && cursorQuery.getCount() != 0) {
        if (cursorQuery.moveToFirst()) {
            do {
                LocalContactInfo info = new LocalContactInfo();
                info.name = cursorQuery.getString(cursorQuery.getColumnIndex("name"));
                info.number = cursorQuery.getString(cursorQuery.getColumnIndex("number"));
                mContactInfos.add(info);
            } while (cursorQuery.moveToNext());
        }
    }
}
```

---

## ⚠️ ПРОБЛЕМА С "ПЕРЕПУТАННЫМИ" КОНТАКТАМИ

### Возможные причины:

1. **Мультиюзерность**: Если используется `ISMUTI_USER = true`, контакты хранятся отдельно для каждого пользователя
2. **MAC адрес**: ContactsProvider фильтрует по `mac` — если MAC неизвестен, использует `"mac:is:null"`
3. **Кэш в Launcher**: PhoneCardView кэширует контакты в `mContactInfos`
4. **Асинхронная загрузка**: AsyncTask может завершиться после закрытия Activity

### Решение:

```java
// Проверка MAC адреса устройства
String mac = getBluetoothMac();  // Получить MAC текущего устройства

// Загрузка с проверкой
Cursor cursor = getContentResolver().query(
    Uri.parse("content://com.qinggan.bluetoothphone/contactsinfo/" + mac),
    new String[]{"name", "number"},
    null, null, null
);

if (cursor != null) {
    try {
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String number = cursor.getString(cursor.getColumnIndex("number"));
            // Обработка контакта
        }
    } finally {
        cursor.close();
    }
}
```

---

## 🔌 Альтернативные способы получения контактов

### 1. Through WeChat Service (IWeChatService):

```java
// Подключение к WeChat сервису
IWeChatService service = bindToWeChatService();

// Получить все контакты
List<Contact> contacts = service.getContacts();

// Для каждого контакта:
// - contact.getUserName() — уникальный ID (не номер)
// - contact.getNickName() — имя для отображения
// - contact.isGroup() — групповой ли это контакт
```

**Проблема**: WeChat контакты хранятся в памяти, не синхронизируются с BluetoothPhone.

### 2. Through QGBus (WeChat events):

```java
// Подписка на WeChat события
QGBus qGBus = new QGBus(context);
QGBusEventFilter filter = new QGBusEventFilter();
filter.addEventType("WeChat/REPLYMSG");
qGBus.subscribe(filter, handler);

// В handler:
public void onHandleEvent(QGBusEvent event) {
    if ("WeChat/REPLYMSG".equals(event.getEventType())) {
        Bundle data = event.getData();
        Contact contact = data.getParcelable("contact");
        // contact.getNickName(), contact.getUserName()
    }
}
```

**Проблема**: Только WeChat контакты, не телефонные.

---

## 📋 РЕКОМЕНДАЦИЯ ДЛЯ VOOBOOST

### Текущий способ (через AIDL):

```kotlin
// Voboost использует IBluetoothPhoneService
val service = bindToBluetoothPhone()
val contacts = service.getContacts()  // ← Этот метод
```

**Почему бывают проблемы:**
1. `getContacts()` может возвращать **кэшированные данные**
2. Синхронизация происходит **асинхронно** через PBAP
3. Есть задержка между синхронизацией и обновлением списка

### Лучший способ — прямой запрос к ContentProvider:

```kotlin
// Voboost должен использовать этот метод:
fun getBluetoothContacts(context: Context): List<ContactInfo> {
    val contacts = mutableListOf<ContactInfo>()
    
    try {
        // Получить MAC текущего Bluetooth устройства (если известен)
        val mac = getCurrentBluetoothMac()
        
        val uri = Uri.parse("content://com.qinggan.bluetoothphone/contactsinfo/$mac")
        val cursor = context.contentResolver.query(uri, arrayOf("name", "number"), null, null)
        
        cursor?.use {
            while (it.moveToNext()) {
                contacts.add(ContactInfo(
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    number = it.getString(it.getColumnIndexOrThrow("number"))
                ))
            }
        }
    } catch (e: Exception) {
        Log.e("Voboost", "Failed to load contacts", e)
    }
    
    return contacts
}
```

### Дополнительные улучшения:

1. **Добавить уведомление об изменении контактов**:
   ```kotlin
   context.contentResolver.registerContentObserver(
       Uri.parse("content://com.qinggan.bluetoothphone/contactsinfo"),
       true,
       observer
   )
   ```

2. **Проверять статус синхронизации** через `PbapProfileManager.getSyncSuccess()` (если можно получить доступ)

3. **Запрашивать контакты после события** `PbapEvents.SyncEndEvent` через EventBus

---

## 📂 КЛЮЧЕВЫЕ ФАЙЛЫ ДЛЯ ИССЛЕДОВАНИЯ

```
BluetoothPhone-release-signed/
├── app/src/main/java/com/qinggan/bluetoothphone/logic/component/ContactsProvider.java
│   └── ContentProvider для экспорта контактов в систему
├── app/src/main/java/com/qinggan/bluetoothphone/logic/manager/PbapProfileManager.java
│   └── Управление PBAP синхронизацией
├── app/src/main/java/com/qinggan/bluetoothphone/logic/manager/CacheCallDataManager.java
│   └── Кэширование и обработка контактов
└── app/src/main/java/com/qinggan/aar_phone/bean/People.java
    └── Константы URI для ContentProvider

Launcher-release-signed/
└── app/src/main/java/com/qinggan/aar_phone/PhoneCardView.java
    └── Как Launcher получает и отображает контакты
```

---

## 🎯 РЕЗЮМЕ

| Способ | Плюсы | Минусы |
|------|-------|--------|
| **AIDL IBluetoothPhoneService.getContacts()** | Простой API | Кэшированные данные, задержка |
| **ContentProvider query()** | Актуальные данные, фильтрация по MAC | Требует знания MAC |
| **WeChat Service** | WeChat контакты | Не телефонные номера |

### Рекомендация:
Использовать **ContentProvider с прямым запросом к базе**, добавив подписку на `notifyChange` для автоматического обновления.

---

**Последнее обновление:** 24 мая 2026  
**Статус:** ✅ Полный анализ завершён
