# 📡 CANBUSLISTENER — ПОЛНАЯ ДОКУМЕНТАЦИЯ

**Класс:** `com.qinggan.canbus.CanBusListener`  
**Наследование:** `ICanBusServiceCallback.Stub` (AIDL callback)  
**Назначение:** Слушатель событий CAN-шины автомобиля

---

## 🏗️ АРХИТЕКТУРА

```
CanBusService (AIDL)
       ↓
       ↓ callback (ICanBusServiceCallback)
       ↓
CanBusListener ← Ваше приложение
```

---

## 📋 МЕТОДЫ (76 callback'ов)

### 1. ДВЕРИ И ЗАМКИ

#### `onDoorStatusChanged(DoorStatus doorStatus)`
**Описание:** Изменение состояния дверей и замков

**Параметры `DoorStatus`:**
| Поле | Тип | Значения |
|------|-----|----------|
| `bonnetDoor` | int | Капот: `0`=закрыт, `1`=открыт |
| `fLDoor` | int | Передняя левая: `0`=закрыта, `1`=открыта |
| `fRDoor` | int | Передняя правая: `0`=закрыта, `1`=открыта |
| `rLDoor` | int | Задняя левая: `0`=закрыта, `1`=открыта |
| `rRDoor` | int | Задняя правая: `0`=закрыта, `1`=открыта |
| `loadSpace` | int | Багажник: `0`=закрыт, `1`=открыт |
| `fLDoorLockStatus` | int | Замок FL: `0`=заблокирован, `1`=разблокирован |
| `fRDoorLockStatus` | int | Замок FR: `0`=заблокирован, `1`=разблокирован |
| `rLDoorLockStatus` | int | Замок RL: `0`=заблокирован, `1`=разблокирован |
| `rRDoorLockStatus` | int | Замок RR: `0`=заблокирован, `1`=разблокирован |

**Пример:**
```java
@Override
public void onDoorStatusChanged(DoorStatus doorStatus) {
    if (doorStatus.getFLDoor() == DoorStatus.OPEN) {
        Log.d("CAN", "Передняя левая дверь открыта!");
    }
    if (doorStatus.getfLDoorLockStatus() == DoorStatus.DOOR_STATUS_UNLOCK) {
        Log.d("CAN", "Дверь водителя разблокирована");
    }
}
```

---

#### `onWindowsStatusChanged(WindowStatus state)`
**Описание:** Изменение состояния окон и люка

**Параметры `WindowStatus`:**
| Поле | Тип | Значения |
|------|-----|----------|
| `fLWindow` | int | Окно FL: `0`=закрыто, `1`=открыто |
| `fRWindow` | int | Окно FR: `0`=закрыто, `1`=открыто |
| `rLWindow` | int | Окно RL: `0`=закрыто, `1`=открыто |
| `rRWindow` | int | Окно RR: `0`=закрыто, `1`=открыто |
| `sunroof` | int | Люк: `0`=TILT_UP, `1`=VENT_AREA, `3`=FULLY_CLOSE, `4`=HALF_OPEN, `5`=PARTIALLT_SLIDE, `6`=FULLY_OPEN |
| `fLWindowPosition` | int | Позиция окна FL (0-100%) |
| `fRWindowPosition` | int | Позиция окна FR (0-100%) |
| `rLWindowPosition` | int | Позиция окна RL (0-100%) |
| `rRWindowPosition` | int | Позиция окна RR (0-100%) |
| `sunroofPosition` | int | Позиция люка |
| `sunroofTilt` | int | Наклон люка |

**Пример:**
```java
@Override
public void onWindowsStatusChanged(WindowStatus state) {
    if (state.getFLWindow() == WindowStatus.OPEN) {
        Log.d("CAN", "Окно водителя открыто");
    }
    Log.d("CAN", "Позиция люка: " + state.sunroofPosition);
}
```

---

### 2. КЛИМАТ-КОНТРОЛЬ

