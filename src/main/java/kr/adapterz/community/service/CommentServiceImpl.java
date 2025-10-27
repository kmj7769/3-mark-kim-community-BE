package kr.adapterz.community.service;

import kr.adapterz.community.dto.comment.AddCommentRequestDto;
import kr.adapterz.community.dto.comment.AddCommentResponseDto;
import kr.adapterz.community.entity.Comment;
import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.User;
import kr.adapterz.community.repository.CommentRepository;
import kr.adapterz.community.repository.PostRepository;
import kr.adapterz.community.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,  UserRepository userRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public AddCommentResponseDto addComment(AddCommentRequestDto addCommentRequestDto, Long userId, Long postId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("post not found"));

        Comment comment = new Comment(
                addCommentRequestDto.getContent(),
                post,
                user
        );

        commentRepository.save(comment);

        return AddCommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .userNickname(user.getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
