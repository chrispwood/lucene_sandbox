lucene_sandbox
==============

My sandbox of lucene indexers

There are two simple shell scripts for simplifying the index and search operations at the root level:

1. **lucene:** Usage: lucene index_directory data_directory. The index_directory indicates where you would like to put the lucene index. The data_directory points to the directory you want to index
2. **search:** Usage: search index_directory query_string. The index_directory is the location of the lucene inverted index. The query_string is the search query.
