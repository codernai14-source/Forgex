package com.forgex.mobile.core.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters。
 *
 * 提供 Long <-> LocalDateTime (epoch millis) 和 Map<String, String> <-> String (JSON) 转换。
 * JSON 序列化使用 Gson, 与 Retrofit GsonConverterFactory 保持一致。
 */
class Converters {

    private val gson = Gson()

    /**
     * epoch millis (Long) -> LocalDateTime。
     *
     * @param value epoch millis, 可为 null
     * @return LocalDateTime, null 输入返回 null
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): java.time.LocalDateTime? {
        return value?.let {
            java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                java.time.ZoneId.systemDefault()
            )
        }
    }

    /**
     * LocalDateTime -> epoch millis (Long)。
     *
     * @param date LocalDateTime, 可为 null
     * @return epoch millis, null 输入返回 null
     */
    @TypeConverter
    fun toTimestamp(date: java.time.LocalDateTime?): Long? {
        return date?.atZone(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    /**
     * JSON String -> Map<String, String>。
     *
     * @param value JSON 字符串, 可为 null
     * @return Map, null 或空输入返回空 Map
     */
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, String> {
        if (value.isNullOrEmpty()) {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }

    /**
     * Map<String, String> -> JSON String。
     *
     * @param map Map, 可为 null
     * @return JSON 字符串, null 输入返回 null
     */
    @TypeConverter
    fun toStringMap(map: Map<String, String>?): String? {
        if (map == null || map.isEmpty()) {
            return null
        }
        return gson.toJson(map)
    }
}
