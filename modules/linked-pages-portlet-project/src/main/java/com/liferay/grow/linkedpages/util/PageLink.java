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

package com.liferay.grow.linkedpages.util;

import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Laszlo Hudak
 */
public class PageLink implements Comparable<PageLink> {

	public PageLink(String pageTitle, String pageLink) {
		_pageTitle = pageTitle;
		_pageLink = pageLink;
	}

	@Override
	public int compareTo(PageLink pageLink) {
		return StringUtil.toLowerCase(_pageTitle).compareTo(
			StringUtil.toLowerCase(pageLink.getPageTitle()));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PageLink)) {
			return false;
		}

		PageLink pageLink = (PageLink)obj;

		if (_pageLink.equals(pageLink.getPageLink()) &&
			_pageTitle.equals(pageLink.getPageTitle())) {

			return true;
		}

		return false;
	}

	public String getPageLink() {
		return _pageLink;
	}

	public String getPageTitle() {
		return _pageTitle;
	}

	public void setPageLink(String pageLink) {
		_pageLink = pageLink;
	}

	public void setPageTitle(String pageTitle) {
		_pageTitle = pageTitle;
	}

	private String _pageLink;
	private String _pageTitle;

}