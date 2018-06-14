Assume there is an Application backed by remote Solr node. Data in Solr could be modified independently from Application.
But there is a need to have cache in Application with 'up to date' data from Solr.
For example, if you use Solr as a persistence storage for some kind of data which is needed for each request, but amount of this data is big,
so extraction and network transmission could affect latency. Lets cache it on Application layer, but will keep in synch with Solr by following
this steps:
- Cache Solr's index version on Application side
- Read current index version from Solr on each get(...) operation. It's simple and cheap, see
https://github.com/apache/lucene-solr/blob/6e0da7e2f83ca3291f1f167cfad00ad2e4d3abd7/solr/core/src/java/org/apache/solr/handler/ReplicationHandler.java#L251
how '.../replication?command=indexversion' works
- Compare cached and current index versions, invalidate cache if version was changed.
Make sure that there is happens-before relationship between cache invalidation and update of cached version of index!
