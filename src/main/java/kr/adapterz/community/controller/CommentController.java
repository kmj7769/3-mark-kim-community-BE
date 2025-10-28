package kr.adapterz.community.controller;

import kr.adapterz.community.dto.ApiResponseDto;
import kr.adapterz.community.dto.comment.AddCommentRequestDto;
import kr.adapterz.community.dto.comment.AddCommentResponseDto;
import kr.adapterz.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    // 댓글 추가 작업을 처리하는 메서드
    @PostMapping
    public ResponseEntity<ApiResponseDto<AddCommentResponseDto>> addComment(
            @RequestHeader Long userId,
            @RequestBody AddCommentRequestDto addCommentRequestDto,
            @PathVariable Long postId) {
        AddCommentResponseDto addCommentResponseDto = commentService.addComment(addCommentRequestDto, userId, postId);
        ApiResponseDto<AddCommentResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.CREATED.value(),
                "Add comment successfully",
                "/posts/" + postId + "/comments",
                addCommentResponseDto
        );

        return ResponseEntity.created(URI.create("/posts/" + postId + "/comments")).body(apiResponseDto);
    }
}
