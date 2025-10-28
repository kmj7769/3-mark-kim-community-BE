package kr.adapterz.community.dto.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PostOneInListDto {
    private Long postId;
    private String userNickname;
    private String profileImage;
    private String title;
    private Integer likeCount;
    private Integer commentCount;
    private Long viewCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
