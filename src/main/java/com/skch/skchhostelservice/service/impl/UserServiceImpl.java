package com.skch.skchhostelservice.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.skch.skchhostelservice.dao.UsersDAO;
import com.skch.skchhostelservice.dto.Navigation;
import com.skch.skchhostelservice.dto.Result;
import com.skch.skchhostelservice.dto.SubNavigarion;
import com.skch.skchhostelservice.dto.UserDTO;
import com.skch.skchhostelservice.dto.UserPrivilegeDTO;
import com.skch.skchhostelservice.exception.CustomException;
import com.skch.skchhostelservice.mapper.ObjectMapper;
import com.skch.skchhostelservice.model.Resource;
import com.skch.skchhostelservice.model.Roles;
import com.skch.skchhostelservice.model.UserPrivilege;
import com.skch.skchhostelservice.model.UserRole;
import com.skch.skchhostelservice.model.Users;
import com.skch.skchhostelservice.service.UserService;
import com.skch.skchhostelservice.util.Utility;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	private ObjectMapper MAPPER = ObjectMapper.INSTANCE;

	@Autowired
	private UsersDAO usersDAO;

	/**
	 * User Save Or Update Method
	 */
	@Override
	public Result saveOrUpdateUser(UserDTO dto) {
		Result result = new Result();
		try {
			Users user = usersDAO.findByEmailIdIgnoreCase(dto.getEmailId());
			if (dto.getUserId() == null || dto.getUserId() == 0) {
				if (user != null) {
					result.setStatusCode(HttpStatus.BAD_REQUEST.value());
					result.setErrorMessage("Email Id Already Exist...");
				} else {
					Users saveUser = MAPPER.fromUserDTO(dto);

					saveUser.setIsActive(true);
//					saveUser.setCreatedDate(new Date());
//					saveUser.setModifiedDate(new Date());
					Utility.updateFields(saveUser,"C");
					String userUuid = UUID.randomUUID().toString() + "#" + System.currentTimeMillis();
					saveUser.setUserUuid(userUuid);
					// Set The User Role
					setUserRoles(dto, saveUser);

					// Set User Privileges
					setUserPrivileges(dto, saveUser);

					Users serverUser = usersDAO.save(saveUser);
					result.setData(serverUser);
					result.setStatusCode(HttpStatus.OK.value());
					result.setSuccessMessage("User Saved .....");

				}
			} else {
				if (user != null) {
					// Update Properties from dto
					user.setFirstName(dto.getFirstName());
					user.setLastName(dto.getLastName());
					user.setDob(Utility.dateConvert(dto.getDob()));
					user.setPhoneNumber(dto.getPhoneNumber());
//					user.setModifiedById(dto.getModifiedById());
//					user.setModifiedDate(new Date());
					Utility.updateFields(user,"U");
					
					if (user.getUserRole().getRoles().getRoleId() != dto.getRoleId()) {
						Roles role = new Roles();
						role.setRoleId(dto.getRoleId());
						user.getUserRole().setRoles(role);
//						user.getUserRole().setModifiedById(dto.getModifiedById());
//						user.getUserRole().setModifiedDate(new Date());
						Utility.updateFields(user.getUserRole(),"U");
					}

					List<UserPrivilege> userPrivileges = user.getUserPrivilege();
					userPrivileges.sort(
							Comparator.comparingLong(userPrivilege -> userPrivilege.getResource().getResourceId()));
					List<UserPrivilegeDTO> userPrivilegeDTOs = dto.getUserPrivilege();
					Collections.sort(userPrivilegeDTOs, Comparator.comparing(UserPrivilegeDTO::getResourceId));

					for (int i = 0; i < userPrivilegeDTOs.size(); i++) {
						UserPrivilege userPrivilege = userPrivileges.get(i);
						UserPrivilegeDTO userPrivilegeDTO = userPrivilegeDTOs.get(i);

						userPrivilege.setReadOnlyFlag(userPrivilegeDTO.getReadOnlyFlag());
						userPrivilege.setReadWriteFlag(userPrivilegeDTO.getReadWriteFlag());
						userPrivilege.setTerminateFlag(userPrivilegeDTO.getTerminateFlag());
//						userPrivilege.setModifiedById(dto.getModifiedById());
//						userPrivilege.setModifiedDate(new Date());
						Utility.updateFields(userPrivilege,"U");

					}
					user.setUserPrivilege(userPrivileges);

					user = usersDAO.save(user);
					result.setData(user);
					result.setStatusCode(HttpStatus.OK.value());
					result.setSuccessMessage("Updated...");

				}
			}
		} catch (Exception e) {
			log.error("Error in saveOrUpdateUser.... ::" + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

	public void setUserRoles(UserDTO dto, Users users) {
		try {
			UserRole userRole = new UserRole();
			Roles roles = new Roles();
			roles.setRoleId(dto.getRoleId());
			userRole.setRoles(roles);
			userRole.setActive(true);
//			userRole.setCreatedById(dto.getCreatedById());
//			userRole.setCreatedDate(new Date());
//			userRole.setModifiedById(dto.getModifiedById());
//			userRole.setModifiedDate(new Date());
			Utility.updateFields(userRole,"C");
			userRole.setUsers(users);

			users.setUserRole(userRole);
		} catch (Exception e) {
			log.error("Error in setUserRoles .... ::" + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void setUserPrivileges(UserDTO dto, Users users) {
		try {
			List<UserPrivilege> userPrivilegeList = new ArrayList<>();
			for (UserPrivilegeDTO userPrivilegeDTO : dto.getUserPrivilege()) {
				UserPrivilege userPrivilege = MAPPER.fromUserPrivilegeDTO(userPrivilegeDTO);
				Resource resource = new Resource();
				resource.setResourceId(userPrivilegeDTO.getResourceId());
				userPrivilege.setResource(resource);
				userPrivilege.setIsActive(true);
//				userPrivilege.setCreatedById(dto.getCreatedById());
//				userPrivilege.setCreatedDate(new Date());
//				userPrivilege.setModifiedById(dto.getModifiedById());
//				userPrivilege.setModifiedDate(new Date());
				Utility.updateFields(userPrivilege,"C");

				userPrivilege.setUsers(users);
				userPrivilegeList.add(userPrivilege);
			}
			users.setUserPrivilege(userPrivilegeList);
		} catch (Exception e) {
			log.error("Error in setUserPrivileges .... ::" + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public Result navigations(Long userId) {
		Result result = new Result();
		SortedMap<Long, Object> navMap = new TreeMap<>();
		try {
			Users user = usersDAO.findByUserId(userId);

			user.getUserPrivilege().stream()
					.filter(obj -> obj.getIsActive() && obj.getReadOnlyFlag() 
							&& obj.getResource().getIsSubnav().equals("N"))
					.forEach(obj -> {
						Navigation navigation = new Navigation();
						BeanUtils.copyProperties(obj.getResource(), navigation);
						navMap.put(obj.getResource().getDisplayOrder(), navigation);
					});
			
			user.getUserPrivilege().stream()
			.filter(obj -> obj.getIsActive() && obj.getReadOnlyFlag() 
					&& obj.getResource().getIsSubnav().equals("Y"))
			.collect(Collectors.groupingBy(obj->obj.getResource().getDisplayOrder()))
			.forEach((displyOrder,listNav)->{
				List<Navigation> navList = new ArrayList<>();
				SubNavigarion nav = new SubNavigarion();
				listNav.forEach(obj -> {
					Navigation navigation = new Navigation();
					BeanUtils.copyProperties(obj.getResource(), navigation);
					navList.add(navigation);	
					});
				if(!listNav.isEmpty()) {
					nav.setResourceName(listNav.get(0).getResource().getParentName());
					nav.setIcon(listNav.get(0).getResource().getParentIcon());
					nav.setDisplayOrder(listNav.get(0).getResource().getDisplayOrder());
					nav.setSubNav(navList);
					navMap.put(listNav.get(0).getResource().getDisplayOrder(), nav);
				}
			});
			result.setData(navMap.values());
		} catch (Exception e) {
			log.error("Error in setUserPrivileges .... ::" + e);
			throw new CustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return result;
	}

}
