# 🔧 Установка JDK 21 для сборки проекта

## Проблема

У вас установлена **Java 25** которая слишком новая для Gradle 8.x/9.x.

**Требуется:** JDK 21 (LTS версия)

---

## 📥 Шаг 1: Скачать JDK 21

### Вариант A: Eclipse Temurin (рекомендуется)
1. Перейдите на: https://adoptium.net/
2. Выберите **JDK 21** (LTS)
3. Скачайте для Windows x64
4. Установите

### Вариант Б: Oracle JDK
1. Перейдите на: https://www.oracle.com/java/technologies/downloads/
2. Выберите **Java 21**
3. Скачайте для Windows x64
4. Установите

### Вариант В: Microsoft Build of OpenJDK
1. Перейдите на: https://learn.microsoft.com/java/openjdk/
2. Выберите **JDK 21**
3. Скачайте для Windows
4. Установите

---

## ⚙️ Шаг 2: Настроить JAVA_HOME

1. Откройте **Панель управления** → **Система**
2. Нажмите **Дополнительные параметры системы**
3. Нажмите **Переменные среды**
4. В **Системные переменные** найдите `JAVA_HOME`
5. Если нет - создайте:
   - Имя: `JAVA_HOME`
   - Значение: `C:\Program Files\Eclipse Adoptium\jdk-21.x.x` (путь установки)
6. Нажмите **OK**

---

## 🔄 Шаг 3: Обновить Path (если нужно)

1. В **Переменные среды** найдите переменную `Path`
2. Нажмите **Изменить**
3. Добавьте: `%JAVA_HOME%\bin`
4. Переместите наверх списка
5. Нажмите **OK**

---

## ✅ Шаг 4: Проверить установку

Откройте **новую** командную строку и выполните:

```bash
java -version
```

Должно показать:
```
openjdk version "21.x.x" ...
```

---

## 🚀 Шаг 5: Собрать проект

```bash
cd D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant
gradlew.bat clean assembleDebug
```

---

## 📝 Альтернатива: Использовать Android Studio JDK

Если у вас установлен **Android Studio**, он включает JDK:

1. Откройте **Android Studio**
2. **File** → **Settings** → **Build, Execution, Deployment** → **Gradle**
3. **Gradle JDK**: Выберите **jbr-21** (встроенный JDK)
4. Нажмите **OK**

Или создайте файл `gradle.properties` в проекте:
```properties
org.gradle.java.home=C:\\Program Files\\Android\\Android Studio\\jbr
```

---

## ❓ Вопросы

### Можно ли оставить Java 25?
Нет, Gradle 8.x не поддерживает Java 25. Нужна Java 17-21.

### Можно ли использовать Java 17?
Да, Java 17 тоже подходит для Gradle 8.x и Android Gradle Plugin.

### Что лучше Java 17 или 21?
**Java 21** - более новая LTS версия с улучшениями производительности.

---

**После установки JDK 21 сборка должна работать!** ✅
