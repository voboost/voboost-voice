#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Добавление информации про Hidden API Policy в документацию
"""

from pathlib import Path

PROJECT_ROOT = Path(r"D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant")

HIDDEN_API_SECTION = """
### Hidden API Policy (для работы с AIDL) ⚠️

**Критично важно!** Для доступа к скрытым AIDL интерфейсам (AudioPolicyService, CanBusService) необходимо установить Hidden API Policy:

```bash
# Основной флаг (универсальный для всех Android)
adb shell "settings put global hidden_api_policy 1"

# Дополнительные флаги для совместимости (Android 9-11)
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"

# Проверка
adb shell settings get global hidden_api_policy
# Должно вернуть: 1
```

**Почему это нужно:**
- Android блокирует доступ к скрытым API (помеченным как `@hide`)
- AIDL интерфейсы системных сервисов (`IAudioPolicyService`, `ICanBusService`) используют скрытые методы
- Без этой настройки сервис не сможет подключиться к CAN bus или AudioPolicyManager
- **Симптомы проблемы:** сервис запускается, но не работает эхоподавление или не приходят события CAN bus

> **Примечание:** Скрипты установки `VoboostVoiceAssistant-install.bat` и `migrate-from-voiceassistant.bat` автоматически устанавливают Hidden API Policy.

"""

def add_to_installation():
    """Добавить секцию в INSTALLATION.md"""
    file_path = PROJECT_ROOT / "docs" / "SETUP" / "INSTALLATION.md"
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Найти место после "Дать разрешение на автозапуск"
    target = "# Дать разрешение на автозапуск\nadb shell appops set ru.voboost.voice RECEIVE_BOOT_COMPLETED allow\n```\n"
    insert_pos = content.find(target)
    
    if insert_pos == -1:
        print("❌ Не найдено место для вставки в INSTALLATION.md")
        return False
    
    insert_pos += len(target)
    
    new_content = content[:insert_pos] + HIDDEN_API_SECTION + content[insert_pos:]
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print("✅ INSTALLATION.md обновлён")
    return True

def add_to_troubleshooting():
    """Добавить проблему в TROUBLESHOOTING.md"""
    file_path = PROJECT_ROOT / "docs" / "SETUP" / "TROUBLESHOOTING.md"
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_problem = """
### Проблема 9: AIDL не работает (CAN bus / AudioPolicy) ⚠️

**Симптомы:**
- Сервис запущен, но **не работает эхоподавление** при Bluetooth звонках
- Не приходят события от CAN bus
- В логах ошибки доступа к скрытым API или `SecurityException`

**Причина:**
Android блокирует доступ к скрытым API (помеченным как `@hide`). AIDL интерфейсы системных сервисов (`IAudioPolicyService`, `ICanBusService`) используют эти методы.

**Решение:**
```bash
# 1. Установить Hidden API Policy
adb shell "settings put global hidden_api_policy 1"
adb shell "settings put global hidden_api_policy_pre_p 1"
adb shell "settings put global hidden_api_policy_pre_q 1"
adb shell "settings put global hidden_api_policy_pre_r 1"

# 2. Проверить настройку
adb shell settings get global hidden_api_policy
# Должно вернуть: 1

# 3. Перезапустить сервис
adb shell am force-stop ru.voboost.voice
adb shell am start-foreground-service -n ru.voboost.voice/.VoboostVoiceService

# 4. Проверить логи
adb logcat | grep -i "AudioPolicy\\|CanBus\\|AIDL"
```

**Проверка работы:**
```bash
# Логи AudioPolicyServiceManager (эхоподавление)
adb logcat | grep "AudioPolicyServiceManager\\|PhoneCallPoller"

# Ожидаемый вывод при звонке:
# I/PhoneCallPoller: Call state: ACTIVE (muting recognizer)
# I/PhoneCallPoller: Call state: IDLE (restoring recognizer)
```

> **Примечание:** Скрипты установки `VoboostVoiceAssistant-install.bat` и `migrate-from-voiceassistant.bat` автоматически устанавливают Hidden API Policy. Если вы устанавливаете APK вручную через `adb install`, нужно выполнить команды выше.

"""
    
    # Найти место перед "## 🔍 Diagnostic commands"
    target = "## 🔍 Diagnostic commands"
    insert_pos = content.find(target)
    
    if insert_pos == -1:
        print("❌ Не найдено место для вставки в TROUBLESHOOTING.md")
        return False
    
    new_content = content[:insert_pos] + new_problem + content[insert_pos:]
    
    # Также добавить чек-лист
    checklist_target = "- [ ] Сервис запущен (`adb shell ps | grep voboost`)"
    checklist_pos = new_content.find(checklist_target)
    
    if checklist_pos != -1:
        checklist_text = "- [ ] **Hidden API Policy установлен** (`adb shell settings get global hidden_api_policy = 1`)"
        insert_check_pos = new_content.find("\n", checklist_pos) + 1
        new_content = new_content[:insert_check_pos] + checklist_text + "\n" + new_content[insert_check_pos:]
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print("✅ TROUBLESHOOTING.md обновлён")
    return True

def main():
    print("Добавление информации про Hidden API Policy...")
    add_to_installation()
    add_to_troubleshooting()
    print("Готово!")

if __name__ == "__main__":
    main()
