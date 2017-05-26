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

package com.liferay.user.notification.event.service;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.service.PortalPreferencesLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceWrapper;
import com.liferay.portal.kernel.service.UserNotificationEventLocalServiceWrapper;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortletKeys;

import org.osgi.service.component.annotations.Component;

/**
 * @author Tamas Molnar
 */
@Component(immediate = true, property = {}, service = ServiceWrapper.class)
public class UserNotificationEventCustomServiceWrapper
	extends UserNotificationEventLocalServiceWrapper {

	public UserNotificationEventCustomServiceWrapper() {
		super(null);
	}

	@Override
	public UserNotificationEvent sendUserNotificationEvents(
			long userId, String portletId, int deliveryType,
			JSONObject notificationEventJSONObject)
		throws PortalException {

		UserNotificationEvent userNotificationEvent =
			super.sendUserNotificationEvents(
				userId, portletId, deliveryType, notificationEventJSONObject);

		storeUserNotificationEventsCount(userId, deliveryType);

		return userNotificationEvent;
	}

	protected void storeUserNotificationEventsCount(
		long userId, int deliveryType) {

		if (deliveryType != UserNotificationDeliveryConstants.TYPE_WEBSITE) {
			return;
		}

		PortalPreferences portalPreferences =
			PortletPreferencesFactoryUtil.getPortalPreferences(userId, true);

		long userNotificationEventsCount = GetterUtil.getLong(
			portalPreferences.getValue(
				UserNotificationEvent.class.getName(),
				"userNotificationEventsCount"));

		portalPreferences.setValue(
			UserNotificationEvent.class.getName(),
			"userNotificationEventsCount",
			String.valueOf(++userNotificationEventsCount));

		PortalPreferencesLocalServiceUtil.updatePreferences(
			userId, PortletKeys.PREFS_OWNER_TYPE_USER,
			PortletPreferencesFactoryUtil.toXML(portalPreferences));
	}

}