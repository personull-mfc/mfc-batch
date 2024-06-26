package com.mfc.batch.batch.application;

import static com.mfc.batch.common.response.BaseResponseStatus.*;

import java.util.HashSet;
import java.util.Set;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mfc.batch.batch.domain.PartnerSummary;
import com.mfc.batch.batch.domain.PostSummary;
import com.mfc.batch.batch.infrastructure.PartnerSummaryRepository;
import com.mfc.batch.batch.infrastructure.PostSummaryRepository;
import com.mfc.batch.common.exception.BaseException;
import com.mfc.batch.common.response.BaseResponseStatus;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Transactional
public class PostJobConfig {
	private final RedisTemplate<String, Object> redisTemplate;
	private final PostSummaryRepository postSummaryRepository;

	@Bean(name = "postSummaryJob")
	public Job postSummaryJob(JobRepository jobRepository, PlatformTransactionManager txm) {
		return new JobBuilder("postSummaryJob", jobRepository)
				.start(postSummaryStep(jobRepository, txm))
				.build();
	}

	@Bean(name = "postSummaryStep")
	public Step postSummaryStep(JobRepository jobRepository, PlatformTransactionManager txm) {
		return new StepBuilder("postSummaryStep", jobRepository)
				.tasklet(postSummaryTasklet(), txm)
				.allowStartIfComplete(true)
				.build();
	}

	@Bean(name = "postSummaryTasklet")
	public Tasklet postSummaryTasklet() {
		return (contribution, chunkContext) -> {
			Set<String> keys = scanKeys("post:like:*");

			for (String key : keys) {
				Long postId = Long.valueOf(key.replace("post:like:", ""));
				Integer bookmarkCnt = Integer.valueOf(String.valueOf(redisTemplate.opsForValue().get(key)));

				PostSummary summary = postSummaryRepository.findByPostId(postId)
						.orElseThrow(() -> new BaseException(BaseResponseStatus.POST_NOT_FOUND));

				postSummaryRepository.save(PostSummary.builder()
						.id(summary.getId())
						.postId(postId)
						.bookmarkCnt(summary.getBookmarkCnt() + bookmarkCnt)
						.build());

				redisTemplate.delete(key);
			}
			return RepeatStatus.FINISHED;
		};
	}

	private Set<String> scanKeys(String pattern) {
		Set<String> keys = new HashSet<>();
		ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();

		try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
				.getConnection()
				.scan(options)) {

			while (cursor.hasNext()) {
				keys.add(new String(cursor.next()));
			}
		} catch (Exception e) {
			// 예외 처리
			throw new RuntimeException("Failed to scan Redis keys", e);
		}
		return keys;
	}
}
