PoolParty Concept Extractor
----------

v1.0.0
---
* All functions provided by PoolParty Concept Extraction API are supported. Please see the following documents for more information.
[PoolParty Extractor API Reference Documentation](http://vocabulary.semantic-web.at/extractor/doc)
[PoolParty Extractor Guide](https://grips.semantic-web.at/display/public/POOLDOKU/PPX+-+Guide)
[PoolParty Manual](https://grips.semantic-web.at/display/POOLDOKU/PoolParty+Manual)

v2.0.0
---
* PoolParty Concept Extractor now supports extraction on text files with formats such as Word, Excel, Powerpoint, Pdf, Open Document.

v2.1.0
---
* Fixed bug in single extraction error.
* Subject, predicate and the first 200 characters of the object are logged in case of an unsuccessful extraction for the RDF input.
* Execution progress report is moved to event level logs for higher significance.
* Extraction model is checked and synchronized before extraction.