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
package com.cloudx.ios17.core.utils;

import android.app.usage.UsageStats;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Created by Amit Kumar Email : mr.doc10jl96@gmail.com */
public class ListUtil {
    @SafeVarargs
    public static <T> List<T> asSafeList(T... tArr) {
        return (tArr == null || tArr.length == 0) ? new ArrayList() : Arrays.asList(tArr);
    }

    /**
     * To compare if two lists of {@link android.app.usage.UsageStats} contain same
     * packages or not.
     *
     * @return true if both the lists contain the same packages otherwise false.
     */
    public static boolean areEqualLists(List<UsageStats> first, List<UsageStats> second) {
        if (first == null) {
            if (second == null) {
                return true;
            } else {
                return false;
            }
        }
        if (second == null) {
            return false;
        }

        Set<String> packages = new HashSet<>();
        for (UsageStats usageStats : first) {
            packages.add(usageStats.getPackageName());
        }

        for (UsageStats usageStats : second) {
            packages.remove(usageStats.getPackageName());
        }

        return packages.size() == 0;
    }
}
