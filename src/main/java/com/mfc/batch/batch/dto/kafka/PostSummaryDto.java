package com.mfc.batch.batch.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryDto {
	private Long postId;
	private String partnerId;
}
