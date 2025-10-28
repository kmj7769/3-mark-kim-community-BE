package kr.adapterz.community.service;

import kr.adapterz.community.dto.comment.AddCommentRequestDto;
import kr.adapterz.community.dto.comment.AddCommentResponseDto;
import kr.adapterz.community.dto.comment.CommentListRetrieveResponseDto;

public interface CommentService {
    /*
        댓글 생성 시 호출
        댓글 생성 요청 본문을 받아 Comment 엔티티를 생성
        생성된 엔티티를 DB에 저장하고 저장된 데이터 정보를 반환
     */
    AddCommentResponseDto addComment(AddCommentRequestDto addCommentRequestDto, Long userId, Long postId);

    /*
        댓글 목록 조회 시 호출
        lastFetchId와 limit를 받아 해당 조건에 맞는 댓글 목록을 반환
     */
    CommentListRetrieveResponseDto getCommentList(Long lastFetchId, Integer limit, Long userId, Long postId);
}
