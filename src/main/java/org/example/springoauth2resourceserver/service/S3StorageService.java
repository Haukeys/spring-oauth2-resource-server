package org.example.springoauth2resourceserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3AsyncClient s3AsyncClient;
    private final S3Presigner s3Presigner;
    private final SqsAsyncClient sqsAsyncClient;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.upload-folder}")
    private String uploadFolder;

    @Value("${aws.sqs.queue-url}")
    private String queueUrl;

    // Changement : On attend 'nickname' au lieu de 'userSub'
    public String uploadOriginalAvatar(String nickname, MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();

        // Le path S3 utilisera directement le nickname : upload-folder/nickname/original_timestamp_filename
        String s3Key = String.format("%s/%s/original_%d_%s",
                uploadFolder, nickname, System.currentTimeMillis(), originalFilename);

        log.info("[S3 Upload] Upload of the original file on S3 : {}", s3Key);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();

        s3AsyncClient.putObject(
                putObjectRequest,
                AsyncRequestBody.fromBytes(file.getBytes())
        ).join();

        return s3Key;
    }
    private void sendSqsNotification(String nickname, String s3Key) {
        String jsonPayload = String.format("{\"nickname\":\"%s\", \"s3Key\":\"%s\"}", nickname, s3Key);

        SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(jsonPayload)
                .build();

        sqsAsyncClient.sendMessage(sendMsgRequest)
                .thenAccept(response ->
                        log.info("[SQS SUCCESS] MESSAGE SEND IN SQS QUEUE ! ID: {}", response.messageId())
                )
                .exceptionally(ex -> {
                    log.error("[SQS ERROR] ERROR WHILE SENDING SQS MESSAGE", ex);
                    return null;
                });
    }
    public String generatePresignedUrl(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return null;
        }

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(b -> b.bucket(bucketName).key(s3Key))
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }
}