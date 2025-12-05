package kr.adapterz.community.service;

import kr.adapterz.community.dto.postlike.PostLikeResponseDto;

import java.util.Optional;

public interface PostLikeService {
    /*
        좋아요 생성 시 호출
        해당 게시글에 대한 해당 유저의 좋아요 기록 생성
        생성한 좋아요 id와 업데이트 된 좋아요 수를 dto에 매핑해 반환
        이미 해당 게시글에 대한 해당 유저의 좋아요 기록이 있을 경우 빈 Optional 반환
     */
    Optional<PostLikeResponseDto> createPostLike(Long userId, Long postId);

    /*
        좋아요 취소 시 호출
        해당 게시글에 대한 해당 유저의 좋아요 기록 삭제
        생성한 좋아요 id와 업데이트 된 좋아요 수를 dto에 매핑해 반환
        해당 게시글에 대한 해당 유저의 좋아요 기록이 없을 경우 빈 Optional 반환
     */
    Optional<PostLikeResponseDto> deletePostLike(Long userId, Long postId);
}
