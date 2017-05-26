/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.recommender.service.impl;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.spring.extender.service.ServiceReference;
import com.liferay.recommender.provider.RecommendationProvider;
import com.liferay.recommender.provider.RecommendationProviderRegistry;
import com.liferay.recommender.service.base.RecommenderServiceBaseImpl;

import java.util.List;

/**
 * TODO Write JavaDoc
 *
 * The implementation of the recommender remote service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link com.liferay.recommender.service.RecommenderService} interface.
 *
 * <p>
 * This is a remote service. Methods of this service are expected to have security checks based on the propagated JAAS credentials because this service can be accessed remotely.
 * </p>
 *
 * @author Tibor Lipusz
 */
@ProviderType
public class RecommenderServiceImpl extends RecommenderServiceBaseImpl {

	@Override
	public List<String> getRecommendations(
			long userId, String className, int maxEntries)
		throws PortalException {

		// TODO Do permission check
		// PermissionChecker permissionChecker = getPermissionChecker();

		// TODO Check registory

		if (recommendationProviderRegistry == null) {
			throw new PortalException();
		}

		RecommendationProvider<?> recommendationProvider =
			recommendationProviderRegistry.getRecommendationProvider(className);

		if (recommendationProvider == null) {
			throw new UnsupportedOperationException(
				"Recommendations for " + className + " is not supported.");
		}

		try {
			return recommendationProvider.getRecommendationURLs(
				userId, maxEntries);
		}
		catch (Exception exception) {
			throw new PortalException(exception);
		}
	}

	@ServiceReference(type = RecommendationProviderRegistry.class)
	protected RecommendationProviderRegistry recommendationProviderRegistry;

}