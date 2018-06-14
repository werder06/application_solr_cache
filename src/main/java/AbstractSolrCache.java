
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public abstract class AbstractSolrCache<K, V> {
  private static final String INDEX_VERSION = "indexversion";

  protected final SolrClient client;
  private final Cache<K, V> backendCache;
  private volatile Long cachedIndexVersion;


  public AbstractSolrCache(SolrClient client, int maxCacheSize) {
    this.client = client;
    this.backendCache = CacheBuilder.newBuilder()
        .maximumSize(maxCacheSize).build(
            new CacheLoader<K, V>() {
              public V load(K key) {
                return loadFromSolr(key);
              }
            });
  }


  public V get(K k) throws ExecutionException {
    Long currentIndexVersion = getVersionFromSolr();
    if (!currentIndexVersion.equals(cachedIndexVersion)) {
      backendCache.invalidateAll();
      cachedIndexVersion = currentIndexVersion;
    }
    return backendCache.get(k);
  }

  public abstract V loadFromSolr(K k);

  private Long getVersionFromSolr() {
    ModifiableSolrParams solrParams = new ModifiableSolrParams();
    solrParams.add("command", INDEX_VERSION);

    QueryRequest queryRequest = new QueryRequest(solrParams);
    queryRequest.setPath("/replication");

    try {
      NamedList<Object> namedList = client.request(queryRequest);
      return (Long) namedList.get(INDEX_VERSION);
    } catch (SolrServerException | IOException e) {
      throw new RuntimeException("Can't load index version from Solr", e);
    }
  }
}
