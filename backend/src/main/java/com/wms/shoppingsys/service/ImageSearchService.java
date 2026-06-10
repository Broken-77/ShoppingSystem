package com.wms.shoppingsys.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ImageSearchService {

    private static final String BAIDU_IMAGE_URL = "https://image.baidu.com/search/index?tn=baiduimage&word=";
    private static final Random random = new Random();
    
    public String searchImage(String keyword) {
        try {
            Thread.sleep(random.nextInt(3000) + 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<String> images = searchImages(keyword, 1);
        if (!images.isEmpty()) {
            String result = images.get(0);
            if (isValidImage(result)) {
                return result;
            }
        }
        return generateFallbackImage(keyword);
    }
    
    private boolean isValidImage(String url) {
        if (url == null || url.isEmpty()) return false;
        if (url.contains("baidu.com") && url.contains("default")) return false;
        if (url.contains("placeholder")) return false;
        return true;
    }
    
    private String generateFallbackImage(String keyword) {
        int hash = keyword.hashCode();
        return "https://picsum.photos/seed/" + Math.abs(hash) + "/640/480";
    }
    
    public List<String> searchImages(String keyword, int count) {
        List<String> result = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = BAIDU_IMAGE_URL + encodedKeyword + "&ie=utf-8&ct=201326592&cl=2&lm=-1&st=-1&fm=result&fr=&sf=1&fmq=1699907225879_R&pv=&ic=&nc=1&z=&hd=&latest=&copyright=";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + (random.nextInt(30) + 100) + ".0.0.0 Safari/537.36")
                    .referrer("https://image.baidu.com/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .timeout(15000)
                    .get();
            Elements imgElements = doc.select("img");
            for (Element img : imgElements) {
                if (result.size() >= count) break;
                String imgUrl = img.attr("data-src");
                if (imgUrl.isEmpty()) imgUrl = img.attr("src");
                if (!imgUrl.isEmpty() && (imgUrl.startsWith("http://") || imgUrl.startsWith("https://"))) {
                    String originalUrl = convertToOriginalUrl(imgUrl);
                    if (isValidImage(originalUrl)) {
                        result.add(originalUrl);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("图片搜索失败(" + keyword + "): " + e.getMessage());
        }
        return result;
    }
    
    private String convertToOriginalUrl(String thumbnailUrl) {
        if (thumbnailUrl.contains("?")) {
            int paramIndex = thumbnailUrl.indexOf("?");
            return thumbnailUrl.substring(0, paramIndex);
        }
        return thumbnailUrl;
    }
}