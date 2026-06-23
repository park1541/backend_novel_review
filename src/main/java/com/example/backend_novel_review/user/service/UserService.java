package com.example.backend_novel_review.user.service;

import com.example.backend_novel_review.auth.service.SocialUnlinkService;
import com.example.backend_novel_review.review.mapper.ReviewMapper;
import com.example.backend_novel_review.user.dto.User;
import com.example.backend_novel_review.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;
    private final SocialUnlinkService socialUnlinkService;

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userMapper.findAll();
    }

    public User findUserOrThrow(Long userId) {
        return userMapper.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
    }

    public void deleteAccount(Long userId) {
        User user = findUserOrThrow(userId);
        socialUnlinkService.unlink(user.getProvider(), user.getProviderId(), user.getSocialAccessToken());
        userMapper.deleteById(user.getId());
    }

    public void banUser(Long id) {
        User user = findUserOrThrow(id);
        reviewMapper.deleteByUserId(user.getId());
        socialUnlinkService.unlink(user.getProvider(), user.getProviderId(), user.getSocialAccessToken());
        userMapper.deleteById(user.getId());
    }
}
