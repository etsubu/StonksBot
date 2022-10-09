package com.etsubu.stonksbot.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class S3Config {
    private String s3bucket;
    private String file;
    private String region;
    private Integer refreshTTL;
}
