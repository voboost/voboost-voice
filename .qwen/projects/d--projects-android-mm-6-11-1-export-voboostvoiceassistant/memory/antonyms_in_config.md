---
name: antonyms_in_config
description: Антонимы перенесены из NLUOrtEngine.kt в config.json для гибкости конфигурации
type: project
---

Антонимы теперь хранятся в config.json вместо кода:
```json
{
  "nlu": {
    "engine": "onnx",
    "antonyms": {
      "открой": "закрой",
      "подними": "опусти",
      "включи": "выключи"
    }
  }
}
```

Изменённые файлы:
- NLUOrtEngine.kt — loadAntonymsFromConfig() загружает из config.nlu.antonyms
- AppConfig.kt — добавлено поле antonyms: Map<String, String>? в NLUConfig
- config.json — добавлен блок "antonyms"

**Плюсы:**
- Гибкость без перекомпиляции
- Мультиязычность (разные языки = разные антонимы)
- Легко управлять через конфиг

**How to apply:** При добавлении новых антонимов или изменении логики штрафа — править config.json, не код.
