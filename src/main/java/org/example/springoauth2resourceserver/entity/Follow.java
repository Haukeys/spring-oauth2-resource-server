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
@Table(name = "user_follows", uniqueConstraints = {
        // Un utilisateur ne peut pas follower 2 fois la même personne
        @UniqueConstraint(columnNames = {"follower_id", "following_id"})})

@Audited
@AuditOverride(forClass = Auditable.class) // On hérite du suivi automatique (createdAt, createdBy, etc.)
public class Follow extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_follow")
    private UUID idFollow;

    // L'utilisateur qui suit (Abonné)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User_Profile follower;

    // L'utilisateur qui est suivi (Abonnement)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User_Profile following;
}