# Команды переключения режимов вождения через CAN-шину

## Описание

Для реализации команд переключения режимов вождения через CAN-шину в проекте VoboostVoiceAssistant используются Intent с экшеном `pateo.dls.canbus.SEND_CANBUS_COMMAND`.

## Константы режимов вождения
Константы для команд переключения режима вождения:
- DRIVING_MODE_SET_ECO = 2 (режим экономичный)
- DRIVING_MODE_SET_NORMAL = 3 (обычный режим)
- DRIVING_MODE_SET_SPORT = 1 (спортивный режим)

## Пример реализации команды "Переключить в режим отдыха"

```kotlin
// Создание Intent для отправки команды через CAN-шину
val intent = Intent("pateo.dls.canbus.SEND_CANBUS_COMMAND")
intent.putExtra("command_id", 545) // ID команды переключения режима вождения
intent.putExtra("value", 2)        // Значение для режима "отдых" (eco)
context.sendBroadcast(intent)
```

## Пример реализации команды "Переключить в спортивный режим"

```kotlin
// Создание Intent для отправки команды через CAN-шину
val intent = Intent("pateo.dls.canbus.SEND_CANBUS_COMMAND")
intent.putExtra("command_id", 545) // ID команды переключения режима вождения
intent.putExtra("value", 1)        // Значение для спортивного режима
context.sendBroadcast(intent)
```

## Пример реализации команды "Переключить в обычный режим"

```kotlin
// Создание Intent для отправки команды через CAN-шину
val intent = Intent("pateo.dls.canbus.SEND_CANBUS_COMMAND")
intent.putExtra("command_id", 545) // ID команды переключения режима вождения
intent.putExtra("value", 3)        // Значение для обычного режима
context.sendBroadcast(intent)
```

## Примечание

Все команды направляются через широковещательный Intent сервису CanBusService, который обрабатывает их и отправляет в CAN-шину. Необходимо убедиться, что VoboostVoiceAssistant имеет нужные разрешения для отправки широковещательных сообщений.
