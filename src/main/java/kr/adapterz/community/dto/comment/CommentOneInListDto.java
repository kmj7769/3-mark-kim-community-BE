package kr.adapterz.community.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentOneInListDto {
    private Long commentId;
    private String userNickname;
    private String profileImage;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder.Default
    private Boolean canEdit = false;

    @Builder.Default
    private Boolean canDelete = false;
}
