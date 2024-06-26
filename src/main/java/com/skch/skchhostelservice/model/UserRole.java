package com.skch.skchhostelservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_roles", schema = "hostel")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "users" })
public class UserRole extends Audit{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Long userRoleId;

    @OneToOne
	@JoinColumn(name = "user_id", nullable = true)
	private Users users;

    @OneToOne
	@JoinColumn(name = "role_id", nullable = true)
	private Roles roles;

    @Column(name = "is_active")
    private boolean isActive;

}
