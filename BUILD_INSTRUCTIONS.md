# 🚀 ИНСТРУКЦИЯ ПО СБОРКЕ

## ✅ Исправленная ошибка Gradle

**Проблема:** Неправильный синтаксис зависимости Vosk  
**Решение:** Убрано `@aar` из `build.gradle`

**Было:**
```gradle
implementation 'com.alphacephei:vosk-android:0.3.45@aar'  ❌
```

**Стало:**
```gradle
implementation 'com.alphacephei:vosk-android:0.3.45'  ✅
```

---

## 📋 ПОШАГОВАЯ ИНСТРУКЦИЯ

### Шаг 1: Открыть проект в Android Studio

```
File → Open → D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
```

### Шаг 2: Синхронизировать Gradle

```
File → Sync Project with Gradle Files
```

**Или нажмите:** 🔄 (Sync Now вверху)

### Шаг 3: Дождаться завершения синхронизации

**Ожидаемые сообщения:**
```
✓ Gradle sync finished
✓ Dependencies downloaded
✓ Project configured
```

### Шаг 4: Собрать APK

**Вариант A: Через Android Studio**
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

**Вариант B: Через командную строку**
```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew assembleDebug
```

### Шаг 5: Проверить успешность сборки

**Сообщение:**
```
✓ APK generated successfully
Location: app/build/outputs/apk/debug/app-debug.apk
```

### Шаг 6: Установить на устройство

**Вариант A: Через Android Studio**
```
Run → Run 'app' (Shift+F10)
```

**Вариант B: Через ADB**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚠️ ВОЗМОЖНЫЕ ОШИБКИ И РЕШЕНИЯ

### 1. **"Gradle sync failed"**

**Причина:** Проблемы с интернетом или кэшем  
**Решение:**
```bash
# Очистить кэш Gradle
gradlew clean

# Пересинхронизировать
File → Invalidate Caches / Restart → Invalidate and Restart
```

### 2. **"SDK not found"**

**Решение:**
1. File → Project Structure → SDK Location
2. Указать путь к Android SDK
3. Sync Gradle

### 3. **"Build failed - Dependency error"**

**Решение:**
- Проверить интернет-соединение
- Очистить кэш: `gradlew clean`
- Пересинхронизировать Gradle

### 4. **"Vosk model not found"**

**Решение:**
- Проверить, что модель в `app/src/main/assets/vosk/vosk-model-small-ru-0.22/`
- Пересобрать проект
- Очистить данные приложения на устройстве

---

## ✅ ПРОВЕРКА УСПЕШНОЙ СБОРКИ

### Файлы должны существовать:

```
✅ app/build/outputs/apk/debug/app-debug.apk
```

### Размер APK:

```
📦 app-debug.apk (~55-60 MB с моделью Vosk)
```

---

## 📱 ПЕРВЫЙ ЗАПУСК

### 1. Установить приложение
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Дать разрешения

**На устройстве:**
- Настройки → Приложения → Voboost Voice
- ✅ Микрофон
- ✅ Поверх других окон
- ✅ Спец. возможности → Voboost Voice Button → ВКЛ

### 3. Запустить сервис

Сервис запускается автоматически при установке.

### 4. Протестировать

```
Сказать: "Привет, Вобуст"
Сказать: "Открой лючок зарядки"
```

### 5. Проверить логи

```bash
adb logcat | grep -i "voboost\|vosk\|voice"
```

**Ожидаемые логи:**
```
I/SpeechRecognition: Vosk initialized successfully!
I/SpeechRecognition: Keyword detected!
I/NLUEngine: Command parsed: charge_port_open
I/CommandExecutor: Intent sent successfully
```

---

## 🎯 ЧЕК-ЛИСТ ГОТОВНОСТИ

- ✅ Модель Vosk скачана и распакована
- ✅ build.gradle исправлен (убрано @aar)
- ✅ Все импорты на месте
- ✅ Проект открывается в Android Studio
- ✅ Gradle синхронизируется без ошибок
- ✅ APK собирается успешно
- ✅ Устройство подключено по USB
- ✅ Отладка по USB включена

---

## 📊 СТАТИСТИКА ПРОЕКТА

| Параметр | Значение |
|----------|----------|
| **Файлов Kotlin** | 14 |
| **Файлов XML** | 9 |
| **Команд** | 13 |
| **Размер APK** | ~55-60 MB |
| **Мин. Android** | API 26 (Android 8.0) |
| **Целевой Android** | API 30 (Android 11) |

---

## 🎉 ГОТОВО!

**Проект полностью готов к тестированию!**

Следующий шаг: Найти keycode кнопки на руле (вы знаете об этом)

---

**Удачи! 🚀**
