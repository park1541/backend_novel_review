package com.example.backend_novel_review.user.mapper;

import com.example.backend_novel_review.user.dto.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findByProviderAndProviderId(@Param("provider") String provider,
                                               @Param("providerId") String providerId);
    Optional<User> findById(@Param("id") Long id);
    List<User> findAll();
    void save(User user);
    void update(User user);
    void deleteById(@Param("id") Long id);
}
