<%--
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
--%>

<%@ include file="/init.jsp" %>

<%
String portletNamespace = PortalUtil.getPortletNamespace(LinkedPagesPortletKeys.LINKED_PAGES);

PortletURL linkedPagesPanelURL = PortletURLFactoryUtil.create(request, LinkedPagesPortletKeys.LINKED_PAGES, PortletRequest.RENDER_PHASE);

linkedPagesPanelURL.setWindowState(LiferayWindowState.EXCLUSIVE);
%>

<li class="control-menu-nav-item">
	<a class="control-menu-icon lfr-portal-tooltip product-menu-toggle sidenav-toggler"
		data-qa-id="Linked Pages" data-content="body" data-title="Linked Pages"
		data-url="<%= linkedPagesPanelURL.toString() %>"
		data-toggle="sidenav"
		data-type="fixed-push"
		data-open-class="open-admin-panel"
		data-target="#<%= portletNamespace %>linkedPagesPanelId"
		href="#<%= portletNamespace %>linkedPagesPanelId" id="<%= portletNamespace %>linkedPagesToggleId">

		<aui:icon image="link" markupView="lexicon" />
	</a>
</li>

<script>
	$('.sidenav-toggler').sideNavigation({
		url: '/view.jsp'
	});
</script>