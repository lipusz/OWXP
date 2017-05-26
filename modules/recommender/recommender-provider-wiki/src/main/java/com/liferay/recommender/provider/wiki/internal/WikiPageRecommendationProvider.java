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

package com.liferay.recommender.provider.wiki.internal;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.recommender.provider.RecommendationProvider;
import com.liferay.social.kernel.model.SocialActivity;
import com.liferay.social.kernel.model.SocialActivityConstants;
import com.liferay.social.kernel.service.SocialActivityLocalService;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiPageLocalService;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tibor Lipusz
 * @author Gergely Mathe
 */
@Component(
	immediate = true,
	property = {"recommendation.provider.type=com.liferay.wiki.model.WikiPage"},
	service = RecommendationProvider.class
)
public class WikiPageRecommendationProvider
	implements RecommendationProvider<WikiPage> {

	public static final String PROVIDER_TYPE = WikiPage.class.getName();

	@Override
	public String getProviderType() {
		return PROVIDER_TYPE;
	}

	@Override
	public List<WikiPage> getRecommendations(long userId, int maxEntries)
		throws Exception {

		// TODO Implement :-)

		if (_log.isDebugEnabled()) {
			_log.debug("Invoked");
		}

		return new ArrayList<>();
	}

	@Override
	public List<String> getRecommendationURLs(long userId, int maxEntries)
		throws Exception {

		// TODO Implement :-)

		if (_log.isDebugEnabled()) {
			_log.debug("Invoked");
		}

		return new ArrayList<>();
	}

	/**
	 * TODO Amadea
	*/
	protected List<WikiPage> getLikedWikiPages(long userId) {
		return null;
	}

	/**
	 * TODO Tibi
	 */
	protected List<WikiPage> getSubscribedWikiPages(long userId) {
		return null;
	}

	protected List<WikiPage> getVisitedWikiPages(long userId) {
		DynamicQuery socialActivityDynamicQuery =
			_socialActivityLocalService.dynamicQuery();

		Property userIdProperty = PropertyFactoryUtil.forName("userId");

		socialActivityDynamicQuery.add(userIdProperty.eq(userId));

		Property classNameIdProperty =
			PropertyFactoryUtil.forName("classNameId");

		socialActivityDynamicQuery.add(
			classNameIdProperty.eq(
				_portal.getClassNameId(WikiPage.class.getName())));

		Property typeProperty = PropertyFactoryUtil.forName("type_");

		socialActivityDynamicQuery.add(
			typeProperty.eq(SocialActivityConstants.TYPE_VIEW));

		socialActivityDynamicQuery.addOrder(
			OrderFactoryUtil.desc("createDate"));

		List<SocialActivity> socialActivities =
			_socialActivityLocalService.dynamicQuery(
				socialActivityDynamicQuery);

		List<WikiPage> wikiPages = new ArrayList<>();

		for (SocialActivity socialActivity : socialActivities) {
			wikiPages.add(
				_wikiPageLocalService.fetchPage(socialActivity.getClassPK()));
		}

		return wikiPages;
	}

	@Reference(unbind = "-")
	protected void setSocialActivityLocalService(
		SocialActivityLocalService socialActivityLocalService) {

		_socialActivityLocalService = socialActivityLocalService;
	}

	@Reference(unbind = "-")
	protected void setWikiPageLocalService(
		WikiPageLocalService wikiPageLocalService) {

		_wikiPageLocalService = wikiPageLocalService;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		WikiPageRecommendationProvider.class);

	@Reference
	private Portal _portal;

	private SocialActivityLocalService _socialActivityLocalService;
	private WikiPageLocalService _wikiPageLocalService;

}