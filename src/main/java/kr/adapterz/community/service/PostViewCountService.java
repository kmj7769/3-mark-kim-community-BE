package kr.adapterz.community.service;

import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.PostViewCount;

public interface PostViewCountService {
    /*
        게시글 생성 시 호출
        조회수 데이터 생성 후 영속화
     */
    PostViewCount createPostViewCount(Post post);

    /*
        게시글 상세 정보 조회 시 호출
        해당 게시글의 조회수를 1만큼 증가
        변경된 조회수 데이터를 영속화
     */
    PostViewCount incrementPostViewCount(Post post);

    /*
        해당 게시글의 조회수를 한 번에 업데이트해야 할 때 호출
        해당 게시글의 조회수를 amount만큼 증가
        변경된 조회수 데이터를 영속화
     */
    PostViewCount updatePostViewCount(Post post, Long amount);
}
