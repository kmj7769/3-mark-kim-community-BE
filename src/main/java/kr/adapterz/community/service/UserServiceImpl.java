package kr.adapterz.community.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.adapterz.community.dto.auth.UserPwdUpdateRequestDto;
import kr.adapterz.community.dto.auth.UserPwdUpdateResponseDto;
import kr.adapterz.community.dto.user.*;
import kr.adapterz.community.entity.User;
import kr.adapterz.community.entity.UserAuth;
import kr.adapterz.community.repository.UserAuthRepository;
import kr.adapterz.community.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @PersistenceContext
    private EntityManager em;

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           UserAuthRepository userAuthRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserSignUpResponseDto saveUser(UserSignUpRequestDto userSignUpRequestDto) {
        // User 엔티티 생성 및 영속화
        User user = new User(
                userSignUpRequestDto.getNickname(),
                userSignUpRequestDto.getProfileImage()
                );
        User savedUser = userRepository.save(user);

        // 비밀번호 Bcrypt 암호화
        String encodedPassword = passwordEncoder.encode(userSignUpRequestDto.getPassword());

        // UserAuth 엔티티 생성 및 영속화
        UserAuth userAuth = new UserAuth(
                user,
                userSignUpRequestDto.getEmail(),
                encodedPassword
        );
        UserAuth savedUserAuth = userAuthRepository.save(userAuth);

        // 응답 DTO에 담아서 반환
        return new UserSignUpResponseDto(
                savedUser.getId(),
                savedUserAuth.getEmail(),
                savedUser.getNickname(),
                savedUser.getCreatedAt()
        );
    }

    public UserInfoResponseDto getUserInfo(Long userId) { // 세션 정보를 가져오게 되면 userId 파라미터 불필요

        return userRepository.findUserInfoById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    @Transactional
    public UserInfoUpdateResponseDto updateUserInfo(UserInfoUpdateRequestDto userInfoUpdateRequestDto) {

        // 회원정보 수정
        User user = userRepository.findById(userInfoUpdateRequestDto.getUserId()).orElseThrow(() -> new IllegalArgumentException("user not found"));
        user.setNickname(userInfoUpdateRequestDto.getNickname());
        user.setProfileImage(userInfoUpdateRequestDto.getProfileImage());

        // 변경사항 커밋
        em.flush();

        // 수정된 회원정보 반환
        return new UserInfoUpdateResponseDto(user.getNickname(), user.getProfileImage(), user.getModifiedAt());
    }

    @Transactional
    public UserPwdUpdateResponseDto updateUserPwd(UserPwdUpdateRequestDto userPwdUpdateRequestDto) {

        // 비밀번호 수정
        UserAuth userAuth = userAuthRepository.findById(userPwdUpdateRequestDto.getUserId()).orElseThrow(() -> new IllegalArgumentException("user not found"));
        userAuth.setPassword(userPwdUpdateRequestDto.getPassword()); // Bcrypt 암호화 방법에 대해 학습하고 수정

        // 수정 시각 업데이트
        User user = userAuth.getUser();
        user.setModifiedAt(LocalDateTime.now());

        // 수정된 시각 반환
        return new UserPwdUpdateResponseDto(user.getModifiedAt());
    }


    // soft delete로 변경될 수도 있음
    @Transactional
    public void deleteUser(Long userId) { // 세션 정보를 가져오게 되면 userId 파라미터 불필요

        // 해당 유저의 회원정보 삭제
        userRepository.deleteById(userId);
    }
}
