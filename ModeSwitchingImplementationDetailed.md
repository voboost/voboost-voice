# Реализация команд управления режимами в системах голосового управления (ECO/COMFORT/SPORT)

## Общее описание

В системах голосового управления (например, в проекте Ivoka) реализована возможность переключения режимов вождения с помощью голосовых команд. В данной документации описаны ключевые компоненты реализации.

## Структура команды

### Стандартные параметры команды:
- **intentName**: `CAR_DRIVER_MODE` 
- **target**: (не используется)
- **action**: `SET` или `OPEN` 
- **mode**: `ECO`, `COMFORT`, `SPORT`
- **modeType**: (не используется)
- **value**: (не используется)
- **delta**: (не используется)
- **position**: (не используется)

## Логика обработки

### 1. Обработка в VehicleParseHelpMgr.java
При получении `SYSTEM_DRIVE_MODE`:
- Если `mode` = `"ECO"` → `subClassify = 5`
- Если `mode` = `"SPORT"` → `subClassify = 6`
- Если `mode` = `"COMFORTABLE"` → `subClassify = 7

### 2. Структура отправляемого объекта
Когда команда обрабатывается:
- `classify = 26`
- `command = 0` (для установки режима)
- `param = X` (где X — значение режима)
  - ECO → 5
  - SPORT → 6
  - COMFORT → 7

### 3. Константы в DFVehicleState.java
- `dirve_mode_eco = 1` (индекс режима)
- `dirve_mode_sport = 3` (индекс режима)  
- `dirve_mode_comfort = 2` (индекс режима)

### 4. Поддерживаемые команды

#### Режим ECO:
- "Включи режим экономии" 
- "Включи экономичный режим"
- "Включи ECO режим"

#### Режим COMFORT:
- "Включи комфортный режим"
- "Включи комфорт"
- "Включи COMFORT режим"

#### Режим SPORT:
- "Включи спортивный режим"
- "Включи спортивный режим"
- "Включи SPORT режим"

### 5. Пример реализации в коде

```java
// В VehicleParseHelpMgr.java
if (SYSTEM_DRIVE_MODE.equals(intentName2)) {
    if ("ECO".equals(mode)) {
        carOrderBean.setSubClassify(5);
    } else if ("SPORT".equals(mode)) {
        carOrderBean.setSubClassify(6);
    } else if ("COMFORTABLE".equals(mode)) {
        carOrderBean.setSubClassify(7);
    }
    carOrderBean.setClassify(26);
    carOrderBean.setCommand(0);
}
```

### 6. Уникальные особенности

- Режимы обрабатываются как часть системы управления автомобилем (`classify = 26`)
- Для всех режимов используется `command = 0` (установка)
- Каждый режим имеет уникальное `subClassify` значение:
  - ECO → 5
  - SPORT → 6
  - COMFORT → 7

## Результат

После обработки система отправляет команду:
- `classify = 26`
- `command = 0`
- `param = [5|6|7]` в зависимости от режима

## Заключение

Реализация похожа на другие команды в системе - она использует:
1. Стандартную структуру `intentName = SYSTEM_DRIVE_MODE`
2. Идентификацию режимов по `mode` параметру
3. Установку `classify` как 26 для режимов
4. Установку `command = 0` для установки
5. Использование `subClassify` для конкретного режима

## Связанные файлы

1. `VehicleParseHelpMgr.java` - обработка команд
2. `DFVehicleState.java` - константы режимов
3. `VehicleState.java` - общие состояния