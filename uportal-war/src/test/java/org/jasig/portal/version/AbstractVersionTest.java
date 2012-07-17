package org.jasig.portal.version;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSortedSet;

public class AbstractVersionTest {
    @Test
    public void testVersionComparison() {
        SimpleVersion v1 = new SimpleVersion(4, 3, 2);
        SimpleVersion v2 = new SimpleVersion(4, 3, 2);
        
        assertEquals(0, v1.compareTo(v2));
        assertEquals(0, v2.compareTo(v1));
        assertFalse(v1.isBefore(v2));
        assertFalse(v2.isBefore(v1));

        
        v2 = new SimpleVersion(4, 3, 3);
        assertEquals(-1, v1.compareTo(v2));
        assertEquals(1, v2.compareTo(v1));
        assertTrue(v1.isBefore(v2));
        assertFalse(v2.isBefore(v1));
        
        Set<SimpleVersion> set = ImmutableSortedSet.of(v1, v2);
        Assert.assertArrayEquals(new SimpleVersion[] { v1, v2 }, set.toArray(new SimpleVersion[0]));
        
        
        v1 = new SimpleVersion(5, 0, 0);
        assertEquals(1, v1.compareTo(v2));
        assertEquals(-1, v2.compareTo(v1));
        assertFalse(v1.isBefore(v2));
        assertTrue(v2.isBefore(v1));
        
        set = ImmutableSortedSet.of(v1, v2);
        Assert.assertArrayEquals(new SimpleVersion[] { v2, v1 }, set.toArray(new SimpleVersion[0]));
    }
    
    private static final class SimpleVersion extends AbstractVersion {
        private static final long serialVersionUID = 1L;
        
        private final int major;
        private final int minor;
        private final int patch;
        
        public SimpleVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int getMajor() {
            return major;
        }

        @Override
        public int getMinor() {
            return minor;
        }

        @Override
        public int getPatch() {
            return patch;
        }
    }
}
