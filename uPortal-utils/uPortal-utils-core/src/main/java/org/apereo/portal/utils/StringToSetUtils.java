package org.apereo.portal.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Utils class to load a String property into Set with space separator. */
public abstract class StringToSetUtils {

    public static Set<String> delimitedSpaceListToSet(final String entry) {
        return new HashSet<>(Arrays.asList(entry.split("\\s+")));
    }
}
