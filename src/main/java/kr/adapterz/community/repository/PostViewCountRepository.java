package kr.adapterz.community.repository;

import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.PostViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostViewCountRepository extends JpaRepository<PostViewCount, Long> {
    Optional<PostViewCount> findByPost(Post post);
}
