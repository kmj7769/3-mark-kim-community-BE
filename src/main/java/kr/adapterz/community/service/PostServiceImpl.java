package kr.adapterz.community.service;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.adapterz.community.dto.post.*;
import kr.adapterz.community.entity.*;
import kr.adapterz.community.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @PersistenceContext
    private EntityManager em;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeAndCommentCountRepository postLikeAndCommentCountRepository;
    private final PostViewCountRepository postViewCountRepository;
    private final PostLikeRepository postLikeRepository;

    private final UserRepository userRepository;

    private final JPAQueryFactory jpaQueryFactory;

    private final PostViewCountService postViewCountService;

    @Autowired
    public PostServiceImpl(
            PostRepository postRepository,
            PostImageRepository postImageRepository,
            PostLikeAndCommentCountRepository postLikeAndCommentCountRepository,
            PostViewCountRepository postViewCountRepository,
            PostLikeRepository postLikeRepository,
            UserRepository userRepository,
            JPAQueryFactory jpaQueryFactory,
            PostViewCountService postViewCountService) {
        this.postRepository = postRepository;
        this.postImageRepository = postImageRepository;
        this.postLikeAndCommentCountRepository = postLikeAndCommentCountRepository;
        this.postViewCountRepository = postViewCountRepository;
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.jpaQueryFactory = jpaQueryFactory;
        this.postViewCountService = postViewCountService;
    }

    @Transactional
    public PostUploadResponseDto savePost(PostUploadRequestDto postUploadRequestDto) {
        // 해당 User 조회
        User user = userRepository.findById(postUploadRequestDto.getUserId()).orElseThrow(() -> new IllegalArgumentException("user not found"));

        // Post 엔티티 생성 및 영속화
        Post post = new Post(postUploadRequestDto.getTitle(), postUploadRequestDto.getContent(), user);
        postRepository.save(post);

        // 게시글에 포함된 이미지가 존재하는 경우 PostImage 엔티티 생성 및 영속화
        if (postUploadRequestDto.getImages() != null && !postUploadRequestDto.getImages().isEmpty()) {
            for (String url : postUploadRequestDto.getImages()) {
                PostImage postImage = new PostImage(url, post);
                postImageRepository.save(postImage);
            }
        }

        return new PostUploadResponseDto(post.getId(), post.getTitle(), post.getCreatedAt());
    }

    public PostListRetrieveResponseDto getPostList(Long lastFetchId, Integer limit) {
        // Q클래스
        QPost post = QPost.post;
        QUser user = QUser.user;
        QPostLikeAndCommentCount postLikeAndCommentCount = QPostLikeAndCommentCount.postLikeAndCommentCount;
        QPostViewCount postViewCount = QPostViewCount.postViewCount;

        // lastFetchId가 null인 경우 해당 조건을 제외
        BooleanExpression scrollingCondition = lastFetchId == null ? null : post.id.lt(lastFetchId);

        // 조인 후 조건에 맞는 게시글 정보 조회 및 DTO 매핑
        List<PostOneInListDto> posts =  jpaQueryFactory
                .select(Projections.constructor(
                        PostOneInListDto.class,
                        post.id,
                        user.nickname,
                        user.profileImage,
                        post.title,
                        postLikeAndCommentCount.likeCount.coalesce(0),
                        postLikeAndCommentCount.commentCount.coalesce(0),
                        postViewCount.viewCount.coalesce(0L),
                        post.createdAt
                ))
                .from(post)
                .leftJoin(post.user, user)
                .leftJoin(postLikeAndCommentCount).on(postLikeAndCommentCount.postId.eq(post.id))
                .leftJoin(postViewCount).on(postViewCount.postId.eq(post.id))
                .where(scrollingCondition)
                .orderBy(post.id.desc())
                .limit(limit)
                .fetch();

        Long nextLastFetchId = null;
        if (!posts.isEmpty()) {
            nextLastFetchId = posts.getLast().getPostId();
        }

        return PostListRetrieveResponseDto.builder().posts(posts).lastFetchId(nextLastFetchId).build();
    }

    @Transactional
    public PostDetailRetrieveResponseDto getPostDetail(Long postId, Long userId) {
        Post postEntity = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

        // Q클래스
        QPost post = QPost.post;
        QUser user = QUser.user;
        QPostImage postImage = QPostImage.postImage;
        QPostLike postLike = QPostLike.postLike;
        QPostLikeAndCommentCount postLikeAndCommentCount = QPostLikeAndCommentCount.postLikeAndCommentCount;
        QPostViewCount postViewCount = QPostViewCount.postViewCount;

        // 해당 게시글을 작성한 유저와 조회하는 유저가 동일한지 확인
        Expression<Boolean> permission = new CaseBuilder()
                .when(post.user.id.eq(userId))
                .then(true)
                .otherwise(false);

        // 해당 게시글을 조회하는 유저의 좋아요 여부 확인
        Expression<Boolean> liked = new CaseBuilder()
                .when(
                        JPAExpressions
                                .selectOne()
                                .from(postLike)
                                .where(
                                        postLike.post.id.eq(postId)
                                                .and(postLike.user.id.eq(userId))
                                )
                                .exists()
                )
                .then(true)
                .otherwise(false);

        // 조인 후 조건에 맞는 게시글 상세 정보 조회 및 DTO 매핑
        PostDetailRetrieveResponseDto postDetailRetrieveResponseDto = jpaQueryFactory
                .select(Projections.constructor(
                        PostDetailRetrieveResponseDto.class,
                        post.id,
                        user.nickname,
                        user.profileImage,
                        post.title,
                        post.content,
                        Expressions.constant(List.of()),
                        postLikeAndCommentCount.likeCount.coalesce(0),
                        postLikeAndCommentCount.commentCount.coalesce(0),
                        postViewCount.viewCount.coalesce(0L),
                        post.createdAt,
                        post.modifiedAt,
                        permission,
                        permission,
                        liked
                ))
                .from(post)
                .leftJoin(post.user, user)
                .leftJoin(postLikeAndCommentCount).on(postLikeAndCommentCount.postId.eq(post.id))
                .leftJoin(postViewCount).on(postViewCount.postId.eq(post.id))
                .leftJoin(postImage).on(postImage.post.eq(post))
                .where(post.id.eq(postId))
                .fetchOne();

        // postId에 해당하는 게시글이 없을 경우 예외 처리
        if (postDetailRetrieveResponseDto == null) {
            throw new IllegalArgumentException("post not found");
        }

        // 게시글에 포함된 이미지들 조회
        List<String> images = jpaQueryFactory
                .select(postImage.imagePath)
                .from(postImage)
                .where(postImage.post.id.eq(postId))
                .fetch();

        // DTO에 이미지 매핑
        postDetailRetrieveResponseDto.setImages(images);

        // 조회수 1만큼 증가
        postViewCountService.incrementPostViewCount(postEntity);

        return postDetailRetrieveResponseDto;
    }

    @Transactional
    public PostUpdateResponseDto updatePost(Long postId, PostUpdateRequestDto postUpdateRequestDto) {
        // 해당 Post 조회
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

        // title, content 수정
        post.setTitle(postUpdateRequestDto.getTitle());
        post.setContent(postUpdateRequestDto.getContent());

        // 삭제할 PostImage 삭제
        for (Long imageId : postUpdateRequestDto.getDeletedImages()) {
            postImageRepository.deleteById(imageId);
        }

        // 추가할 PostImage 생성
        for (String imagePath : postUpdateRequestDto.getAddedImages()) {
            PostImage postImage = new PostImage(imagePath, post);
            postImageRepository.save(postImage);
        }

        // 수정 후 해당 게시글의 이미지 목록 조회
        List<String> images = postImageRepository.findImagesByPostId(postId).orElse(List.of());

        // 변경 사항 커밋
        em.flush();

        return new PostUpdateResponseDto(
                new PostUpdateResponseDto.Data(
                        post.getTitle(),
                        post.getContent(),
                        images,
                        post.getModifiedAt()
                )
        );
    }

    @Transactional
    public void deletePost(Long postId) {
        // 해당 게시글 삭제
        postRepository.deleteById(postId);
    }
}
