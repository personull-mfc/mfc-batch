package com.mfc.batch.batch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class PostSummary {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, updatable = false)
	private Long postId;
	@Column
	private String partnerId;
	@Column(nullable = false)
	private Integer bookmarkCnt;

	@Builder
	public PostSummary(Long id, Long postId, String partnerId, Integer bookmarkCnt) {
		this.id = id;
		this.postId = postId;
		this.partnerId = partnerId;
		this.bookmarkCnt = bookmarkCnt;
	}
}
