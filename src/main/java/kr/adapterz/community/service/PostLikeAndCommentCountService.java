package kr.adapterz.community.service;

import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.PostLikeAndCommentCount;

public interface PostLikeAndCommentCountService {
    /*
        특정 게시글에 대한 좋아요나 댓글이 최초로 생성될 시 호출
        특정 게시글에 대한 좋아요, 댓글 집계 데이터 생성 및 영속화
     */
    PostLikeAndCommentCount createLikeAndCommentCount(Post post);
}
