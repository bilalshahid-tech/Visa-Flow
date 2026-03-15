package com.visaflow.auth.repository;

import com.visaflow.auth.model.LoginHistory;
import com.visaflow.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {
    List<LoginHistory> findByUserOrderByLoginAtDesc(User user);
}
