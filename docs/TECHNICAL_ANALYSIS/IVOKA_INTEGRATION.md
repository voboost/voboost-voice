# Интеграция с IVoka через KeyManager

**Дата:** 2026-05-15  
**Статус:** ✅ Реализовано  
**Источник:** Анализ системных APK и Frida перехваты

---

## 🎯 Проблема

IVoka (стандартный ассистент) конфликтует с Voboost Voice Assistant:
- Оба сервиса слушают кнопку на руле (KEYCODE=130)
- Оба обрабатывают команды через QGSpeechService
- Возникает дублирование и конфликты

---

## 🔧 Решение: Перехват через KeyManager + Frida

### Архитектура:

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  KEY BUTTON │────►│ KeyManager       │────►│ IVoka Service    │
│ (KEYCODE=130)│     │ System Service   │     │ (отключен)       │
└─────────────┘     └──────────────────┘     └──────────────────┘
                         ▲
                         │
                    Frida hook
                    (frida-voice-button.js)
                         │
                         ▼
                 ┌──────────────────┐
                 │ Voboost Service  │
                 │ (активен)        │
                 └──────────────────┘
```

---

## 📋 Шаги интеграции

### Шаг 1: Отключить IVoka и QGSpeechService

```bash
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
adb shell pm disable com.qinggan.sttservice
```

**Проверка:**
```bash
adb shell pm list packages -d | grep -E "ivoka|sttservice"
```

---

### Шаг 2: Создать VoiceActivationService

**Файл:** `app/src/main/java/ru/voboost/voiceassistant/VoiceActivationService.kt`

```kotlin
class VoiceActivationService : Service() {
    companion object {
        const val KEYCODE_IVOKA = 130
    }
    
    private var keyManager: KeyManager? = null
    
    private val keyEventListener = object : KeyManager.OnKeyEventListener {
        override fun onKeyShortClick(keyCode: Int, keyEvent: KeyEvent?): Boolean {
            if (keyCode == KEYCODE_IVOKA) {
                Log.i(TAG, "IVA button pressed - launching Voboost!")
                activateVoiceAssistant()
                return true  // Обрабатываем сами, не передаем дальше
            }
            return false
        }
        
        override fun onKeyDown(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStart(keyCode: Int, keyEvent: KeyEvent?) = false
        override fun onKeyLongPressStop(keyCode: Int, keyEvent: KeyEvent?) = false
    }
    
    private fun activateVoiceAssistant() {
        val intent = Intent(this, VoboostVoiceService::class.java)
        intent.action = "ru.voboost.voiceassistant.ACTIVATE"
        startService(intent)
    }
}
```

---

### Шаг 3: Создать Frida скрипт для KeyManager

**Файл:** `frida-voice-button.js`

```javascript
// Перехват KeyManager.inputKeyEvent()
Interceptor.attach(
    Module.getExportByName(null, 'KeyManager_inputKeyEvent'),
    {
        onEnter: function (args) {
            var keyCode = parseInt(args[2].toInt32());
            console.log("📢 KeyManager.inputKeyEvent(): keyCode = " + keyCode);
            
            if (keyCode == 130) {  // KEYCODE_IVOKA
                console.log("🎯 IVA button pressed (KEYCODE_IVOKA=130)!");
                console.log("🟢 Start Voboost recognition");
                
                // Запустить Voboost через startService()
                var context = Java.use('android.app.ActivityThread').currentApplication().getApplicationContext();
                var intent = Java.use('android.content.Intent');
                var serviceIntent = intent.$new(
                    context,
                    "ru.voboost.voiceassistant/.VoboostVoiceService"
                );
                
                // Отключить Ivoka (если не отключен через pm disable)
                try {
                    var packageManager = context.getPackageManager();
                    var componentName = Java.use('android.content.ComponentName').$new(
                        "com.qinggan.ivoka",
                        "com.qinggan.ivoka.service.WindowService"
                    );
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        2,  // COMPONENT_ENABLED_STATE_DISABLED
                        1   // DONT_KILL_APP
                    );
                } catch (e) {
                    console.log("⚠️ Failed to disable Ivoka: " + e.message);
                }
                
                // Запустить Voboost
                context.startService(serviceIntent);
            }
        }
    }
);
```

---

### Шаг 4: Запустить Frida скрипт

**Вариант A: Системный перехват (требует root)**
```bash
# На устройстве
adb shell
su
/data/local/tmp/frida-server &

# На компьютере
frida -U -f system_server -l frida-voice-button.js --no-pause
```

**Вариант B: Перехват через Voboost (без root)**
```bash
# Если Voboost уже запущен
frida -U ru.voboost.voiceassistant -l frida-voice-button.js
```

---

## 🎯 Как это работает

### Путь кнопки:

```
1. Водитель нажимает кнопку на руле
   ↓
2. KeyManager.inputKeyEvent(130) вызывается в system_server
   ↓
3. Frida перехватывает вызов
   ↓
4. Проверка: keyCode == 130 (KEYCODE_IVOKA)
   ↓
5. Отключить IVoka (если не отключен)
   ↓
6. Создать Intent для VoboostVoiceService
   ↓
7. Вызвать startService() для Voboost
   ↓
8. VoboostVoiceService.onCommandReceived(ACTIVATE)
   ↓
9. activateVoiceAssistant()
   ↓
10. Запуск распознавания речи
```

---

## 📊 Сравнение подходов

| Подход | Root | Надежность | Сложность |
|--------|------|------------|----------|
| **KeyManager + Frida** | ✅ Требует | Высокая | Средняя |
| **pm disable + Broadcast** | ❌ Не требуется | Средняя | Простая |
| **Модификация QGSpeechService** | ✅ Требует | Высокая | Высокая |

---

## 🐛 Решение проблем

### Проблема 1: Frida не перехватывает

**Решение:**
```bash
# Проверить что frida-server запущен
adb shell ps -A | grep frida

# Если нет - запустить
adb shell su -c "/data/local/tmp/frida-server &"
```

### Проблема 2: Кнопка не работает после перехвата

**Решение:**
```bash
# Проверить KEYCODE
adb logcat | grep -i "keycode\|KeyEvent"

# Возможно в вашей системе другой keycode (например, 131 или 286)
```

### Проблема 3: IVoka все еще активен

**Решение:**
```bash
# Убедиться что отключен через pm disable
adb shell pm disable com.qinggan.ivoka
adb shell pm disable com.qinggan.ivoka1
```

---

## 📝 Итоговая конфигурация

### После интеграции:

1. ✅ IVoka отключен (`pm disable`)
2. ✅ QGSpeechService отключен (`pm disable`)  
3. ✅ Frida скрипт запущен (перехват KeyManager)
4. ✅ VoiceActivationService активен (Accessibility Service)
5. ✅ VoboostVoiceService запускается по кнопке

---

## 📚 См. также

- [Полное решение](../TECHNICAL_ANALYSIS/COMPLETE_SOLUTION.md) - Звуковые эффекты и анимация
- [Установка](../SETUP/INSTALLATION.md) - Инструкция по установке
- [Решение проблем](../SETUP/TROUBLESHOOTING.md) - Устранение неполадок