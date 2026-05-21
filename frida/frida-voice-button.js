// frida-voice-button.js
// Перехват кнопки на руле для VoboostVoiceAssistant
// ПЕРЕХВАТ ЧЕРЕЗ KeyManagerService (com.qinggan.keymanager.service)
// Запуск: frida -U -f com.qinggan.keymanager.service -l frida-voice-button.js --no-pause

Java.perform(function() {
    console.log("🚀 VoboostVoiceAssistant - KeyManagerService Intercept Loaded");
    console.log("   Target: com.qinggan.keymanager.service.KeyManagerService");
    console.log("   Method: inputKeyEvent(int, int, String)");
    console.log("   QGSpeechService: DISABLED");
    console.log("   Ivoka: DISABLED");
    console.log("");
    
    var Context = Java.use("android.content.Context");
    var Intent = Java.use("android.content.Intent");
    
    // Флаг: активно ли распознавание
    var isListening = false;
    
    // ============================================================
    // ПЕРЕХВАТ: KeyManagerService.inputKeyEvent()
    // ============================================================
    
    console.log("📌 Hooking KeyManagerService.inputKeyEvent()...");
    
    try {
        // Находим класс KeyManagerService
        var KeyManagerService = Java.use("com.qinggan.keymanager.service.KeyManagerService");
        
        // Перехватываем inputKeyEvent(int keyCode, int flags, String callingPackageName)
        KeyManagerService.mBinder.implementation.inputKeyEvent.overload('int', 'int', 'java.lang.String').implementation = function(keyCode, flags, callingPackageName) {
            console.log("📢 KeyManagerService.inputKeyEvent():");
            console.log("   keyCode = " + keyCode + " (0x" + keyCode.toString(16) + ")");
            console.log("   flags = " + flags);
            console.log("   callingPackageName = " + callingPackageName);
            
            if (keyCode === 130) {  // KEYCODE_IVOKA
                console.log("🎯 IVA button pressed (KEYCODE_IVOKA=130)!");
                console.log("   isListening = " + isListening);
                
                if (isListening) {
                    // Кнопка нажата во время распознавания → ОТМЕНА
                    console.log("🔴 CANCEL: Button pressed during listening");
                    isListening = false;
                    
                    // Отправляем broadcast для отмены
                    sendBroadcastToVoboost("ru.voboost.voice.CANCEL");
                    
                } else {
                    // Кнопка нажата → ЗАПУСК Voboost
                    console.log("🟢 ACTIVATE: Start Voboost recognition");
                    isListening = true;
                    
                    // Отправляем broadcast для запуска
                    sendBroadcastToVoboost("ru.voboost.voice.ACTIVATE");
                }
                
                // Возвращаем true (обработали, но передаем дальше для совместимости)
                // return true;
            }
            
            // Вызываем оригинальный метод
            return this.inputKeyEvent(keyCode, flags, callingPackageName);
        };
        
        console.log("✅ Hook installed: KeyManagerService.inputKeyEvent()");
        
    } catch (e) {
        console.error("❌ Failed to hook KeyManagerService.inputKeyEvent(): " + e);
        console.error("   Stack: " + e.stack);
        
        // Попытка альтернативного перехвата
        console.log("🔄 Trying alternative hook method...");
        hookKeyManagerEngine();
    }
    
    // ============================================================
    // АЛЬТЕРНАТИВНЫЙ ПЕРЕХВАТ: KeyManagerEngine
    // ============================================================
    
    function hookKeyManagerEngine() {
        try {
            var KeyManagerEngine = Java.use("com.qinggan.keymanager.service.engine.KeyManagerEngine");
            
            console.log("📌 Hooking KeyManagerEngine.pushMsg()...");
            
            KeyManagerEngine.pushMsg.implementation = function(msg) {
                try {
                    // Проверяем тип сообщения
                    if (msg && msg.mWhat !== undefined) {
                        // mWhat = 6 → inputKeyEvent
                        if (msg.mWhat === 6 && msg.mKeyCode === 130) {
                            console.log("🎯 KeyManagerEngine: IVA button (keyCode=130, what=6)");
                            console.log("   isListening = " + isListening);
                            
                            if (isListening) {
                                console.log("🔴 CANCEL");
                                isListening = false;
                                sendBroadcastToVoboost("com.voboost.voiceassistant.CANCEL");
                            } else {
                                console.log("🟢 ACTIVATE");
                                isListening = true;
                                sendBroadcastToVoboost("ru.voboost.voice.ACTIVATE");
                            }
                        }
                    }
                } catch (e) {
                    // Игнорируем ошибки при обработке сообщения
                }
                
                return this.pushMsg(msg);
            };
            
            console.log("✅ Hook installed: KeyManagerEngine.pushMsg()");
            
        } catch (e) {
            console.error("❌ Failed to hook KeyManagerEngine: " + e);
        }
    }
    
    // ============================================================
    // Функция отправки Broadcast в Voboost
    // ============================================================
    
    function sendBroadcastToVoboost(action) {
        try {
            console.log("📤 Sending broadcast: " + action);
            
            // Находим контекст KeyManagerService
            var serviceInstance = Java.use("com.qinggan.keymanager.service.KeyManagerService");
            
            // Получаем экземпляр сервиса через статическое поле
            var service = serviceInstance.service.value;
            
            if (service) {
                console.log("📍 Found KeyManagerService instance");
                
                var intent = Intent.$new(action);
                intent.setPackage("ru.voboost.voice");
                
                service.sendBroadcast(intent);
                console.log("✅ Broadcast sent: " + action);
            } else {
                console.warn("⚠️  Service instance not found, trying alternative...");
                
                // Альтернативный метод: найти любой контекст
                var contexts = Java.choose("android.content.Context", {
                    onMatch: function(instance) {
                        try {
                            var intent = Intent.$new(action);
                            intent.setPackage("ru.voboost.voice");
                            instance.sendBroadcast(intent);
                            console.log("✅ Broadcast sent via alternative method");
                        } catch (e) {
                            // Игнорируем
                        }
                    },
                    onComplete: function() {}
                });
            }
            
        } catch (e) {
            console.error("❌ Failed to send broadcast: " + e);
        }
    }
    
    // ============================================================
    // МОНИТОРИНГ: Логирование всех кнопок
    // ============================================================
    
    console.log("📌 Monitoring all key events...");
    
    try {
        var KeyManagerService = Java.use("com.qinggan.keymanager.service.KeyManagerService");
        
        KeyManagerService.mBinder.implementation.inputKeyEvent.overload('int', 'int', 'java.lang.String').implementation = function(keyCode, flags, callingPackageName) {
            // Логируем все кнопки
            if (keyCode >= 0 && keyCode <= 256) {
                console.log("🔑 Key event: keyCode=" + keyCode + " (0x" + keyCode.toString(16) + ")");
            }
            
            return this.inputKeyEvent(keyCode, flags, callingPackageName);
        };
        
    } catch (e) {
        // Уже перехвачено выше
    }
    
    // ============================================================
    // ГОТОВО
    // ============================================================
    
    console.log("");
    console.log("✅ ✅ ✅ Frida hook loaded successfully ✅ ✅ ✅");
    console.log("");
    console.log("   Process: com.qinggan.keymanager.service");
    console.log("   Waiting for button press...");
    console.log("   KEYCODE_IVOKA = 130");
    console.log("");
    console.log("   Behavior:");
    console.log("   - Press once   → ACTIVATE (start recognition)");
    console.log("   - Press twice  → CANCEL (stop recognition)");
    console.log("");
    console.log("   Logs:");
    console.log("   - 📢 KeyManagerService.inputKeyEvent()");
    console.log("   - 📤 sendBroadcast()");
    console.log("   - 🔑 Key events (monitoring)");
    console.log("");
});

