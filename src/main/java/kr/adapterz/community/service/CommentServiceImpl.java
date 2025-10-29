package kr.adapterz.community.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.adapterz.community.dto.comment.AddCommentRequestDto;
import kr.adapterz.community.dto.comment.AddCommentResponseDto;
import kr.adapterz.community.dto.comment.CommentListRetrieveResponseDto;
import kr.adapterz.community.dto.comment.CommentOneInListDto;
import kr.adapterz.community.entity.*;
import kr.adapterz.community.repository.CommentRepository;
import kr.adapterz.community.repository.PostRepository;
import kr.adapterz.community.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public CommentServiceImpl(
            CommentRepository commentRepository,
            UserRepository userRepository,
            PostRepository postRepository,
            JPAQueryFactory jpaQueryFactory) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.jpaQueryFactory = jpaQueryFactory;
    }

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

        // 응답 dto 매핑
        return AddCommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .userNickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .modifiedAt(comment.getCreatedAt())
                .build();
    }

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

        return CommentListRetrieveResponseDto.builder().comments(comments).lastFetchId(nextLastFetchId).build();
    }
}
