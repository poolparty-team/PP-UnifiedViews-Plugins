### Description

This DPU connects to the REST APIs of Atlassian Confluence and Jira installations, extracts the information and expresses it as RDF data using the DIO ontology. It expects Confluence Requirements documents to be hierarchically structured (under a page titled "Requirements PP") and follow a template-based layout. For instance, requirements documents must have a _h2_ Section titled "Goals", design intent documents must contain a _h2_ Section titled "User Story".  

### Configuration parameters

| Name | Description |
|:----|:----|
|**confluenceApiBaseUri** | The URI where the Confluence instance API is available, e.g., "https://grips.semantic-web.at/rest/api/". The DPU assumes that a page titled "Requirements PP" exists in a space called "PP". From there it consumes the pages down the hierarchy and extracts their content.|
|**jiraApiBaseUri** | The URI where the Jira instance API is available, e.g., "http://jira-dev.semantic-web.at:8080/rest/api/latest/". |
|**jiraProjectKeys** | Comma-separated list of keys of Jira projects that should be retrieved, e.g., "POOL" or "PPS". |
|**username** | Username for both Confluence and Jira. |
|**password** | Password for both Confluence and Jira. |

### Inputs and outputs

|Name |Type | DataUnit | Description | Mandatory |
|:--------|:------:|:------:|:-------------|:---------------------:|
|output |o |WritableRDFDataUnit |Produced RDF data |x|
