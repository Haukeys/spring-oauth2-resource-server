package org.example.springoauth2resourceserver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Audited;
import org.hibernate.envers.AuditOverride;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name="User_Profile")
@Audited
@AuditOverride(forClass = Auditable.class)
public class User_Profile extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id_profile")
    private UUID idProfile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false, unique = true)
    private User userUuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(name="avatar_url")
    private String avatar_url;

    @Column(columnDefinition = "TEXT")//optimiser pour des biografie longue dans MySQL
    private String biography;
}
