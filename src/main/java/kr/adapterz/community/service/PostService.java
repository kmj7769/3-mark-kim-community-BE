package kr.adapterz.community.service;

import kr.adapterz.community.dto.post.*;

public interface PostService {

    /*
        게시글 생성 시 호출
        게시글 생성 요청 본문을 받아 Post 엔티티를 생성
        생성된 엔티티를 DB에 저장하고 저장된 데이터 정보를 반환
    */
    PostUploadResponseDto savePost(PostUploadRequestDto postUploadRequestDto, Long userId);

    /*
        게시글 목록 조회 시 호출
        lastFetchId를 받아 해당 값보다 작은 id를 가진 게시글을 limit에 맞는 개수만큼 반환
     */
    PostListRetrieveResponseDto getPostList(Long lastFetchId, Integer limit);

    /*
        게시글 상세 조회 시 호출
        해당 게시글의 상세 정보를 DTO에 매핑해서 반환
     */
    PostDetailRetrieveResponseDto getPostDetail(Long postId, Long userId); // 세션 정보를 가져오게 되면 userId 불필요

    /*
        게시글 수정 시 호출
        해당 게시글을 수정
        수정된 게시글 정보(title, content, images, modifiedAt)를 DTO에 매핑해서 반환
     */
    PostUpdateResponseDto updatePost(Long postId, PostUpdateRequestDto postUpdateRequestDto);

    /*
        게시글 삭제 시 호출
        해당 게시글을 삭제
        게시글에 포함된 댓글, 사진, 좋아요, 집계 데이터 삭제
     */
    void deletePost(Long postId);
}
