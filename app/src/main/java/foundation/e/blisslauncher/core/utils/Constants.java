/*
 * Copyright 2018 /e/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package foundation.e.blisslauncher.core.utils;

import foundation.e.blisslauncher.BuildConfig;

/** Created by falcon on 8/3/18. */
public class Constants {
    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String PREF_NAME = "foundation.e.blisslauncher.prefs";

    public static final String WEATHER_SOURCE = "weather_source";
    public static final String WEATHER_USE_CUSTOM_LOCATION = "weather_use_custom_location";
    public static final String WEATHER_CUSTOM_LOCATION_CITY = "weather_custom_location_city";
    public static final String WEATHER_CUSTOM_LOCATION = "weather_custom_location";
    public static final String WEATHER_USE_METRIC = "weather_use_metric";
    public static final String WEATHER_REFRESH_INTERVAL = "weather_refresh_interval";
    public static final String WEATHER_FONT_COLOR = "weather_font_color";
    public static final String WEATHER_ICONS = "weather_icons";

    public static final String MONOCHROME = "mono";
    public static final String COLOR_STD = "color";

    // other shared pref entries
    public static final String WEATHER_LAST_UPDATE = "last_weather_update";
    public static final String WEATHER_DATA = "weather_data";

    // First run is used to hide the initial no-weather message for a better OOBE
    public static final String WEATHER_FIRST_UPDATE = "weather_first_update";

    public static final String DEFAULT_LIGHT_COLOR = "#ffffffff";
    public static final String DEFAULT_DARK_COLOR = "#80ffffff";

    public static final String USER_CREATION_TIME_KEY = "user_creation_time_";

    /** Represents types of item displayed in Launcher. */
    public static final int ITEM_TYPE_APPLICATION = 0;

    public static final int ITEM_TYPE_SHORTCUT = 1;
    public static final int ITEM_TYPE_FOLDER = 2;

    /** Represents types of container. */
    public static final int CONTAINER_DESKTOP = -100;

    public static final int CONTAINER_HOTSEAT = -101;
}
