import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class QueryToProductIdSolrCache extends AbstractSolrCache<String, List<String>> {
  private static final String ID = "id";
  private final String pageSize;

  public QueryToProductIdSolrCache(SolrClient client, int maxCacheSize, int pageSize) {
    super(client, maxCacheSize);
    this.pageSize = String.valueOf(pageSize);
  }

  @Override
  public List<String> loadFromSolr(String query) {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.add("q", query);
    params.add("fl", ID);
    params.add("rows", pageSize);
    try {
      QueryResponse response = client.query(params);
      return response.getResults().stream()
          .map(document -> (String) document.getFirstValue(ID)).collect(Collectors.toList());
    } catch (SolrServerException | IOException e) {
      throw new RuntimeException("Can't load key for " + query, e);
    }
  }
}
