package kr.adapterz.community.dto.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentDeleteResponseDto {
    Long deleteCommentId;
}
