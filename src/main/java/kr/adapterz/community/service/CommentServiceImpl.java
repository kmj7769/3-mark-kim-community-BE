package kr.adapterz.community.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.adapterz.community.dto.comment.*;
import kr.adapterz.community.entity.*;
import kr.adapterz.community.repository.CommentRepository;
import kr.adapterz.community.repository.PostLikeAndCommentCountRepository;
import kr.adapterz.community.repository.PostRepository;
import kr.adapterz.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeAndCommentCountRepository postLikeAndCommentCountRepository;

    private final PostLikeAndCommentCountService postLikeAndCommentCountService;

    private final JPAQueryFactory jpaQueryFactory;

    @Transactional
    public AddCommentResponseDto addComment(AddCommentRequestDto addCommentRequestDto, Long userId, Long postId) {
        // 해당 User, 대상 Post 조회
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

        // 새로운 Comment 생성
        Comment comment = new Comment(
                addCommentRequestDto.getContent(),
                post,
                user
        );

        // 새로운 Comment 영속화
        commentRepository.save(comment);

        // 댓글 수 업데이트
        // 집계 데이터가 없다면 먼저 생성
        PostLikeAndCommentCount postLikeAndCommentCount = postLikeAndCommentCountRepository.findByPostId(post.getId())
                .orElseGet(() -> postLikeAndCommentCountService.createLikeAndCommentCount(post));
        postLikeAndCommentCount.setCommentCount(postLikeAndCommentCount.getCommentCount() + 1);

        // 응답 dto 매핑
        return AddCommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .userNickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .modifiedAt(comment.getCreatedAt())
                .build();
    }

    @Transactional
    public Optional<CommentUpdateResponseDto> updateComment(CommentUpdateRequestDto commentUpdateRequestDto, Long userId, Long postId, Long commentId) {
        // 해당 댓글 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("comment not found"));

        // 권한 확인
        // 실패 시 빈 Optional 반환
        if (!comment.getUser().getId().equals(userId) || !comment.getPost().getId().equals(postId)) {
            return Optional.empty();
        }

        // 댓글 수정 및 영속화
        comment.setContent(commentUpdateRequestDto.getContent());

        // dto 매핑
        CommentUpdateResponseDto commentUpdateResponseDto = CommentUpdateResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .userNickname(comment.getUser().getNickname())
                .profileImage(comment.getUser().getProfileImage())
                .modifiedAt(comment.getModifiedAt())
                .build();

        return Optional.of(commentUpdateResponseDto);
    }

    @Transactional
    public Optional<CommentDeleteResponseDto> deleteComment(Long userId, Long postId, Long commentId) {
        // 해당 댓글 조회
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("comment not found"));

        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

        // 권한 확인
        // 실패 시 빈 Optional 반환
        if (!comment.getUser().getId().equals(userId) || !comment.getPost().getId().equals(postId)) {
            return Optional.empty();
        }

        // 댓글 삭제 및 영속화
        commentRepository.delete(comment);

        // 댓글 수 업데이트
        // 집계 데이터가 없다면 먼저 생성
        PostLikeAndCommentCount postLikeAndCommentCount = postLikeAndCommentCountRepository.findByPostId(post.getId())
                .orElseGet(() -> postLikeAndCommentCountService.createLikeAndCommentCount(post));
        postLikeAndCommentCount.setCommentCount(postLikeAndCommentCount.getCommentCount() == 0 ? 0 : postLikeAndCommentCount.getCommentCount() - 1);

        // dto 매핑
        CommentDeleteResponseDto commentDeleteResponseDto = CommentDeleteResponseDto.builder()
                .deleteCommentId(commentId)
                .build();

        return Optional.of(commentDeleteResponseDto);
    }

    @Transactional(readOnly = true)
    public CommentListRetrieveResponseDto getCommentList(Long lastFetchId, Integer limit, Long userId, Long postId) {
        // Q클래스
        QPost post = QPost.post;
        QUser user = QUser.user;
        QComment comment = QComment.comment;

        // lastFetchId가 null일 경우 쿼리 조건에서 제외
        BooleanExpression scrollingCondition = lastFetchId == null ? null : comment.id.lt(lastFetchId);

        // 해당 댓글을 작성한 유저와 조회하는 유저가 동일한지 확인
        Expression<Boolean> permission = new CaseBuilder()
                .when(comment.user.id.eq(userId))
                .then(true)
                .otherwise(false);

        // 조건에 맞는 댓글 목록 조회 및 dto 매핑
        List<CommentOneInListDto> comments =  jpaQueryFactory
                .select(
                        Projections.constructor(
                                CommentOneInListDto.class,
                                comment.id,
                                user.nickname,
                                user.profileImage,
                                comment.content,
                                comment.modifiedAt,
                                permission,
                                permission
                        )
                )
                .from(comment)
                .leftJoin(comment.post, post)
                .leftJoin(comment.user, user)
                .where(
                        comment.post.id.eq(postId)
                                .and(scrollingCondition)
                )
                .orderBy(comment.id.desc())
                .limit(limit)
                .fetch();

        Long nextLastFetchId = null;
        if (!comments.isEmpty()) {
            nextLastFetchId = comments.getLast().getCommentId();
        }

        PostLikeAndCommentCount postLikeAndCommentCount = postLikeAndCommentCountRepository.findByPostId(postId).orElse(null);

        return CommentListRetrieveResponseDto.builder()
                .comments(comments)
                .lastFetchId(nextLastFetchId)
                .commentCount(postLikeAndCommentCount == null ? 0 : postLikeAndCommentCount.getCommentCount())
                .build();
    }
}
