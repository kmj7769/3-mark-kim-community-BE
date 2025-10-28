package kr.adapterz.community.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class PostDetailRetrieveResponseDto {
    private Long postId;
    private String userNickname;
    private String title;
    private String content;
    private List<String> images;
    private Integer likeCount;
    private Integer commentCount;
    private Long viewCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean liked;
}
