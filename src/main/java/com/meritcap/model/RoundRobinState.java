package com.meritcap.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "round_robin_state")
@Getter
@Setter
@NoArgsConstructor
public class RoundRobinState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "counselor_id", nullable = false, unique = true)
    private User counselor;

    @Column(name = "last_assigned_at")
    private LocalDateTime lastAssignedAt;

    @Column(name = "assignment_count", nullable = false)
    private int assignmentCount = 0;

    public RoundRobinState(User counselor) {
        this.counselor = counselor;
    }
}
