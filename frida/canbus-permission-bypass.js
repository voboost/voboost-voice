// CanBusService Permission Bypass
// Хукает метод checkPermission в CanBusService и пропускает вызовы от com.voboost.voiceassistant

(function() {
    'use strict';

    const TAG = 'canbus-permission-bypass';
    const ALLOWED_PACKAGE = 'com.voboost.voiceassistant';

    function getTimestamp() {
        const now = new Date();
        return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}.${String(now.getMilliseconds()).padStart(3, '0')}`;
    }

    function log(level, message) {
        console.log(`${getTimestamp()} [${level}] ${TAG}: ${message}`);
    }

    function info(message) {
        log('INFO', message);
    }

    function error(message) {
        log('ERROR', message);
    }

    Java.perform(function() {
        try {
            // Хукаем метод checkPermission в CanBusService
            const CanBusService = Java.use('com.qinggan.canbus.service.CanBusService');
            
            // Хукаем checkPermission(String permission)
            CanBusService.checkPermission.overload('java.lang.String').implementation = function(permission) {
                try {
                    // Получаем имя пакета вызывающего
                    const callingUid = Java.use('android.os.Binder').getCallingUid();
                    const packageManager = this.getPackageManager();
                    const packages = packageManager.getPackagesForUid(callingUid);
                    
                    let callerPackage = null;
                    if (packages && packages.length > 0) {
                        callerPackage = packages[0];
                    }
                    
                    info(`checkPermission called: permission=${permission}, callingUid=${callingUid}, package=${callerPackage}`);
                    
                    // Если вызов от нашего приложения - не проверяем разрешение
                    if (callerPackage === ALLOWED_PACKAGE) {
                        info(`Bypassing permission check for ${ALLOWED_PACKAGE}`);
                        return; // Возвращаем без исключения
                    }
                    
                    // Для остальных - вызываем оригинальный метод
                    this.checkPermission(permission);
                } catch (e) {
                    error(`Error in checkPermission hook: ${e.message}`);
                    error(e.stack);
                    // В случае ошибки - вызываем оригинальный метод
                    this.checkPermission(permission);
                }
            };
            
            info('CanBusService.checkPermission hook installed successfully');
            info(`Allowing ${ALLOWED_PACKAGE} to bypass CAN bus permission checks`);
            
        } catch (e) {
            error(`Failed to install hook: ${e.message}`);
            error(e.stack);
        }
    });
})();
