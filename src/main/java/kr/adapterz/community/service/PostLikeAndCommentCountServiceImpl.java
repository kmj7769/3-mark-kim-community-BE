package kr.adapterz.community.service;

import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.PostLikeAndCommentCount;
import kr.adapterz.community.repository.PostLikeAndCommentCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeAndCommentCountServiceImpl implements PostLikeAndCommentCountService {

    private final PostLikeAndCommentCountRepository postLikeAndCommentCountRepository;

    @Transactional
    public PostLikeAndCommentCount createLikeAndCommentCount(Post post) {
        return postLikeAndCommentCountRepository.save(new PostLikeAndCommentCount(post));
    }
}
