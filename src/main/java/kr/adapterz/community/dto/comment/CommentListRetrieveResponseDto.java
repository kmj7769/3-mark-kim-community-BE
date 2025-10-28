package kr.adapterz.community.dto.comment;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentListRetrieveResponseDto {
    private List<CommentOneInListDto> comments;
    private Long lastFetchId;
}
