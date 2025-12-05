package kr.adapterz.community.repository;

import kr.adapterz.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findPostLikeByPostIdAndUserId(Long postId, Long userId);
}
