package org.jasig.portal.utils;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Created by jgribonvald on 05/07/16.
 */
public abstract class StringToSetUtils {

    public static Set<String> delimitedSpaceListToSet(final String entry) {
        return Sets.newHashSet(entry.split("\\s+"));
    }
}
