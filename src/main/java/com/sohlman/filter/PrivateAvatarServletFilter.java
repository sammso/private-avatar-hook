package com.sohlman.filter;

import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

public class PrivateAvatarServletFilter extends BaseFilter {
	
	protected void processFilter(
			HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain)
		throws Exception {

		long profileId = ParamUtil.get(request, "img_id", 0);
		
		if (profileId <= 0) {
			processFilter(
				PrivateAvatarServletFilter.class, request, response, 
				filterChain);
			return;
		}
			
		User currentUser = getCurrentUser(request);
	
		User imageUser = getUserByProfileId(profileId);

		
		if ((currentUser==null) || imageUser==null) {
			return;
		}		
		
		if (currentUser.equals(imageUser)) {
			processFilter(
				PrivateAvatarServletFilter.class, request, response,
				filterChain);
			return;
		}
		
		for (Group currentUserSiteGroup : currentUser.getSiteGroups()) {
			for (Group imageUserSiteGroup : imageUser.getSiteGroups()) {
				if (imageUserSiteGroup.equals(currentUserSiteGroup)) {
					processFilter(
						PrivateAvatarServletFilter.class, request, response, 
						filterChain);
					return;
				}
			}
		}
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
	
	protected User getCurrentUser(HttpServletRequest request) 
		throws PortalException, SystemException {
		
		return PortalUtil.getUser(request);
	}
		
	protected User getUserByProfileId(long profileId) throws SystemException {
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			User.class);
		
		dynamicQuery.add(RestrictionsFactoryUtil.eq("portraitId", profileId));
		
		List<User> userList = UserLocalServiceUtil.dynamicQuery(dynamicQuery);
		
		if (userList.size() > 0) {
			return userList.get(0);
		}
		else {
			return null;
		}
	}

	@Override
	protected Log getLog() {
		return LogFactoryUtil.getLog(PrivateAvatarServletFilter.class);
	}

}
