package kr.adapterz.community.dto.postlike;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostLikeResponseDto {
    private Long postLikeId;
    private Integer likeCount;
}
