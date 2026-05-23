package ru.voboost.voice.services.qgbus

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * Класс события для QGBus. Идентичен системному com.qinggan.bus.QGBusEvent.
 */
class QGBusEvent : Parcelable {
    companion object {
        const val EVENT_LOW_PRIORITY = 0
        const val EVENT_NORMAL_PRIORITY = 1
        const val EVENT_HIGH_PRIORITY = 2

        @JvmField
        val CREATOR: Parcelable.Creator<QGBusEvent> = object : Parcelable.Creator<QGBusEvent> {
            override fun createFromParcel(parcel: Parcel): QGBusEvent = QGBusEvent(parcel)
            override fun newArray(size: Int): Array<QGBusEvent?> = arrayOfNulls(size)
        }
    }

    var eventType: String? = null
    var needCache: Int = 0
    var source: String? = null
    var destination: String? = null
    var priority: Int = EVENT_NORMAL_PRIORITY
    var data: Bundle? = null

    constructor() {}

    constructor(parcel: Parcel) {
        eventType = parcel.readString()
        needCache = parcel.readInt()
        source = parcel.readString()
        destination = parcel.readString()
        priority = parcel.readInt()
        data = parcel.readBundle()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventType)
        parcel.writeInt(needCache)
        parcel.writeString(source)
        parcel.writeString(destination)
        parcel.writeInt(priority)
        parcel.writeBundle(data)
    }

    fun isSticky(): Boolean = needCache > 0
    fun setSticky(sticky: Boolean) {
        needCache = if (sticky) 1 else 0
    }
}
