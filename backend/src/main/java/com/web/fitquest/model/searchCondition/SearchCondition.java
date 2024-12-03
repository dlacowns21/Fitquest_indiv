package com.web.fitquest.model.searchCondition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "SearchCondition DTO")
public class SearchCondition {
	private String tag;
	private String key;
	private String word;
	private String orderByDir;
	private String orderBy;
	@JsonProperty("isChoseong")
	private boolean isChoseong;
}
