### Description

PoolParty Concept Extractor is a plugin for UnifiedViews to consume the Concept Extraction service provided by PoolParty Thesaurus Server. Given a list of triples with string literal objects representing texts as input, this extractor annotates texts against a knowledge organization managed by PoolParty and produces annotations in RDF triples as output. Please refer to the following documentation for more information about PoolParty.

[PoolParty Extractor API Reference Documentation](http://vocabulary.semantic-web.at/extractor/doc)
[PoolParty Extractor Guide](https://grips.semantic-web.at/display/public/POOLDOKU/PPX+-+Guide)
[PoolParty Manual](https://grips.semantic-web.at/display/POOLDOKU/PoolParty+Manual)

### Configuration parameters

| Name | Description | Example |
|:----|:----|:----|
|**Host** | Resolvable host name or IP address of a PoolParty thesaurus server | test.poolparty.biz |
|**Port** | Port number of PoolParty services | 80 |
|**Extraction service path** | PoolParty Concept Extraction service path relative to base URL | /extractor/api/annotate | 
|**Project ID** | Project identifier of the PoolParty thesaurus to be extracted against | 1DCE358B-0316-0001-D178-1C371D0019B0 |
|**Language code** | Two-digit ISO 639-1 code of source language of the texts to be extracted | en |
|**Username** | Account name of a user for the target PoolParty thesaurus server | test |
|**Password** | Password of a user for the target PoolParty thesaurus server | **** |
|**Corpus ID** | Identifier of a corpus in the project used to adapt scores with corpus analysis | 1C41C4B1-7654-2546-A341-14A2DB543D542 |
|**Number of terms to return** | Maximum number of terms to return | 25 |
|**Number of concepts to return** | Maximum number of concepts to return | 25 |
|**useTransitiveBroaderConcepts** | Retrieve transitive broader concepts of the extracted concepts | false |
|**useTransitiveBroaderTopConcepts** | Retrieve transitive broader top concepts of the extracted concepts | false |
|**useRelatedConcepts** | Retrieve related concepts of the extracted concepts | false |
|**filterNestedConcepts** | Nested concept filter removes concepts matches which are contained within other matches | true |
|**tfidfScoring** | The scores of the concepts and terms are weighted by tfidf (term frequency-inverse document frequency) formula | false |
|**useTypes** | Retrieve the custom types for concepts | false |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|input|i|RDFDataUnit|RDF input where the object of each triple must be a string literal representing the text to be annotated|x|
|output|o|RDFDataUnit|RDF output where a graph of annotated concepts and terms is created for each triple from the input|x|

### Example

Given the following triple as the input of the extractor, 
`<http://example.org> <http://example.org#title> "organization" .`

The extraction result for the string literal "organization" against the [Eurovoc](http://vocabulary.semantic-web.at/CBeurovoc.html) vocabulary will be annotations described in a graph:
```
@prefix rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix skos:	<http://www.w3.org/2004/02/skos/core#> .
@prefix xsd:	<http://www.w3.org/2001/XMLSchema#> .
@prefix ns1:	<http://commontag.org/ns#> .
@prefix ns3:	<http://schema.semantic-web.at/ppx/> .
@prefix ns4:	<http://vocabulary.semantic-web.at/CBeurovoc/> .
@prefix ns5:	<http://purl.org/dc/terms/> .
@prefix ns6:	<http://schema.semantic-web.at/ppx/title/8e8e2302-4946-4dcc-b7ca-d4e64f4d6cbf#> .
<http://example.org>	ns3:titleIsTaggedBy	ns6:id .
ns6:id	ns5:description	"organization"^^xsd:string ;
	ns1:tagged	<ppx:86110fda-e59d-4d7f-9b15-cddb76b7eb7e> .
<ppx:86110fda-e59d-4d7f-9b15-cddb76b7eb7e>	rdf:type	ns1:AutoTag .
<ppx:86110fda-e59d-4d7f-9b15-cddb76b7eb7e>	ns3:score	100.0 ;
	ns1:label	"organisation"@en .
<ppx:86110fda-e59d-4d7f-9b15-cddb76b7eb7e>	ns1:means	ns4:C4189 ;
	ns1:taggingDate	"Wed Oct 21 10:37:48 UTC 2015"^^xsd:string .
ns4:C4189	rdf:type	skos:Concept ;
	skos:inScheme	ns4:D40 ;
	skos:prefLabel	"organisation"@en ;
	skos:altLabel	"legal status of an undertaking"@en ,
		"organization"@en .
```