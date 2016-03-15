package org.aksw.jena_sparql_api.lookup;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;


public class LookupServiceTransformValue<K, W, V>
    implements LookupService<K, W>
{
    private LookupService<K, V> base;
    private Function<? super V, W> fn;

    public LookupServiceTransformValue(LookupService<K, V> base, Function<? super V, W> fn) {
        this.base = base;
        this.fn = fn;
    }

    @Override
    public Map<K, W> apply(Iterable<K> keys) {
        Map<K, V> tmp = base.apply(keys);
        Map<K, W> result = Maps.transformValues(tmp, GuavaFunctionWrapper.wrap(fn));
        return result;
    }

    @Deprecated
    public static <K, W, V> LookupServiceTransformValue<K, W, V> create(LookupService<K, V> base, com.google.common.base.Function<? super V, W> fn) {
        LookupServiceTransformValue<K, W, V> result = new LookupServiceTransformValue<K, W, V>(base, (arg) -> fn.apply(arg));
        return result;
    }


    public static <K, W, V> LookupServiceTransformValue<K, W, V> create(LookupService<K, V> base, Function<? super V, W> fn) {
        LookupServiceTransformValue<K, W, V> result = new LookupServiceTransformValue<K, W, V>(base, fn);
        return result;
    }
}
