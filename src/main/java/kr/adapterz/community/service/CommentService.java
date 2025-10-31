package kr.adapterz.community.service;

import kr.adapterz.community.dto.comment.*;

import java.util.Optional;

public interface CommentService {
    /*
        댓글 생성 시 호출
        댓글 생성 요청 본문을 받아 Comment 엔티티를 생성
        생성된 엔티티를 DB에 저장하고 저장된 데이터 정보를 반환
     */
    AddCommentResponseDto addComment(AddCommentRequestDto addCommentRequestDto, Long userId, Long postId);

    /*
        댓글 수정 시 호출
        댓글 수정 요청 본문을 받아 Comment 엔티티의 content를 수정
        수정성된 엔티티를 영속화하고 수정된 데이터 정보를 반환
     */
    Optional<CommentUpdateResponseDto> updateComment(CommentUpdateRequestDto commentUpdateRequestDto, Long userId, Long postId, Long commentId);

    /*
        댓글 삭제 시 호출
        댓글 삭제 요청을 받아 해당 Comment 엔티티를 삭제
    */
    Optional<CommentDeleteResponseDto> deleteComment(Long userId, Long postId, Long commentId);

    /*
        댓글 목록 조회 시 호출
        lastFetchId와 limit를 받아 해당 조건에 맞는 댓글 목록을 반환
     */
    CommentListRetrieveResponseDto getCommentList(Long lastFetchId, Integer limit, Long userId, Long postId);
}