#### `onAirConditionChanged(AirCondition airConditionData)`
**Описание:** Изменение параметров климат-контроля

**Параметры `AirCondition` (основные):**
| Поле | Тип | Значения |
|------|-----|----------|
| `airSWStatus` | int | Питание AC: `0`=выкл, `1`=вкл |
| `airACStatus` | int | Компрессор: `0`=выкл, `1`=вкл |
| `airWindSpeed` | int | Скорость вентилятора (0-7) |
| `airLeftTemperature` | float | Температура левая (°C) |
| `airRightTemperature` | float | Температура правая (°C) |
| `airCirculationMode` | int | Режим циркуляции: `0`=внешняя, `1`=внутренняя, `2`=авто |
| `airMode` | int | Режим обдува: `0`=AUTO, `1`=MANUAL |
| `airSupplyStatus` | int | Направление: `0`=FACE, `1`=FACE_LEG, `2`=LEG, `3`=LEG_DEF, `4`=DEF |
| `airDUALStatus` | int | Двухзонный: `0`=MONO, `1`=DUAL |
| `airFrontWindowDefogger` | int | Обдув лобового: `0`=выкл, `1`=вкл |
| `airRearWindowHeatingStatus` | int | Подогрев заднего: `0`=выкл, `1`=вкл |
| `airLeftSeatHeatingLevel` | int | Подогрев сиденья L (0-3) |
| `airRightSeatHeatingLevel` | int | Подогрев сиденья R (0-3) |
| `airPm2_5` | int | PM2.5 внутри (мкг/м³) |
| `airPm2_5_outcar` | int | PM2.5 снаружи (мкг/м³) |
| `airAQSStatus` | int | AQS: `0`=выкл, `1`=вкл |
| `acRapidCooling` | int | Быстрое охлаждение: `0`=выкл, `1`=вкл |
| `acOneButtonWarmth` | int | Быстрый нагрев: `0`=выкл, `1`=вкл |

**Пример:**
```java
@Override
public void onAirConditionChanged(AirCondition ac) {
    Log.d("CAN", "AC включен: " + (ac.getAirACStatus() == 1));
    Log.d("CAN", "Температура: " + ac.getAirLeftTemperature() + "°C");
    Log.d("CAN", "Скорость вентилятора: " + ac.getAirWindSpeed());
    Log.d("CAN", "PM2.5 внутри: " + ac.getAirPm2_5());
}
```

---

### 3. СИДЕНЬЯ И РЕМНИ

#### `onSeatBeltChanged(SeatBelt state)`
**Описание:** Изменение состояния ремней безопасности

**Параметры `SeatBelt`:**
| Поле | Тип | Значения |
|------|-----|----------|
| `driverSeatBeltState` | int | Водитель: `0`=пристёгнут, `1`=отстёгнут |
| `passengerSeatBeltState` | int | Пассажир: `0`=пристёгнут, `1`=отстёгнут |
| `secondLeftSeatBeltState` | int | Задний левый: `0`=пристёгнут, `1`=отстёгнут |
| `secondMidSeatBeltState` | int | Задний центр: `0`=пристёгнут, `1`=отстёгнут |
| `secondRightSeatBeltState` | int | Задний правый: `0`=пристёгнут, `1`=отстёгнут |

**Пример:**
```java
@Override
public void onSeatBeltChanged(SeatBelt state) {
    if (state.getDriverSeatBeltState() == SeatBelt.SEATBELT_STATE_PUSHED) {
        Log.w("CAN", "Водитель НЕ пристёгнут!");
    }
}
```

---

#### `onSeatAdjustStateChanged(SeatAdjustState state)`
**Описание:** Изменение положения сидений и зеркал

