#!/usr/bin/env python
"""
Author: Tommaso Soru <tsoru@informatik.uni-leipzig.de>
"""
import sparql
import sys

ENDPOINT = "http://www.linklion.org:8890/sparql"
QUERY_SUB = "select ?s where { ?a prov:wasAssociatedWith <http://www.linklion.org/version/???> . ?m prov:wasGeneratedBy ?a . ?l prov:wasDerivedFrom ?m . ?l rdf:subject ?s } limit 10000 offset "

QUERY_OBJ = "select ?s where { ?a prov:wasAssociatedWith <http://www.linklion.org/version/???> . ?m prov:wasGeneratedBy ?a . ?l prov:wasDerivedFrom ?m . ?l rdf:object ?s } limit 10000 offset "

for vers in ["LIMES-0-5", "Silk-2-6-0", "DBPedia_Information_Extraction_Framework-0-1", "sameasOrgDumpFramework-2012-07-21"]:
    print "Doing: " + vers
    with open(vers + '.txt', 'w') as f:
        for i in xrange(0, 25000000, 10000):
            s = sparql.Service(ENDPOINT, "utf-8", "GET")
            result = s.query(QUERY_SUB.replace("???", vers) + str(i))
            j = 0
            print "Opening file..."
            for row in result.fetchone():
                j += 1
                f.write(str(row[0])+"\n")
            print "Done: " + str(i) + " rows=" + str(j)
            if j == 0:
                break
        for i in xrange(0, 25000000, 10000):
            s = sparql.Service(ENDPOINT, "utf-8", "GET")
            result = s.query(QUERY_OBJ.replace("???", vers) + str(i))
            j = 0
            print "Opening file..."
            for row in result.fetchone():
                j += 1
                f.write(str(row[0])+"\n")
            print "Done: " + str(i) + " rows=" + str(j)
            if j == 0:
                break
