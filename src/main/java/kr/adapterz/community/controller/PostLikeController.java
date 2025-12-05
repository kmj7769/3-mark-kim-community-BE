package kr.adapterz.community.controller;

import jakarta.servlet.http.HttpServletRequest;
import kr.adapterz.community.dto.ApiResponseDto;
import kr.adapterz.community.dto.postlike.PostLikeResponseDto;
import kr.adapterz.community.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/likes")
public class PostLikeController {
    private final PostLikeService postLikeService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<PostLikeResponseDto>> createPostLike(
            HttpServletRequest request,
            @PathVariable Long postId) {

        Optional<PostLikeResponseDto> postLikeResponseDtoOpt = postLikeService.createPostLike((Long) request.getAttribute("userId"), postId);

        if (postLikeResponseDtoOpt.isEmpty()) {
            ApiResponseDto<PostLikeResponseDto> apiResponseDto = new ApiResponseDto<>(
                    HttpStatus.CONFLICT.value(),
                    "This user have already clicked like on this post",
                    null,
                    null
            );

            return new ResponseEntity<>(apiResponseDto, HttpStatus.CONFLICT);
        }

        ApiResponseDto<PostLikeResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.CREATED.value(),
                "Like count was incremented successfully.",
                "/posts/" + postId + "/likes",
                postLikeResponseDtoOpt.get()
        );

        return ResponseEntity.created(URI.create("/posts/" + postId + "/likes")).body(apiResponseDto);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponseDto<PostLikeResponseDto>> deletePostLike(
            HttpServletRequest request,
            @PathVariable Long postId
    ) {
        Optional<PostLikeResponseDto> postLikeResponseDtoOpt = postLikeService.deletePostLike((Long) request.getAttribute("userId"), postId);

        if (postLikeResponseDtoOpt.isEmpty()) {
            ApiResponseDto<PostLikeResponseDto> apiResponseDto = new ApiResponseDto<>(
                    HttpStatus.CONFLICT.value(),
                    "This user have not clicked like on this post",
                    null,
                    null
            );

            return new ResponseEntity<>(apiResponseDto, HttpStatus.CONFLICT);
        }

        ApiResponseDto<PostLikeResponseDto> apiResponseDto = new ApiResponseDto<>(
                HttpStatus.OK.value(),
                "Like count was decremented successfully.",
                null,
                postLikeResponseDtoOpt.get()
        );

        return ResponseEntity.ok(apiResponseDto);
    }
}