**Параметры `SeatAdjustState`:**
| Поле | Тип | Значения |
|------|-----|----------|
| `driverSeatBack` | int | Спинка: `0`=нет давления, `1`=вперёд, `2`=назад |
| `driverSeatSlide` | int | Сдвиг: `0`=нет давления, `1`=вперёд, `2`=назад |
| `driverSeatHeight` | int | Высота: `0`=нет давления, `1`=вверх, `2`=вниз |
| `driverSeatAngle` | int | Угол: `0`=нет давления, `1`=вперёд, `2`=назад |
| `leftMirrorSwitch` | int | Левое зеркало: `0`=нет, `1`=нажато |
| `rightMirrorSwitch` | int | Правое зеркало: `0`=нет, `1`=нажато |
| `mirrorAdjust` | int | Регулировка: `0`=нет, `1`=X_UP, `2`=X_DOWN, `3`=Y_LEFT, `4`=Y_RIGHT |

---

### 4. ОСВЕЩЕНИЕ

#### `onLightStatusChanged(LightStatus lightStatus)`
**Описание:** Изменение состояния освещения

**Параметры `LightStatus`:**
| Поле | Тип | Значения |
|------|-----|----------|
| `directionL` | int | Поворотник левый: `0`=выкл, `1`=вкл |
| `directionR` | int | Поворотник правый: `0`=выкл, `1`=вкл |
| `fogFront` | int | Передние ПТФ: `0`=выкл, `1`=вкл |
| `fogRear` | int | Задние ПТФ: `0`=выкл, `1`=вкл |
| `mainBeam` | int | Дальний свет: `0`=выкл, `1`=вкл |
| `dippedBeam` | int | Ближний свет: `0`=выкл, `1`=вкл |
| `positionLamp` | int | Габариты: `0`=выкл, `1`=вкл |
| `dayLight` | int | ДХО: `0`=выкл, `1`=вкл |
| `stopLight` | int | Стоп-сигнал: `0`=выкл, `1`=вкл |
| `reversLight` | int | Задний ход: `0`=выкл, `1`=вкл |
| `cautionLight` | int | Аварийка: `0`=выкл, `1`=вкл |
| `headLight` | int | Фары: `0`=выкл, `1`=вкл |
| `autoLamp` | int | Авто свет: `0`=выкл, `1`=вкл |

**Пример:**
```java
@Override
public void onLightStatusChanged(LightStatus lights) {
    if (lights.getDirectionL() == LightStatus.ON) {
        Log.d("CAN", "Левый поворотник включен");
    }
    if (lights.getReversLight() == LightStatus.ON) {
        Log.d("CAN", "Задний ход включен");
    }
}
```

---

### 5. ДВИГАТЕЛЬ И ТРАНСМИССИЯ

#### `onGearStatusChanged(GearState gear)`
**Описание:** Изменение передачи КПП

**Параметры `GearState` (enum):**
| Значение | Код |
|----------|-----|
| `Parking` | 0 |
| `Reverse` | 1 |
| `Neutral` | 2 |
| `Drive` | 3 |
| `Battery` | 4 |
| `Unknown` | -1 |

**Пример:**
```java
@Override
public void onGearStatusChanged(GearState gear) {
    switch (gear) {
        case Parking:
            Log.d("CAN", "Парковка");
            break;
        case Reverse:
            Log.d("CAN", "Задний ход");
            break;
        case Drive:
            Log.d("CAN", "Движение");
            break;
    }
}
```

---

#### `onEngineSpeedChanged(int speed)`
**Описание:** Обороты двигателя

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `speed` | int | об/мин |

---

#### `onEngineStatusChanged(int status)`
**Описание:** Состояние двигателя

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Выключен |
| `1` | Включен |

---

#### `onEngineTemperatureChanged(int temperature)`
**Описание:** Температура двигателя

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `temperature` | int | °C |

---

#### `onVehicleSpeedChanged(int speed)`
**Описание:** Скорость автомобиля

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `speed` | int | км/ч |

---

#### `onOdometerChanged(Odometer odometer)`
**Описание:** Пробег автомобиля

**Параметры `Odometer`:**
| Поле | Тип | Ед. изм. |
|------|-----|----------|
| `totalMileage` | float | км |
| `tripMileage` | float | км |

---

### 6. ТОПЛИВО И ЭНЕРГИЯ

