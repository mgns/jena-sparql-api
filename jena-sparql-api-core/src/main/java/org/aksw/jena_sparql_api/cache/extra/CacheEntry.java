package org.aksw.jena_sparql_api.cache.extra;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:39 PM
 */
public interface CacheEntry {

    long getTimestamp();
    long getLifespan();
    InputStreamProvider getInputStreamProvider();
}
