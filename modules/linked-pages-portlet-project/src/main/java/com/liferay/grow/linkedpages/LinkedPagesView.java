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

package com.liferay.grow.linkedpages;

import com.liferay.grow.linkedpages.util.PageLink;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.service.WikiNodeLocalServiceUtil;
import com.liferay.wiki.service.WikiPageLocalServiceUtil;

import java.util.ArrayList;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Laszlo Hudak
 */
public class LinkedPagesView {

	/**
	 * Finding a linked pages in a wiki page
	 * Based on Grow Navigation ADT
	 * https://grow.liferay.com/group/guest/people/-/wiki/Grow/Grow+navigation+ADT
	 */
	public LinkedPagesView(ThemeDisplay themeDisplay) {
		_linkedPages = new TreeSet<>();
		HttpServletRequest request = themeDisplay.getRequest();

		String wikiNode = ParamUtil.getString(
			request,
			"p_r_p__http://www.liferay.com/public-render-parameters" +
				"/wiki_nodeName");
		String wikiTitle = ParamUtil.getString(
			request,
			"p_r_p__http://www.liferay.com/public-render-parameters" +
				"/wiki_title");

		long groupId = themeDisplay.getScopeGroupId();

		fillLinkedPages(groupId, wikiNode, wikiTitle);
	}

	public void fillLinkedPages(
		long groupId, String nodeTitle, String pageTitle) {

		try {
			long nodeId = WikiNodeLocalServiceUtil.getNode(
				groupId, nodeTitle).getNodeId();

			WikiPage wikiPage = WikiPageLocalServiceUtil.getPage(
				nodeId, pageTitle);

			String content = wikiPage.getContent();

			if (wikiPage.getFormat().equals("creole")) {
				_addLinksCreole(content);
			}
			else if (wikiPage.getFormat().equals("html")) {
				_addLinksHTML(content);
			}
			else if (wikiPage.getFormat().equals("markdown")) {
				_addLinksMarkdown(content);
			}
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe.getMessage());
			}
		}
	}

	public ArrayList<PageLink> getLinkedPages() {
		ArrayList<PageLink> linkedPages = new ArrayList<>();

		linkedPages.addAll(_linkedPages);

		return linkedPages;
	}

	private void _addLink(String link) {
		if (!link.contains(_GROW_URL + _PUBLIC_PAGE)) {
			String title = link.substring(link.lastIndexOf('/') + 1);

			title = title.replace('+', CharPool.SPACE);

			if (title.contains("#section")) {
				title = title.substring(0, title.indexOf("#section"));
			}

			_linkedPages.add(new PageLink(title, link));
		}
	}

	private void _addLinksCreole(String content) {
		while (content.indexOf(_GROW_URL) > 0) {
			content = content.substring(content.indexOf(_GROW_URL));

			String link = content.substring(0, content.indexOf("]]"));

			if (link.contains("|")) {
				link = link.substring(0, link.indexOf("|"));
			}

			_addLink(link);

			content = content.substring(content.indexOf("]]") + 2);
		}
	}

	private void _addLinksHTML(String content) {
		while (content.indexOf(_GROW_URL) > 0) {
			content = content.substring(content.indexOf(_GROW_URL));

			String link = content.substring(0, content.indexOf("\">"));

			_addLink(link);

			content = content.substring(content.indexOf("</a>") + 4);
		}
	}

	private void _addLinksMarkdown(String content) {
		content = content.replace(CharPool.NEW_LINE, CharPool.SPACE).replace(
			CharPool.CLOSE_BRACKET, CharPool.SPACE);

		String[] contentElements = StringUtil.split(content, CharPool.SPACE);

		for (String element : contentElements) {
			if (element.contains(_GROW_URL)) {
				if (element.startsWith("(")) {
					element = element.substring(1);
				}

				if (element.endsWith(")")) {
					element = element.substring(0, element.length() - 1);
				}

				_addLink(element);
			}
		}
	}

	private static final String _GROW_URL = "https://grow.liferay.com/";

	private static final String _PUBLIC_PAGE = "web";

	private static final Log _log = LogFactoryUtil.getLog(
		LinkedPagesView.class);

	private TreeSet<PageLink> _linkedPages;

}