package com.web.fitquest.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Hidden
@Slf4j
@RestController
@RequestMapping("/api/article")
@Tag(name = "Article", description = "검색 API")
public class ArticleController {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Operation(summary = "유튜브 플레이리스트 비디오 검색", description = "유튜브 플레이리스트 비디오 검색 API를 통해 플레이리스트 비디오 검색을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플레이리스트 비디오 검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색어 인코딩 실패")
    })
    @GetMapping("/playlist/videos")
    public ResponseEntity<String> getPlaylistVideos(
            @RequestParam String playlistId,
            @RequestParam(required = false) String pageToken,
            @RequestParam(defaultValue = "50") int maxResults) {

        String apiURL = String.format(
                "https://www.googleapis.com/youtube/v3/playlistItems" +
                        "?part=snippet,contentDetails" +
                        "&playlistId=%s" +
                        "&maxResults=%d" +
                        "&key=%s",
                playlistId, maxResults, youtubeApiKey);

        if (pageToken != null && !pageToken.isEmpty()) {
            apiURL += "&pageToken=" + pageToken;
        }

        Map<String, String> requestHeaders = new HashMap<>();
        String responseBody = get(apiURL, requestHeaders);
        return ResponseEntity.ok(responseBody);
    }

    @Operation(summary = "네이버 블로그와 이미지 통합 검색", description = "네이버 블로그 검색과 이미지 검색을 한 번에 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통합 검색 성공"),
            @ApiResponse(responseCode = "400", description = "검색어 인코딩 실패")
    })
    @GetMapping("/search/blog-with-images")
    public ResponseEntity<String> searchBlogWithImages(
            @RequestParam String query,
            @RequestParam int start,
            @RequestParam int display) {
        try {
            // 1. 블로그 검색
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String blogApiURL = "https://openapi.naver.com/v1/search/blog?query=" + encodedQuery
                    + "&start=" + start
                    + "&display=" + display
                    + "&sort=sim"
                    + "&filter=all";

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("X-Naver-Client-Id", clientId);
            requestHeaders.put("X-Naver-Client-Secret", clientSecret);

            // 2. 블로그 검색 결과 가져오기
            String blogResponse = get(blogApiURL, requestHeaders);
            
            // 3. 블로그 응답을 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode blogJson = mapper.readTree(blogResponse);
            
            // 4. 블로그 아이템들을 10개씩 배치로 나누어 처리
            JsonNode items = blogJson.get("items");
            List<JsonNode> itemList = new ArrayList<>();
            items.forEach(itemList::add);
            
            List<List<JsonNode>> batches = new ArrayList<>();
            for (int i = 0; i < itemList.size(); i += 10) {
                batches.add(itemList.subList(i, Math.min(i + 10, itemList.size())));
            }

            // 5. 각 배치를 병렬로 처리
            // n개씩 담긴 m개 배치마다 실행될 작업 정의
            // supplyAsync로 각 batch마다 실행될 작업을 대기방에 등록해놓음
            List<CompletableFuture<List<JsonNode>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> {
                    List<JsonNode> batchResults = new ArrayList<>();
                    for (JsonNode item : batch) {
                        try {
                            String title = item.get("title").asText()
                                .replaceAll("<[^>]*>", "") // HTML 태그 제거
                                .replaceAll("&[^;]+;", ""); // HTML 엔티티 제거
                            
                            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());
                            String imageApiURL = "https://openapi.naver.com/v1/search/image?query=" 
                                    + encodedTitle
                                    + "&display=1";
                            
                            String imageResponse = get(imageApiURL, requestHeaders);
                            JsonNode imageJson = mapper.readTree(imageResponse);
                            
                            if (imageJson.has("items") && imageJson.get("items").size() > 0) {
                                batchResults.add(imageJson.get("items").get(0));
                            }
                            
                            Thread.sleep(10); // 배치 내 요청 간격
                        } catch (Exception e) {
                            log.error("이미지 검색 실패: {}", e.getMessage());
                        }
                    }
                    return batchResults;
                }))
                .collect(Collectors.toList());

            // 6. 모든 배치 결과 실제 실행.(m개의 배치가 m개의 스레드로 동시에 실행)
            List<JsonNode> imageResults = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

            // 7. 결과 조합
            ObjectNode result = mapper.createObjectNode();
            result.set("blog", blogJson);
            ArrayNode imagesArray = result.putArray("images");
            imageResults.forEach(imagesArray::add);

            return ResponseEntity.ok(mapper.writeValueAsString(result));

        } catch (Exception e) {
            throw new RuntimeException("통합 검색 실패", e);
        }
    }

    private String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Accept-Charset", "UTF-8");
            return con;
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private String readBody(InputStream body) {
        try (BufferedReader lineReader = new BufferedReader(
                new InputStreamReader(body, StandardCharsets.UTF_8))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
        }
    }
}