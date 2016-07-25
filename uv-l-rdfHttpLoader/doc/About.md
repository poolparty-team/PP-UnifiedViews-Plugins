### Description

RDF HTTP Loader is a Data Processing Unit (DPU) or plugin for UnifiedViews to execute update queries on and load the input RDF data into a remote SPARQL endpoint via HTTP based on SPARQL 1.1 Protocol and SPARQL Graph Store HTTP Protocol.

### Configuration parameters

| Name | Description | Example |
|:----|:----|:----|
|**Host** | Resolvable host name or IP address of the target remote SPARQL endpoint | test.poolparty.biz |
|**Port** | Port number of the SPARQL endpoint | 8890 |
|**SPARQL Endpoint** | The path of the SPARQL endpoint relative to base URL | /sparql | 
|**Basic Authentication** | HTTP Basic Authentication for the SPARQL endpoint | true |
|**Username** | Account name of the user granted access to the SPARQL endpoint | dba |
|**Password** | Password of the user | \*\*\* |
|**Input Data Type** | Type of input data for this plugin, see chapter *Input Data Type* for more information | RDF \| File \| SPARQL Update |
|**RDF File Format** | Serializing format of the RDF data when input as file | Turtle |
|**Specify Target Graph** | Customize the loading destination of RDF data | true |
|**Graph URI** | URI of the target RDF graph | http://example.org |
|**SPARQL Update** | SPARQL Update query to be executed on the SPARQL endpoint | delete where {?s ?p ?o} |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|rdfInput|i|RDFDataUnit|RDF data in RDF objects or RDF data structure| |
|fileInput|i|FilesDataUnit|RDF data serialized to a standard RDF serialization file format| |

### Input Data Type

RDF HTTP Loader deals with three types of input data with tasks to be executed on the remote SPARQL endpoint:
* RDF: used when RDF data comes from the *rdfInput* as *RDFDataUnit* represented in Java Objects. In this case the input data is serialized into N-Triples, inserted into a SPARQL Update query, and loaded to the target remote SPARQL endpoint by executing the SPARQL update query. This approach can be used for small datasets.
* File: used when RDF data comes from the *fileInput* as *FilesDataUnit* represented in files based on any W3C standard RDF serialization format. In this case the input data is uploaded to the remote SPARQL endpoint as files in post body. In the meanwhile *RDF File Format* must be specified properly to set the appropriate content type header in the request. This approach is recommended for large datasets. Note that for some RDF databases (e.g., Virtuoso) the SPARQL endpoint for file uploading is different, please adjust the path of SPARQL endpoint accordingly.
* SPARQL Update: used when input data is provided manually in the update query instead of the predecessor DPU or any graph update and management task should be executed on the remote SPARQL endpoint.
Based on the selection of input data type, the corresponding input data source is used to retrieve data. An error will be thrown when the input data type and input data source do not match.

### Graph URI

URI of the target RDF graph on the remote SPARQL endpoint can be specify to describe the destination of the RDF data to be loaded into. The default graph is used if no graph URI is specified by the user. In the case that input data type is a SPARQL update query, this option is disabled because graph operations should be specified in the update query.