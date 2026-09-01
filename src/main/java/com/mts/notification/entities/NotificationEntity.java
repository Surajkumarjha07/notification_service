package com.mts.notification.entities;

import com.mts.notification.enums.NotificationChannel;
import com.mts.notification.enums.NotificationStatus;
import com.mts.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @Column(name = "\"notificationId\"")
    private String notificationId;

    @Column(name = "sender", nullable = false)
    private String sender;

//    @ElementCollection
//    @CollectionTable(
//            name = "notification_recipients",
//            joinColumns = @JoinColumn(name = "\"notificationId\"")
//    )
    @Column(name = "\"recipientId\"", nullable = false)
    private String recipientId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "channel", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "\"updatedAt\"")
    private Instant updatedAt;
}
