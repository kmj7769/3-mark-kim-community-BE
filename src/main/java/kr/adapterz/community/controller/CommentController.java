package kr.adapterz.community.controller;

import kr.adapterz.community.dto.ApiResponseDto;
import kr.adapterz.community.dto.comment.*;
import kr.adapterz.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

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

    // 댓글 수정 작업을 처리하는 메서드
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentUpdateResponseDto>> updateComment(
            @RequestHeader Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        Optional<CommentUpdateResponseDto> commentUpdateResponseDtoOpt = commentService.updateComment(commentUpdateRequestDto, userId, postId, commentId);

        if (commentUpdateResponseDtoOpt.isEmpty()) {
            ApiResponseDto<CommentUpdateResponseDto> apiResponseDto = new ApiResponseDto<>(
                    HttpStatus.FORBIDDEN.value(),
                    "There's no permission to edit this comment.",
                    null,
                    null
            );

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponseDto);
        }

        ApiResponseDto<CommentUpdateResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Comment was successfully edited.",
                null,
                commentUpdateResponseDtoOpt.get()
        );

        return ResponseEntity.ok(apiResponseDto);
    }

    // 댓글 삭제 작업을 처리하는 메서드
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentDeleteResponseDto>> deleteComment(
            @RequestHeader Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        Optional<CommentDeleteResponseDto> commentDeleteResponseDtoOpt = commentService.deleteComment(userId, postId, commentId);

        if (commentDeleteResponseDtoOpt.isEmpty()) {
            ApiResponseDto<CommentDeleteResponseDto> apiResponseDto = new ApiResponseDto<>(
                    HttpStatus.FORBIDDEN.value(),
                    "There's no permission to delete this comment.",
                    null,
                    null
            );

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponseDto);
        }

        ApiResponseDto<CommentDeleteResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Comment was deleted successfully.",
                null,
                commentDeleteResponseDtoOpt.get()
        );

        return ResponseEntity.ok(apiResponseDto);
    }

    // 댓글 목록 조회 작업을 처리하는 메서드
    @GetMapping
    public ResponseEntity<ApiResponseDto<CommentListRetrieveResponseDto>> retrieveCommentList(
            @RequestHeader Long userId,
            @PathVariable Long postId,
            @RequestParam(required = false) Long lastFetchId,
            @RequestParam Integer limit
    ) {
        CommentListRetrieveResponseDto commentListRetrieveResponseDto = commentService.getCommentList(lastFetchId, limit, userId, postId);

        ApiResponseDto<CommentListRetrieveResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Comment list retrieved successfully.",
                null,
                commentListRetrieveResponseDto
        );

        return ResponseEntity.ok(apiResponseDto);
    }
}
