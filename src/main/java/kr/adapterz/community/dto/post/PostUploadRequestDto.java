package kr.adapterz.community.dto.post;

import lombok.Getter;

import java.util.List;

@Getter
public class PostUploadRequestDto {
    private String title;
    private String content;
    private List<String> images;
}
