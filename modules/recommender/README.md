## Liferay Recommender (TODO - Write Doc)

 :exclamation: **This project is actively being developed thus the content of this file is not finalized either**. :exclamation:  
 
## Structure

### `recommender-api`  

- `com.liferay.recommender.provider.RecommendationProvider`: Provides the RecommendationProvider generic interface, allowing custom recommendation provider creation.
- `com.liferay.recommender.provider.RecommendationProviderRegistry`: Provides the RecommendationProviderRegistry interface. Class implementing this interface are supposed to track RecommendationProvider services.
- `com.liferay.recommender.service` package: The remote service API.  

### `recommender-engine`
- `com.liferay.recommender.engine.internal.RecomendationProviderRegistryImpl`: Default implementation for `RecommendationProviderRegistry`.  

### `recommender-provider-wiki`  
- `com.liferay.recommender.provider.wiki.internal.WikiPageRecommendationProvider`: Recommendation provider for model `com.liferay.wiki.model.WikiPage`.  

### `recommender-service`
- `com.liferay.recommender.service.impl.RecommenderServiceImpl`: Implementation for the remote recommender service API.

## Development

1. Create a new [Liferay Workspace](https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/liferay-workspace) [with Blade CLI](https://dev.liferay.com/develop/tutorials/-/knowledge_base/7-0/creating-a-liferay-workspace-with-blade-cli)  
2. Clone this repository  
3. Copy all files and folders except for the "modules" directory from the newly initialized Liferay Workspace into the root of the local copy of this repository  
4. Open [root]/`gradle.properties` and edit property `liferay.workspace.home.dir` to configure where to deploy the built artifacts  

Now you should be able to use Gradle Wrapper to build and deploy the modules.
