# PoolParty UnifiedViews Plugins

## Introduction

This repository publishes PoolParty plugins developed by the PoolParty Development Team to integrate PoolParty with UnifiedViews such that users are able consume its Web services in UnifiedViews.

Currently two plugins PoolPartyConceptExtractor and RDFHttpLoader are available. Please step into each module for plugin-specific information.

## Introduction to UnifiedViews

UnifiedViews is a complete open source production ready system for data governance, which addresses concerns such as data integration, data management, quality assurance, in addition to conventional ETL. It allows users such as publishers, consumers or analysts to not only define and execute tasks to process data, but also debug, schedule and monitor the execution of tasks. Other subsidiary functionalities such as logging, user management and access control are indispensable ingredients for enterprises as well.

UnifiedViews is designed to be decoupled into the frontend part and the backend part running in individual instances. The frontend application provides a view of all the aforementioned functionalities and interacts with users through its Web-based GUI, while the backend application takes care of the actual data processing. All the configurations are shared across the frontend and the backend through a relational database as a broker. Such decoupling significantly improves scalability, by allowing the backend to be clustered according to the scale of tasks.

By default UnifiedViews has already provided a wide range of data processing units commonly used in data integration such as extractor for relational databases and transformer from tabular data to RDF data. Moreover, it is a general purpose solution with high extensibility based on its modular architecture and standardized APIs, supporting developers to customize and create their own plugins or data processing units for the pipelines according to various use cases. Such customization potentials are the true power of UnifiedViews, enabling the capability to interact and coordinate any external process.

The source code of UnifiedViews is hosted by GitHub, including a wide range of plugins for data processing:

[UnifiedViews GitHub Repository](https://github.com/UnifiedViews)

[Semantic Web Company](http://www.semantic-web.at), the owner of PoolParty, is one of the main contributor to UnifiedViews. Any contribution to this project is highly welcome.

## Introduction to PoolParty

PoolParty Semantic Suite is a world-class semantic technology suite that offers sharply focused solutions to transform knowledge organization and content business. As a semantic middleware, PoolParty enriches all kinds of information with valuable metadata and links business and content assets automatically. 

For more information related to PoolParty, please refer to:

[PoolParty Official Website](https://www.poolparty.biz/)

## License

All the PoolParty Plugins published in this repository is licensed with [LGPL3](./LICENSE).
