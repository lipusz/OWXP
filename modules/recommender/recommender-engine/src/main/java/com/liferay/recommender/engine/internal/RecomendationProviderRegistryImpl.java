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

package com.liferay.recommender.engine.internal;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.recommender.provider.RecommendationProvider;
import com.liferay.recommender.provider.RecommendationProviderRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Tibor Lipusz
 */
@Component(immediate = true, service = RecommendationProviderRegistry.class)
public class RecomendationProviderRegistryImpl
	implements RecommendationProviderRegistry {

	@Override
	public <T> RecommendationProvider<T> getRecommendationProvider(
		String className) {

		return (RecommendationProvider<T>)_recommendationProviders.get(
			className);
	}

	@Override
	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY, unbind = "unregister"
	)
	public void register(RecommendationProvider<?> recommendationProvider) {
		_recommendationProviders.put(
			recommendationProvider.getProviderType(), recommendationProvider);

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Registered provider for type " +
					recommendationProvider.getProviderType());
		}
	}

	@Override
	public void unregister(RecommendationProvider<?> recommendationProvider) {
		_recommendationProviders.remove(
			recommendationProvider.getProviderType());

		if (_log.isDebugEnabled()) {
			_log.debug(
				"Removed provider for type " +
					recommendationProvider.getProviderType());
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		RecomendationProviderRegistryImpl.class);

	private final Map<String, RecommendationProvider<?>>
		_recommendationProviders = new ConcurrentHashMap<>();

}