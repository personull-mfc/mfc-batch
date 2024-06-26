package com.mfc.batch.batch.application;

import static com.mfc.batch.common.response.BaseResponseStatus.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mfc.batch.batch.domain.PartnerRanking;
import com.mfc.batch.batch.domain.PartnerSummary;
import com.mfc.batch.batch.domain.PostSummary;
import com.mfc.batch.batch.dto.resp.PartnerRankingRespDto;
import com.mfc.batch.batch.dto.resp.PartnerSummaryRespDto;
import com.mfc.batch.batch.dto.resp.PostListRespDto;
import com.mfc.batch.batch.dto.resp.PostSummaryRespDto;
import com.mfc.batch.batch.infrastructure.PartnerRankingRepository;
import com.mfc.batch.batch.infrastructure.PartnerSummaryRepository;
import com.mfc.batch.batch.infrastructure.PostSummaryRepository;
import com.mfc.batch.common.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BatchServiceImpl implements BatchService {
	private final PostSummaryRepository postSummaryRepository;
	private final PartnerSummaryRepository partnerSummaryRepository;
	private final PartnerRankingRepository partnerRankingRepository;

	@Override
	public PostSummaryRespDto getBookmarkCnt(Long postId) {
		PostSummary summary = postSummaryRepository.findByPostId(postId)
				.orElseThrow(() -> new BaseException(POST_NOT_FOUND));

		return PostSummaryRespDto.builder()
				.postId(postId)
				.bookmarkCnt(summary.getBookmarkCnt())
				.build();
	}

	@Override
	public PartnerRankingRespDto getPartnerRanking() {
		List<PartnerRanking> ranking = partnerRankingRepository.findOrderByRanking();

		return PartnerRankingRespDto.builder()
				.partners(ranking.stream()
						.map(PartnerSummaryRespDto::new)
						.toList())
				.build();
	}

	@Override
	public PartnerSummaryRespDto getPartnerSummary(String uuid) {
		PartnerSummary summary = partnerSummaryRepository.findByPartnerId(uuid)
				.orElseThrow(() -> new BaseException(PARTNER_NOT_FOUND));

		return PartnerSummaryRespDto.builder()
				.partnerId(uuid)
				.coordinateCnt(summary.getCoordinateCnt())
				.followerCnt(summary.getFollowerCnt())
				.averageStar(summary.getAverageStar())
				.build();
	}

	@Override
	public PostListRespDto getPostList(Pageable page, List<String> partners) {
		Slice<Long> posts = postSummaryRepository.getPostList(page, partners);
		return PostListRespDto.builder()
				.posts(posts.getContent())
				.isLast(posts.isLast())
				.build();
	}
}
