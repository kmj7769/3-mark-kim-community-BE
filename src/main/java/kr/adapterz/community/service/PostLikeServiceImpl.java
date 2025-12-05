package kr.adapterz.community.service;

import kr.adapterz.community.dto.postlike.PostLikeResponseDto;
import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.PostLike;
import kr.adapterz.community.entity.PostLikeAndCommentCount;
import kr.adapterz.community.entity.User;
import kr.adapterz.community.repository.PostLikeAndCommentCountRepository;
import kr.adapterz.community.repository.PostLikeRepository;
import kr.adapterz.community.repository.PostRepository;
import kr.adapterz.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeAndCommentCountRepository postLikeAndCommentCountRepository;

    private final PostLikeAndCommentCountService postLikeAndCommentCountService;

    @Transactional
    @Override
    public Optional<PostLikeResponseDto> createPostLike(Long userId, Long postId) {

        PostLike postLike = postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId).orElse(null);

        if (postLike == null) {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
            Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

            postLike = postLikeRepository.save(new PostLike(post, user));

            PostLikeAndCommentCount postLikeAndCommentCount = postLikeAndCommentCountRepository.findByPostId(postId).orElseGet(
                    () -> postLikeAndCommentCountService.createLikeAndCommentCount(post));
            postLikeAndCommentCount.setLikeCount(postLikeAndCommentCount.getLikeCount() + 1);

            return Optional.of(PostLikeResponseDto.builder()
                        .postLikeId(postLike.getId())
                        .likeCount(postLikeAndCommentCount.getLikeCount())
                    .build());
        }

        return Optional.empty();
    }

    @Transactional
    @Override
    public Optional<PostLikeResponseDto> deletePostLike(Long userId, Long postId) {

        PostLike postLike = postLikeRepository.findPostLikeByPostIdAndUserId(postId, userId).orElse(null);

        if (postLike != null) {
            Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

            Long postLikeId = postLike.getId();
            postLikeRepository.deleteById(postLike.getId());

            PostLikeAndCommentCount postLikeAndCommentCount = postLikeAndCommentCountRepository.findByPostId(postId).orElseGet(
                    () -> postLikeAndCommentCountService.createLikeAndCommentCount(post));
            postLikeAndCommentCount.setLikeCount(Math.max(postLikeAndCommentCount.getLikeCount() - 1, 0));

            return Optional.of(PostLikeResponseDto.builder()
                            .postLikeId(postLikeId)
                            .likeCount(postLikeAndCommentCount.getLikeCount())
                    .build());
        }

        return Optional.empty();
    }
}
