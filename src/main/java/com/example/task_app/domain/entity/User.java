package com.example.task_app.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.task_app.domain.enums.UserRole;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    // パスワードはSpring SecurityのBCryptでハッシュ化した値が入る
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)  // DBには "USER" or "ADMIN" という文字列で保存
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, updatable = false)  // updatable=false = 更新不可
    private LocalDateTime createdAt;

    // DB保存直前に自動実行 → 作成日時を自動セット
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
