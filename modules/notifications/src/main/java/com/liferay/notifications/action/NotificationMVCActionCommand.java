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

package com.liferay.notifications.action;

import com.liferay.notifications.web.internal.constants.NotificationsPortletKeys;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.UserNotificationEventLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tamas Molnar
 */
@Component(
	immediate = true,
	property = {
		"javax.portlet.name=" + NotificationsPortletKeys.NOTIFICATIONS,
		"mvc.command.name=/notifications/markAllAsRead"
	},
	service = MVCActionCommand.class
)
public class NotificationMVCActionCommand extends BaseMVCActionCommand {

	@Override
	public void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		ThemeDisplay themeDisplay = (ThemeDisplay)actionRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		ActionableDynamicQuery userNotificationEventActionableDynamicQuery =
			getUserNotificationEventActionableDynamicQuery(
				themeDisplay.getUserId());

		userNotificationEventActionableDynamicQuery.performActions();
	}

	protected ActionableDynamicQuery
		getUserNotificationEventActionableDynamicQuery(final long userId) {

		ActionableDynamicQuery userNotificationEventActionableDynamicQuery =
			_userNotificationEventLocalService.getActionableDynamicQuery();

		userNotificationEventActionableDynamicQuery.setAddCriteriaMethod(
			new ActionableDynamicQuery.AddCriteriaMethod() {

				@Override
				public void addCriteria(DynamicQuery dynamicQuery) {
					Property userIdProperty = PropertyFactoryUtil.forName(
						"userId");

					dynamicQuery.add(userIdProperty.eq(userId));

					Property deliveryTypeProperty = PropertyFactoryUtil.forName(
						"deliveryType");

					dynamicQuery.add(
						deliveryTypeProperty.eq(
							UserNotificationDeliveryConstants.TYPE_WEBSITE));

					Property archivedProperty = PropertyFactoryUtil.forName(
						"archived");

					dynamicQuery.add(archivedProperty.eq(Boolean.FALSE));
				}

			});

		userNotificationEventActionableDynamicQuery.setPerformActionMethod(
			new ActionableDynamicQuery.
				PerformActionMethod<UserNotificationEvent>() {

				@Override
				public void performAction(
						UserNotificationEvent userNotificationEvent)
					throws PortalException {

					userNotificationEvent =
						_userNotificationEventLocalService.
							fetchUserNotificationEvent(
								userNotificationEvent.
									getUserNotificationEventId());

					userNotificationEvent.setArchived(true);

					_userNotificationEventLocalService.
						updateUserNotificationEvent(userNotificationEvent);
				}

			});

		return userNotificationEventActionableDynamicQuery;
	}

	@Reference(unbind = "-")
	protected void setUserNotificationEventLocalService(
		UserNotificationEventLocalService userNotificationEventLocalService) {

		_userNotificationEventLocalService = userNotificationEventLocalService;
	}

	private UserNotificationEventLocalService
		_userNotificationEventLocalService;

}