#### `onFuelLevelChanged(FuelLevel level)`
**Описание:** Уровень топлива

**Параметры `FuelLevel`:**
| Поле | Тип | Описание |
|------|-----|----------|
| `fuelLevel` | int | Уровень (0-100%) |
| `fuelRange` | int | Запас хода (км) |

---

#### `onFuelConsumptionChanged(int consumption)`
**Описание:** Расход топлива

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `consumption` | int | л/100км |

---

#### `onInstantaneousFuelConsumptionChanged(float consumption)`
**Описание:** Мгновенный расход топлива

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `consumption` | float | л/100км |

---

#### `onBatteryStateChanged(BatteryState state)`
**Описание:** Состояние аккумулятора (12V)

**Параметры `BatteryState`:**
| Поле | Тип | Ед. изм. |
|------|-----|----------|
| `mVoltageLevel` | int | Уровень (%) |
| `mVoltageValue` | int | Напряжение (мВ) |
| `mCurrentValue` | float | Ток (A) |

**Пример:**
```java
@Override
public void onBatteryStateChanged(BatteryState state) {
    Log.d("CAN", "Напряжение: " + state.getBatteryVoltage() + " мВ");
    Log.d("CAN", "Ток: " + state.getBatteryCurrentValue() + " A");
}
```

---

#### `onPowerLevelChanged(PowerLevel powerLevel)`
**Описание:** Уровень заряда HV батареи (EV)

**Параметры `PowerLevel`:**
| Поле | Тип | Описание |
|------|-----|----------|
| `powerLevel` | int | Заряд (0-100%) |
| `powerRange` | int | Запас хода (км) |

---

### 7. ТОРМОЗНАЯ СИСТЕМА

#### `onHandBrakeStatusChanged(int status)`
**Описание:** Состояние ручного тормоза

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Отпущен |
| `1` | Затянут |

---

#### `onBrakePadStatusChanged(BrakePadStatus status)`
**Описание:** Состояние тормозных колодок

**Параметры `BrakePadStatus`:**
| Поле | Тип | Описание |
|------|-----|----------|
| `frontBrakePad` | int | Передние: `0`=норма, `1`=износ |
| `rearBrakePad` | int | Задние: `0`=норма, `1`=износ |

---

#### `onBrakeFluidStatusChanged(int status)`
**Описание:** Уровень тормозной жидкости

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Норма |
| `1` | Низкий уровень |

---

### 8. КОЛЁСА И ДАВЛЕНИЕ

#### `onTPMSInfoChange(TPMSInfo tpmsInfo)`
**Описание:** Система контроля давления в шинах

**Параметры `TPMSInfo`:**
| Поле | Тип | Описание |
|------|-----|----------|
| `mLeftFrontTirePressureValue` | float | Давление переднее левое (bar) |
| `mRightFrontTirePressureValue` | float | Давление переднее правое (bar) |
| `mLeftRearTirePressureValue` | float | Давление заднее левое (bar) |
| `mRightRearTirePressureValue` | float | Давление заднее правое (bar) |
| `mLeftFrontTireWarningStatus` | int | Предупреждение FL: `0`=нет, `1`=высокое, `2`=низкое, `3`=утечка |
| `mRightFrontTireWarningStatus` | int | Предупреждение FR |
| `mLeftRearTireWarningStatus` | int | Предупреждение RL |
| `mRightRearTireWarningStatus` | int | Предупреждение RR |
| `mSystemWaringStatus` | int | Система: `0`=норма, `1`=ошибка, `2`=проверка |

**Пример:**
```java
@Override
public void onTPMSInfoChange(TPMSInfo tpms) {
    Log.d("CAN", "Давление FL: " + tpms.mLeftFrontTirePressureValue + " bar");
    if (tpms.mLeftFrontTireWarningStatus == TPMSInfo.PRESSURE_LOW_PRESSURE_WARNING) {
        Log.w("CAN", "Низкое давление в переднем левом колесе!");
    }
}
```

