# CEDAL: Time-Efficient Detection of Erroneous Links in Large-Scale Link Repositories

## Abstract
More than 500 million facts on the Linked Data Web are statements across knowledge bases. These links are of crucial importance for the Linked Data Web as they make a large number of tasks possible including cross-ontology, question answering and federated queries. However, a large number of these links are erroneous and can thus lead to these applications producing absurd results. We present a time-efficient and complete approach for the detection of erroneous links for properties that are transitive. To this end, we make use of the semantics of URIs on the Data Web and combine it with an efficient graph partitioning algorithm. We then apply our algorithm to the LinkLion repository and show that we can analyze 19,200,114 links in 4.6 minutes. Our results show that at least 5% of `owl:sameAs` links we considered are erroneous. In addition, our analysis of the  provenance of links allows discovering agents and knowledge bases that commonly display poor linking.
Our algorithm can be easily executed in parallel and on a GPU. We show that these implementations are up to two orders of magnitude faster than a non-parallel implementation and classical reasoners.

## How to run

Command line:
```
java -Xmx128G -jar runCEDAL.jar dirLinks
```

where `dirLinks` is the directory where the knownledge base files (linkset files, dump files) are located in.

## Additional content

* [LinkLion](http://www.linklion.org/)
* [Dump files from LinkLion without duplicates](https://www.dropbox.com/s/m24xoxzm0h60ywl/correct.tar.gz?dl=1)
* [Output sample from CEDAL](http://tinyurl.com/100SampleCEDAL)
* [File listing the datasets and knowledge base files without errors](http://tinyurl.com/cedalresults)
* [CEDAL applied to educational LinkSets](https://github.com/firmao/CEDAL/tree/master/CEDALEducation)

## Contact:
* André Valdestilhas

valdestilhas@informatik.uni-leipzig.de
