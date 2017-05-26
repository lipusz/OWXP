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

package com.liferay.wiki.internal.service;

import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.mentions.configuration.MentionsGroupServiceConfiguration;
import com.liferay.mentions.util.MentionsNotifier;
import com.liferay.mentions.util.MentionsUtil;
import com.liferay.portal.kernel.diff.DiffHtmlUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.LayoutConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.notifications.UserNotificationDefinition;
import com.liferay.portal.kernel.portlet.PortletURLFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.settings.LocalizedValuesMap;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.NotificationThreadLocal;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.SubscriptionSender;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.wiki.configuration.WikiGroupServiceOverriddenConfiguration;
import com.liferay.wiki.constants.WikiConstants;
import com.liferay.wiki.constants.WikiPortletKeys;
import com.liferay.wiki.engine.impl.WikiEngineRenderer;
import com.liferay.wiki.model.WikiNode;
import com.liferay.wiki.model.WikiPage;
import com.liferay.wiki.model.WikiPageConstants;
import com.liferay.wiki.service.WikiPageLocalService;
import com.liferay.wiki.service.WikiPageLocalServiceWrapper;
import com.liferay.wiki.util.WikiUtil;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Istvan Sajtos
 * @author Norbert Kocsis
 * @author Tamas Molnar
 */
@Component(immediate = true, service = ServiceWrapper.class)
public class WikiPageCustomServiceWrapper extends WikiPageLocalServiceWrapper {

	public WikiPageCustomServiceWrapper() {
		super(null);
	}

	public WikiPageCustomServiceWrapper(
		WikiPageLocalService wikiPageLocalService) {

		super(wikiPageLocalService);
	}

	@Override
	public WikiPage addPage(
			long userId, long nodeId, String title, double version,
			String content, String summary, boolean minorEdit, String format,
			boolean head, String parentTitle, String redirectTitle,
			ServiceContext serviceContext)
		throws PortalException {

		WikiPage page = super.addPage(
			userId, nodeId, title, version, content, summary, minorEdit, format,
			head, parentTitle, redirectTitle, serviceContext);

		if (!ExportImportThreadLocal.isImportInProcess()) {
			super.subscribePage(userId, nodeId, title);
		}

		return page;
	}

	@Override
	public WikiPage addPage(
			long userId, long nodeId, String title, String content,
			String summary, boolean minorEdit, ServiceContext serviceContext)
		throws PortalException {

		WikiPage page = super.addPage(
			userId, nodeId, title, content, summary, minorEdit, serviceContext);

		if (!ExportImportThreadLocal.isImportInProcess()) {
			super.subscribePage(userId, nodeId, title);
		}

		return page;
	}

	@Override
	public WikiPage updateStatus(
			long userId, WikiPage page, int status,
			ServiceContext serviceContext,
			Map<String, Serializable> workflowContext)
		throws PortalException {

		int oldStatus = page.getStatus();

		page = super.updateStatus(
			userId, page, status, serviceContext, workflowContext);

		WikiGroupServiceOverriddenConfiguration
			wikiGroupServiceOverriddenConfiguration =
				_configurationProvider.getConfiguration(
					WikiGroupServiceOverriddenConfiguration.class,
					new GroupServiceSettingsLocator(
						page.getGroupId(), WikiConstants.SERVICE_NAME));

		// Subscriptions

		if (status == WorkflowConstants.STATUS_APPROVED) {
			if (NotificationThreadLocal.isEnabled() &&
				(!page.isMinorEdit() ||
				 wikiGroupServiceOverriddenConfiguration.
					 pageMinorEditSendEmail())) {

				notifySubscribers(
					userId, page,
					(String)workflowContext.get(WorkflowConstants.CONTEXT_URL),
					serviceContext);
			}
		}

		if ((status != WorkflowConstants.STATUS_APPROVED) ||
			(oldStatus == WorkflowConstants.STATUS_IN_TRASH)) {

			return page;
		}

		long siteGroupId = PortalUtil.getSiteGroupId(page.getGroupId());

		if (!MentionsUtil.isMentionsEnabled(siteGroupId)) {
			return page;
		}

		String contentURL = (String)serviceContext.getAttribute("contentURL");

		if (Validator.isNull(contentURL)) {
			serviceContext.setAttribute(
				"contentURL", workflowContext.get("url"));
		}

		String portalURL = serviceContext.getPortalURL();

		String attachmentURLPrefix = WikiUtil.getAttachmentURLPrefix(
			serviceContext.getPathMain(), serviceContext.getPlid(),
			page.getNodeId(), page.getTitle());

		attachmentURLPrefix = portalURL + attachmentURLPrefix;

		String pageContent = _wikiEngineRenderer.convert(
			page, null, null, attachmentURLPrefix);

		MentionsGroupServiceConfiguration mentionsGroupServiceConfiguration =
			_configurationProvider.getCompanyConfiguration(
				MentionsGroupServiceConfiguration.class, page.getCompanyId());

		_mentionsNotifier.notify(
			page.getUserId(), page.getGroupId(), page.getTitle(), pageContent,
			WikiPage.class.getName(), page.getPageId(),
			mentionsGroupServiceConfiguration.assetEntryMentionEmailSubject(),
			mentionsGroupServiceConfiguration.assetEntryMentionEmailBody(),
			serviceContext);

		return page;
	}