---

### 9. КАМЕРЫ И ВИДЕО

#### `onCameraChanged(CameraState cameraState)`
**Описание:** Состояние камер автомобиля

**Параметры `CameraState`:**
| Поле | Тип | Описание |
|------|-----|----------|
| `dvrCameraState` | int | DVR камера: `0`=выкл, `1`=вкл, `2`=ошибка |
| `roaCameraState` | int | Камера заднего вида |
| `dmsCameraState` | int | DMS камера (мониторинг водителя) |
| `arCameraState` | int | AR камера |
| `dvrHicarState` | int | DVR HiCar состояние |
| `faceidLoginState` | int | Face ID вход: `0`=default, `2`=success, `3`=fail |
| `faceidRegistState` | int | Face ID регистрация |

---

#### `onDVRStateChenaged(DVRState DVRState)`
**Описание:** Состояние видеорегистратора

---

### 10. РАДАРЫ И ПАРКОВКА

#### `onRadarDataChanged(RadarData radarDate)`
**Описание:** Данные парковочных радаров

**Параметры `RadarData`:**
| Поле | Тип | Описание |
|------|-----|----------|
| `leftDistance` | int | Расстояние слева (см) |
| `rightDistance` | int | Расстояние справа (см) |
| `frontDistance` | int | Расстояние спереди (см) |
| `rearDistance` | int | Расстояние сзади (см) |

---

#### `onParkStateChanged(int soundState, int systemState)`
**Описание:** Состояние парктроника

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `soundState` | int | Звук: `0`=тихо, `1`=пилик |
| `systemState` | int | Система: `0`=норма, `1`=ошибка |

---

### 11. РУЛЕВОЕ УПРАВЛЕНИЕ

#### `onReTrackingChanged(int angle)`
**Описание:** Угол поворота руля

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `angle` | int | Градусы |

---

#### `onSWCAngleChanged(SWCAngle swcAngle)`
**Описание:** Угол поворота рулевого колеса

**Параметры `SWCAngle`:**
| Поле | Тип | Ед. изм. |
|------|-----|----------|
| `angle` | int | Градусы (-540...540) |

---

### 12. ТЕМПЕРАТУРА И КЛИМАТ

#### `onAmbientTemperatureChanged(int temperature)`
**Описание:** Температура окружающей среды

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `temperature` | int | °C |

---

#### `onRainFallLevelChanged(int rainfallLevel)`
**Описание:** Интенсивность дождя (датчик дождя)

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Нет дождя |
| `1-5` | Уровень интенсивности |

---

### 13. ЭЛЕКТРОПИТАНИЕ И ЗАЖИГАНИЕ

#### `onCarKeyChanged(int keycode, int keyStatus)`
**Описание:** Состояние ключа зажигания

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `keycode` | int | Код ключа |
| `keyStatus` | int | Статус: `0`=нет, `1`=вставлен, `2`=повёрнут |

---

#### `onVehicleKeyStateChanged(int state)`
**Описание:** Состояние зажигания

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Выключено |
| `1` | ACC |
| `2` | ON |
| `3` | START |

---

#### `onAccStateChanged(int state)`
**Описание:** Состояние ACC режима

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Выключен |
| `1` | Включен |

---

#### `onIlluminationChanged(int illumination)`
**Описание:** Уровень освещённости в салоне

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `illumination` | int | Люксы |

---

### 14. НАВИГАЦИЯ И МАРШРУТ

#### `onTravellingInfo(TravellingInfoType type, float data)`
**Описание:** Информация о поездке

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `type` | enum | Тип данных |
| `data` | float | Значение |

**Типы `TravellingInfoType`:**
- `AVERAGE_SPEED` — Средняя скорость
- `AVERAGE_FUEL_CONSUMPTION` — Средний расход
- `TRIP_DISTANCE` — Расстояние поездки
- `TRIP_TIME` — Время поездки

---

#### `onNaviInfoChanged(NaviInfo info)`
**Описание:** Информация навигации

