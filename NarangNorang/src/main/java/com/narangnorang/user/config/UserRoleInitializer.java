package com.narangnorang.user.config;

import com.narangnorang.user.entity.UserRole;
import com.narangnorang.user.repository.UserRoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRoleInitializer implements ApplicationRunner{

	private final UserRoleRepository userRoleRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		createRoleIfAbsent("NORMAL");
		createRoleIfAbsent("ADMIN");
	}

	private void createRoleIfAbsent(String name) {
		if(userRoleRepository.findByName(name) == null) {
			UserRole role = new UserRole();
			role.setName(name);
			userRoleRepository.save(role);
		}
	}

}
