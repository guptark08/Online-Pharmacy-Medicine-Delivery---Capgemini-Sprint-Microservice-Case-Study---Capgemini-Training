package org.sprint.authService.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@ToString(exclude = "addresses")
@EqualsAndHashCode(exclude = "addresses")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(unique = true, nullable = false, length = 180)
    private String email;

    @Column(unique = true, nullable = false, length = 60)
    private String username;

    @Column(unique = true, length = 20)
    private String mobile;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String role = "CUSTOMER";

    @Builder.Default
    @Column(nullable = false)
    private boolean status = true;

    @Builder.Default
    @Column(name = "email_verified", nullable = true)
    private Boolean emailVerified = false;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();
}
