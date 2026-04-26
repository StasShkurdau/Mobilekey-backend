package com.mobilekey.backend.file.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(private val minioProperties: MinioProperties) {

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(minioProperties.endpoint)
            .credentials(minioProperties.accessKey, minioProperties.secretKey)
            .build()
    }

    @Bean
    fun minioInitializer(minioClient: MinioClient) = CommandLineRunner {
        val bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(minioProperties.bucket).build()
        )
        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(minioProperties.bucket).build()
            )
        }
    }
}