---

### 15. СИСТЕМНЫЕ СОБЫТИЯ

#### `onVehicleStateChanged(VehicleState vehicle, int state)`
**Описание:** Изменение состояния автомобиля

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `vehicle` | enum | Тип системы |
| `state` | int | Состояние |

---

#### `onVehicleSceneModeChanged(int mode)`
**Описание:** Режим сценария вождения

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Обычный |
| `1` | Спорт |
| `2` | Эко |
| `3` | Комфорт |

---

#### `onHEVSystemModelChanged(int hevMode)`
**Описание:** Режим гибридной системы (HEV/PHEV)

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | EV |
| `1` | HEV |
| `2` | Charge |

---

#### `onVehicleIOChanged(VehicleIO state)`
**Описание:** Состояние входов/выходов автомобиля

---

#### `onRealityWarningInfoChange(RealityWarningInfo realityWarningInfo)`
**Описание:** Предупреждения о реальных событиях

---

#### `onRealityWarningInfoChanged(int key, int value)`
**Описание:** Предупреждение (key-value)

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `key` | int | ID предупреждения |
| `value` | int | Значение |

---

#### `onFactoryModeChanged(String sResult)`
**Описание:** Заводской режим (инженерное меню)

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `sResult` | String | JSON результат |

---

### 16. МЕДИА И АУДИО

#### `onICMReqMediaChanged(int flag, int action, int targetType)`
**Описание:** Запрос медиа-системы

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `flag` | int | Флаг |
| `action` | int | Действие |
| `targetType` | int | Тип цели |

---

#### `onICMReqDialing(int serialNumber)`
**Описание:** Запрос звонка

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `serialNumber` | int | Номер телефона |

---

#### `onICMReqMuteModeChanged(boolean isMute)`
**Описание:** Режим без звука

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `isMute` | boolean | true=без звука |

---

#### `onICMReqCallModeChanged(boolean isVehicleCall)`
**Описание:** Режим вызова через авто

---

#### `onICMReqCallStatusChanged(int status)`
**Описание:** Статус вызова

---

### 17. ЭНЕРГОПОТРЕБЛЕНИЕ (EV)

#### `onEnergyConsumptionPercentChanged(EnergyConsumptionPercent consumptionPercent)`
**Описание:** Процент потребления энергии

---

#### `onEnergyConsumptionInfoChanged(EnergyConsumptionInfo consumptionInfo)`
**Описание:** Информация о потреблении энергии

---

### 18. ПРОЧИЕ СОБЫТИЯ

#### `onEngineFluidStatusChanged(int values)`
**Описание:** Уровень жидкости двигателя

---

#### `onWiperFluidStatusChanged(int status)`
**Описание:** Уровень жидкости омывателя

**Параметры:**
| Значение | Описание |
|----------|----------|
| `0` | Норма |
| `1` | Низкий уровень |

---

#### `onSecondaryOdometerChanged(int odometer)`
**Описание:** Дополнительный одометр

**Параметры:**
| Параметр | Тип | Ед. изм. |
|----------|-----|----------|
| `odometer` | int | км |

---

#### `onAlarmDataChanged(int state)` ⚠️ **@Deprecated**
**Описание:** Устаревший метод сигнализации

---

#### `onCanBoxVersionChange(String ver)` ⚠️ **@Deprecated**
**Описание:** Устаревший метод версии CanBox

---

#### `onWheelSpeedChanged(WheelSpeed speed)` ⚠️ **@Deprecated**
**Описание:** Устаревший метод скорости колёс

---

#### `onWheelCountChanged(WheelCount count)` ⚠️ **@Deprecated**
**Описание:** Устаревший метод подсчёта колёс

---

#### `onVehicleCenterControlEnabledChanged(VehicleCenterControlEnabled vehicle, int enabled)` ⚠️ **@Deprecated**
**Описание:** Устаревший метод центрального управления

---

#### `onVehicleStateSettingResponse(VehicleState vehicle, boolean isSuccess)` ⚠️ **@Deprecated**
**Описание:** Устаревший ответ настройки состояния

