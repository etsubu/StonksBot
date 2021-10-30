package com.etsubu.stonksbot.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class S3Config {
    private String s3bucket;
    private String region;
}
