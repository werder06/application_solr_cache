import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {
  public static void main(String[] args) throws ExecutionException {
    SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/rules").build();
    QueryToProductIdSolrCache cache = new QueryToProductIdSolrCache(client, 10, 30);

    List<String> ids = cache.get("searchTermWordsCount:1");

    System.out.println(ids);

  }
}
