package com.narangnorang.user.repository;

import com.narangnorang.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	UserRole findByName(String name);
}
