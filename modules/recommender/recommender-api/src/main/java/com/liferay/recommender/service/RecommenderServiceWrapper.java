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

package com.liferay.recommender.service;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link RecommenderService}.
 *
 * @author Brian Wing Shun Chan
 * @see RecommenderService
 * @generated
 */
@ProviderType
public class RecommenderServiceWrapper implements RecommenderService,
	ServiceWrapper<RecommenderService> {
	public RecommenderServiceWrapper(RecommenderService recommenderService) {
		_recommenderService = recommenderService;
	}

	/**
	* Returns the OSGi service identifier.
	*
	* @return the OSGi service identifier
	*/
	@Override
	public java.lang.String getOSGiServiceIdentifier() {
		return _recommenderService.getOSGiServiceIdentifier();
	}

	@Override
	public java.util.List<java.lang.String> getRecommendations(long userId,
		java.lang.String className, int maxEntries)
		throws com.liferay.portal.kernel.exception.PortalException {
		return _recommenderService.getRecommendations(userId, className,
			maxEntries);
	}

	@Override
	public RecommenderService getWrappedService() {
		return _recommenderService;
	}

	@Override
	public void setWrappedService(RecommenderService recommenderService) {
		_recommenderService = recommenderService;
	}

	private RecommenderService _recommenderService;
}