package kr.adapterz.community.service;

import kr.adapterz.community.entity.Post;
import kr.adapterz.community.entity.PostViewCount;
import kr.adapterz.community.repository.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostViewCountServiceImpl implements PostViewCountService {
    private final PostViewCountRepository postViewCountRepository;

    @Override
    public PostViewCount createPostViewCount(Post post) {
        return postViewCountRepository.save(new PostViewCount(post));
    }

    @Override
    public PostViewCount incrementPostViewCount(Post post) {
        PostViewCount postViewCount = postViewCountRepository.findByPost(post).orElseGet(() -> createPostViewCount(post));

        postViewCount.setViewCount(postViewCount.getViewCount() + 1L);

        return postViewCount;
    }

    @Override
    public PostViewCount updatePostViewCount(Post post, Long amount) {
        PostViewCount postViewCount = postViewCountRepository.findByPost(post).orElseGet(() -> createPostViewCount(post));

        postViewCount.setViewCount(postViewCount.getViewCount() + amount);

        return postViewCount;
    }
}
