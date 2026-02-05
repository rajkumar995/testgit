package com.medidropbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AwsS3Config {

	@Value("${aws.s3.access-key:}")
	private String accessKey;

	@Value("${aws.s3.secret-key:}")
	private String secretKey;

	@Value("${aws.s3.region:ap-south-1}")
	private String region;

	@Value("${aws.s3.bucket-name:hospital}")
	private String bucketName;

	@Bean(destroyMethod = "close")
	public S3Client s3Client() {
		log.info("=== AWS S3 CONFIG CHECK ===");
		log.info("AWS_ACCESS_KEY_ID: {}", mask(accessKey));
		log.info("AWS_SECRET_ACCESS_KEY: {}", mask(secretKey));
		log.info("AWS_REGION: {}", region);
		log.info("AWS_S3_BUCKET: {}", bucketName);
		log.info("===========================");

		if (isBlank(accessKey) || isBlank(secretKey) || isBlank(region)) {
			throw new IllegalStateException(
					"AWS credentials or region not set. Please configure aws.s3.access-key, aws.s3.secret-key, and aws.s3.region in application.properties");
		}

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();
	}

	@Bean(destroyMethod = "close")
	public S3Presigner s3Presigner() {
		if (isBlank(accessKey) || isBlank(secretKey) || isBlank(region)) {
			throw new IllegalStateException(
					"AWS credentials or region not set. Please configure aws.s3.access-key, aws.s3.secret-key, and aws.s3.region in application.properties");
		}

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
		return S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();
	}

	public String getBucketName() {
		if (bucketName == null || bucketName.isBlank()) {
			throw new IllegalStateException("AWS S3 bucket name not set in application.properties");
		}
		return bucketName;
	}

	public String getRegion() {
		return region;
	}

	private static boolean isBlank(String s) {
		return s == null || s.isBlank();
	}

	private static String mask(String value) {
		if (value == null)
			return null;
		if (value.length() <= 4)
			return "****";
		return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
	}
}

