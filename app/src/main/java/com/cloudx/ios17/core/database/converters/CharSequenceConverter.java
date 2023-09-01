package com.cloudx.ios17.core.database.converters;

import androidx.room.TypeConverter;

public class CharSequenceConverter {

    @TypeConverter
    public static String toString(CharSequence value) {
        return value.toString();
    }

    @TypeConverter
    public static CharSequence toCharSequence(String value) {
        return value;
    }
}