---

#### `onBatteryRemainingCapacityChanged(float data)` ⚠️ **@Deprecated**
**Описание:** Устаревший метод остаточной ёмкости батареи

---

#### `onCanRawDataChanged(int canID, Bundle data)`
**Описание:** Сырые данные CAN-шины

**Параметры:**
| Параметр | Тип | Описание |
|----------|-----|----------|
| `canID` | int | ID CAN-сообщения |
| `data` | Bundle | Данные (byte array) |

**Пример:**
```java
@Override
public void onCanRawDataChanged(int canID, Bundle data) {
    byte[] canData = data.getByteArray("data");
    Log.d("CAN", "CAN ID: 0x" + Integer.toHexString(canID));
    Log.d("CAN", "Data: " + Arrays.toString(canData));
}
```

---

## 📝 ПРИМЕР ИСПОЛЬЗОВАНИЯ

```java
public class MyCanBusListener extends CanBusListener {
    private static final String TAG = "MyCanBusListener";

    @Override
    public void onDoorStatusChanged(DoorStatus doorStatus) {
        if (doorStatus.getFLDoor() == DoorStatus.OPEN) {
            Log.w(TAG, "⚠️ Дверь водителя открыта!");
        }
    }

    @Override
    public void onAirConditionChanged(AirCondition ac) {
        Log.d(TAG, "🌡️ Температура: " + ac.getAirLeftTemperature() + "°C");
        Log.d(TAG, "💨 Скорость вентилятора: " + ac.getAirWindSpeed());
    }

    @Override
    public void onGearStatusChanged(GearState gear) {
        Log.d(TAG, "⚙️ Передача: " + gear);
        if (gear == GearState.Reverse) {
            // Автоматически включить камеру заднего вида
            activateRearCamera();
        }
    }

    @Override
    public void onTPMSInfoChange(TPMSInfo tpms) {
        if (tpms.mLeftFrontTireWarningStatus == TPMSInfo.PRESSURE_LOW_PRESSURE_WARNING) {
            Log.e(TAG, "🚨 НИЗКОЕ ДАВЛЕНИЕ в переднем левом колесе!");
        }
    }

    @Override
    public void onSeatBeltChanged(SeatBelt seatBelt) {
        if (seatBelt.getDriverSeatBeltState() == SeatBelt.SEATBELT_STATE_PUSHED) {
            Log.e(TAG, "🚨 Водитель НЕ пристёгнут!");
        }
    }

    private void activateRearCamera() {
        // Логика активации камеры
    }
}
```

---

## 🔧 РЕГИСТРАЦИЯ LISTENER

```java
// 1. Создать listener
CanBusListener listener = new CanBusListener();

// 2. Получить сервис (через AIDL)
ICanBusService canBusService = ICanBusService.Stub.asInterface(
    ServiceManager.getService("canbus_service")
);

// 3. Зарегистрировать callback
try {
    canBusService.registerCallback(listener);
} catch (RemoteException e) {
    Log.e("CAN", "Ошибка регистрации callback", e);
}

// 4. Отписаться при уничтожении
@Override
public void onDestroy() {
    try {
        canBusService.unregisterCallback(listener);
    } catch (RemoteException e) {
        Log.e("CAN", "Ошибка отписки", e);
    }
}
```

---

## ⚠️ ВАЖНЫЕ ЗАМЕЧАНИЯ

1. **Все callback'и выполняются в Binder thread** — не выполняйте длительные операции
2. **Используйте Handler/Looper** для обновления UI
3. **Отписывайтесь при уничтожении** — избегайте утечек памяти
4. **Некоторые методы @Deprecated** — используйте новые аналоги
5. **Значения -1** означают "неизвестно" или "нет данных"

---

**Документация создана:** 2026-04-01  
**Версия CAN-шины:** Qinggan v2.0  
**Количество callback'ов:** 76 (активных: 64, устаревших: 12)
