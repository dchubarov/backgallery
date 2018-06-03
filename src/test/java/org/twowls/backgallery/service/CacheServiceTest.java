package org.twowls.backgallery.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.twowls.backgallery.utils.Equipped;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Tests of {@link CacheService}.
 *
 * @author Dmitry Chubarov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CacheService.class)
public class CacheServiceTest {

    @Autowired
    CacheService cache;

    @Before
    public void clearCache() {
        cache.evictAll();
    }

    @Test
    public void whenAddingSubsequentEntry_thenFactoryInvokedOnce() {
        AtomicInteger factoryInvocationCount = new AtomicInteger();
        Function<String, CacheableString> factory = s -> {
            factoryInvocationCount.incrementAndGet();
            return new CacheableString("Hello world");
        };

        Equipped<CacheableString> s1 = cache.getOrCreate("value1", CacheableString.class, factory);
        Equipped<CacheableString> s2 = cache.getOrCreate("value1", CacheableString.class, factory);

        assertEquals(1, factoryInvocationCount.get());
        assertSame(s2, s1);

        assertEquals("value1", s2.name());
        assertEquals("Hello world", s2.bare().stringValue());
        assertNotNull(s1.prop(CacheService.CREATE_TIME_PROP));
    }

    @Test
    public void whenEntryEvicted_thenFactoryInvokedAgain() {
        AtomicInteger factoryInvocationCount = new AtomicInteger();
        Function<String, CacheableDouble> factory = s -> {
            factoryInvocationCount.incrementAndGet();
            return new CacheableDouble(3.1415926);
        };

        Equipped<CacheableDouble> d1 = cache.getOrCreate("dbl1", CacheableDouble.class, factory);
        assertNotNull(cache.evict("dbl1", CacheableDouble.class));
        Equipped<CacheableDouble> d2 = cache.getOrCreate("dbl1", CacheableDouble.class, factory);

        assertEquals(2, factoryInvocationCount.get());
        assertNotSame(d1, d2);

        assertEquals("dbl1", d2.name());
        assertEquals(3.1415926, d2.bare().doubleValue(), 0.0001);
        assertNotNull(d2.prop(CacheService.CREATE_TIME_PROP));
    }

    static class CacheableString {
        private final String value;

        CacheableString(String value) {
            this.value = value;
        }

        String stringValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '@' + Objects.hashCode(this) +
                    " { stringValue=\"" + this.value + "\" }";
        }
    }

    static class CacheableDouble {
        private final double value;

        CacheableDouble(double value) {
            this.value = value;
        }

        double doubleValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '@' + Objects.hashCode(this) +
                    " { doubleValue=" + this.value + " }";
        }
    }
}