	protected String getDiffsURL(
			WikiPage page, WikiPage previousVersionPage,
			ServiceContext serviceContext)
		throws PortalException {

		if (previousVersionPage == null) {
			return StringPool.BLANK;
		}

		HttpServletRequest request = serviceContext.getRequest();

		if (request == null) {
			return StringPool.BLANK;
		}

		PortletURL portletURL = null;

		long plid = serviceContext.getPlid();

		if (plid == LayoutConstants.DEFAULT_PLID) {
			portletURL = PortalUtil.getControlPanelPortletURL(
				request, WikiPortletKeys.WIKI_ADMIN,
				PortletRequest.RENDER_PHASE);
		}
		else {
			portletURL = PortletURLFactoryUtil.create(
				request, WikiPortletKeys.WIKI, plid,
				PortletRequest.RENDER_PHASE);
		}

		portletURL.setParameter(
			"mvcRenderCommandName", "/wiki/compare_versions");
		portletURL.setParameter("nodeId", String.valueOf(page.getNodeId()));
		portletURL.setParameter("title", page.getTitle());
		portletURL.setParameter(
			"sourceVersion", String.valueOf(previousVersionPage.getVersion()));
		portletURL.setParameter(
			"targetVersion", String.valueOf(page.getVersion()));
		portletURL.setParameter("type", "html");

		return portletURL.toString();
	}

