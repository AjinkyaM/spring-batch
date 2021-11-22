package org.demo.batch.model;

import lombok.Data;

@Data
public class TweetDto {
	
	private Long createdAt;
	private Long id;
	private String text;
	private Integer retweetCount;
	private String lang;
	
}
