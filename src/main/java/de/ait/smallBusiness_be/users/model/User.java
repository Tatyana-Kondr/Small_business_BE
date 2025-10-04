package de.ait.smallBusiness_be.users.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * 1/27/2025
 * Small_business_BE
 *
 * @author Chechkina (AIT TR)
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;



}