	protected void notifySubscribers(
			long userId, WikiPage page, String pageURL,
			ServiceContext serviceContext)
		throws PortalException {

		if (!page.isApproved() || Validator.isNull(pageURL)) {
			return;
		}

		WikiGroupServiceOverriddenConfiguration
			wikiGroupServiceOverriddenConfiguration =
				_configurationProvider.getConfiguration(
					WikiGroupServiceOverriddenConfiguration.class,
					new GroupServiceSettingsLocator(
						page.getGroupId(), WikiConstants.SERVICE_NAME));

		boolean update = false;

		if (page.getVersion() > WikiPageConstants.VERSION_DEFAULT) {
			update = true;
		}

		if (!update &&
			wikiGroupServiceOverriddenConfiguration.emailPageAddedEnabled()) {
		}
		else if (update &&
				 wikiGroupServiceOverriddenConfiguration.
					 emailPageUpdatedEnabled()) {
		}
		else {
			return;
		}

		String portalURL = serviceContext.getPortalURL();

		WikiPage previousVersionPage = getPreviousVersionPage(page);

		String attachmentURLPrefix = WikiUtil.getAttachmentURLPrefix(
			serviceContext.getPathMain(), serviceContext.getPlid(),
			page.getNodeId(), page.getTitle());

		attachmentURLPrefix = portalURL + attachmentURLPrefix;

		String pageDiffs = StringPool.BLANK;

		try {
			pageDiffs = _wikiEngineRenderer.diffHtml(
				previousVersionPage, page, null, null, attachmentURLPrefix);
		}
		catch (Exception e) {
		}

		String pageContent = null;

		if (Objects.equals(page.getFormat(), "creole")) {
			pageContent = _wikiEngineRenderer.convert(
				page, null, null, attachmentURLPrefix);
		}
		else {
			pageContent = page.getContent();
			pageContent = WikiUtil.processContent(pageContent);
		}

		String pageTitle = page.getTitle();

		String fromName =
			wikiGroupServiceOverriddenConfiguration.emailFromName();
		String fromAddress =
			wikiGroupServiceOverriddenConfiguration.emailFromAddress();

		LocalizedValuesMap subjectLocalizedValuesMap = null;
		LocalizedValuesMap bodyLocalizedValuesMap = null;

		if (update) {
			subjectLocalizedValuesMap =
				wikiGroupServiceOverriddenConfiguration.
					emailPageUpdatedSubject();
			bodyLocalizedValuesMap =
				wikiGroupServiceOverriddenConfiguration.emailPageUpdatedBody();
		}
		else {
			subjectLocalizedValuesMap =
				wikiGroupServiceOverriddenConfiguration.emailPageAddedSubject();
			bodyLocalizedValuesMap =
				wikiGroupServiceOverriddenConfiguration.emailPageAddedBody();
		}

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setClassName(page.getModelClassName());
		subscriptionSender.setClassPK(page.getPageId());
		subscriptionSender.setCompanyId(page.getCompanyId());
		subscriptionSender.setContextAttribute(
			"[$PAGE_CONTENT$]", pageContent, false);
		subscriptionSender.setContextAttribute(
			"[$PAGE_DIFFS$]", DiffHtmlUtil.replaceStyles(pageDiffs), false);

		WikiNode node = page.getNode();

		subscriptionSender.setContextAttributes(
			"[$DIFFS_URL$]",
			getDiffsURL(page, previousVersionPage, serviceContext),
			"[$NODE_NAME$]", node.getName(), "[$PAGE_DATE_UPDATE$]",
			page.getModifiedDate(), "[$PAGE_ID$]", page.getPageId(),
			"[$PAGE_SUMMARY$]", page.getSummary(), "[$PAGE_TITLE$]", pageTitle,
			"[$PAGE_URL$]", pageURL);

		subscriptionSender.setContextCreatorUserPrefix("PAGE");
		subscriptionSender.setCreatorUserId(page.getUserId());
		subscriptionSender.setCurrentUserId(userId);
		subscriptionSender.setEntryTitle(pageTitle);
		subscriptionSender.setEntryURL(pageURL);
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);

		if (bodyLocalizedValuesMap != null) {
			subscriptionSender.setLocalizedBodyMap(
				LocalizationUtil.getMap(bodyLocalizedValuesMap));
		}

		if (subjectLocalizedValuesMap != null) {
			subscriptionSender.setLocalizedSubjectMap(
				LocalizationUtil.getMap(subjectLocalizedValuesMap));
		}

		subscriptionSender.setMailId(
			"wiki_page", page.getNodeId(), page.getPageId());

		int notificationType =
			UserNotificationDefinition.NOTIFICATION_TYPE_ADD_ENTRY;

		if (update) {
			notificationType =
				UserNotificationDefinition.NOTIFICATION_TYPE_UPDATE_ENTRY;
		}

		subscriptionSender.setNotificationType(notificationType);

		subscriptionSender.setPortletId(WikiPortletKeys.WIKI);
		subscriptionSender.setReplyToAddress(fromAddress);
		subscriptionSender.setScopeGroupId(page.getGroupId());
		subscriptionSender.setServiceContext(serviceContext);

		subscriptionSender.addPersistedSubscribers(
			User.class.getName(), userId);

		subscriptionSender.flushNotificationsAsync();
	}

	@Reference(unbind = "-")
	protected void setConfigurationProvider(
		ConfigurationProvider configurationProvider) {

		_configurationProvider = configurationProvider;
	}

	@Reference(unbind = "-")
	protected void setMentionsNotifier(MentionsNotifier mentionsNotifier) {
		_mentionsNotifier = mentionsNotifier;
	}

	@Reference(unbind = "-")
	protected void setWikiEngineRenderer(
		WikiEngineRenderer wikiEngineRenderer) {

		_wikiEngineRenderer = wikiEngineRenderer;
	}

	@Reference(unbind = "-")
	protected void setWikiPageLocalService(
		WikiPageLocalService wikiPageLocalService) {

		_wikiPageLocalService = wikiPageLocalService;
	}

	private ConfigurationProvider _configurationProvider;
	private MentionsNotifier _mentionsNotifier;
	private WikiEngineRenderer _wikiEngineRenderer;
	private WikiPageLocalService _wikiPageLocalService;

}