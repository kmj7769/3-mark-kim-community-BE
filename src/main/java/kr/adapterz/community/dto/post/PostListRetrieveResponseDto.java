package kr.adapterz.community.dto.post;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostListRetrieveResponseDto {
    private List<PostOneInListDto> posts;
    private Long lastFetchId;
}
