package com.example.backend_novel_review.user.repository;

import com.example.backend_novel_review.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import java.util.Optional;

@Mapper
public interface UserRepository {
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider,
                                               @Param("providerId") String providerId);
    Optional<User> findById(@Param("id") Long id);
    List<User> findAll();
    void save(User user);
    void update(User user);
    void deleteById(@Param("id") Long id);
}
