package org.gaurav.linkedin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Linkedin {
    Properties properties = new Properties();
    private final String API_KEY;
    public UserInfo userInfo;
    public Linkedin() throws IOException, InterruptedException {
        properties.load(new FileInputStream("src/main/resources/application.properties"));
        this.API_KEY = properties.getProperty("linkedIn.api-key");
        this.userInfo = getUserInfo();
    }
    public byte[] getImageBinaryFile(String imagePath) throws IOException {
        return Files.readAllBytes(Path.of(imagePath));
    }
    private UserInfo getUserInfo() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.linkedin.com/v2/userinfo"))
                .header("Authorization", "Bearer " + this.API_KEY)
                .header("cache-control", "no-cache")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .header("Content-Type", "application/json")
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Gson().fromJson(response.body(), UserInfo.class);
    }

//    Text Share
    public boolean shareText(String content) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.linkedin.com/v2/ugcPosts"))
                .header("Authorization", "Bearer " + this.API_KEY)
                .header("cache-control", "no-cache")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "author": "urn:li:person:%s",
                            "lifecycleState": "PUBLISHED",
                            "specificContent": {
                                "com.linkedin.ugc.ShareContent": {
                                    "shareCommentary": {
                                        "text": "%s"
                                    },
                                    "shareMediaCategory": "NONE"
                                }
                            },
                            "visibility": {
                                "com.linkedin.ugc.MemberNetworkVisibility": "PUBLIC"
                            }
                        }
                        """.formatted(this.userInfo.sub(), content)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode()==201;
    }
    public boolean shareTextWithArticle(String content, String originalUrl) throws IOException, InterruptedException {
        return shareTextWithArticle(content, originalUrl, "", "");
    }
    public boolean shareTextWithArticle(String content, String originalUrl, String descriptionText) throws IOException, InterruptedException {
        return shareTextWithArticle(content, originalUrl, descriptionText, "");
    }
//  Article or URL Share
    public boolean shareTextWithArticle(String content, String originalUrl, String descriptionText, String titleText) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.linkedin.com/v2/ugcPosts"))
                .header("Authorization", "Bearer " + this.API_KEY)
                .header("cache-control", "no-cache")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "author": "urn:li:person:%s",
                            "lifecycleState": "PUBLISHED",
                            "specificContent": {
                                "com.linkedin.ugc.ShareContent": {
                                    "shareCommentary": {
                                        "text": "%s"
                                    },
                                    "shareMediaCategory": "ARTICLE",
                                    "media": [
                                        {
                                            "status": "READY",
                                            "description": {
                                                "text": "%s"
                                            },
                                            "originalUrl": "%s",
                                            "title": {
                                                "text": "%s"
                                            }
                                        }
                                    ]
                                }
                            },
                            "visibility": {
                                "com.linkedin.ugc.MemberNetworkVisibility": "PUBLIC"
                            }
                        }
                        """.formatted(this.userInfo.sub(), content, descriptionText, originalUrl, titleText)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode()==201;
    }

//    Register the Image and get uploadUrl and asset
    private String[] getUploadUrlAndAsset() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.linkedin.com/v2/assets?action=registerUpload"))
                .header("Authorization", "Bearer " + this.API_KEY)
                .header("cache-control", "no-cache")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "registerUploadRequest": {
                                "recipes": [
                                    "urn:li:digitalmediaRecipe:feedshare-image"
                                ],
                                "owner": "urn:li:person:%s",
                                "serviceRelationships": [
                                    {
                                        "relationshipType": "OWNER",
                                        "identifier": "urn:li:userGeneratedContent"
                                    }
                                ]
                            }
                        }""".formatted(this.userInfo.sub())))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement value = JsonParser.parseString(response.body()).getAsJsonObject().get("value");
        return new String[]{value.getAsJsonObject().get("uploadMechanism")
                .getAsJsonObject().get("com.linkedin.digitalmedia.uploading.MediaUploadHttpRequest")
                .getAsJsonObject().get("uploadUrl").getAsString(),
                value.getAsJsonObject().get("asset").getAsString()};
    }

//    Upload Image Binary File
//    get String uploadUrl and a binary image file
    private boolean uploadImageBinaryFile(String uploadUrl, byte[] imageBinaryFile) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("Authorization", "Bearer " + this.API_KEY)
                .header("cache-control", "no-cache")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(imageBinaryFile))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode()==201;
    }

//    Create an Image Share
    public boolean shareTextWithImage(String content, byte[] imageBinaryFile, String descriptionText) throws IOException, InterruptedException {
        return shareTextWithImage(content, imageBinaryFile, descriptionText, "");
    }
    public boolean shareTextWithImage(String content, byte[] imageBinaryFile) throws IOException, InterruptedException {
        return shareTextWithImage(content, imageBinaryFile, "", "");
    }
    public boolean shareTextWithImage(String content, byte[] imageBinaryFile, String descriptionText, String titleText) throws IOException, InterruptedException {
        String[] uploadUrlAndAsset = getUploadUrlAndAsset();
        if(uploadImageBinaryFile(uploadUrlAndAsset[0], imageBinaryFile))
            return postTextWithImage(content, descriptionText, uploadUrlAndAsset[1], titleText);
        return false;
    }
    private boolean postTextWithImage(String content, String descriptionText, String asset, String titleText) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.linkedin.com/v2/ugcPosts"))
                .header("Authorization", "Bearer " + this.API_KEY)
                .header("cache-control", "no-cache")
                .header("X-Restli-Protocol-Version", "2.0.0")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                            "author": "urn:li:person:%s",
                            "lifecycleState": "PUBLISHED",
                            "specificContent": {
                                "com.linkedin.ugc.ShareContent": {
                                    "shareCommentary": {
                                        "text": "%s"
                                    },
                                    "shareMediaCategory": "IMAGE",
                                    "media": [
                                        {
                                            "status": "READY",
                                            "description": {
                                                "text": "%s"
                                            },
                                            "media": "%s",
                                            "title": {
                                                "text": "%s"
                                            }
                                        }
                                    ]
                                }
                            },
                            "visibility": {
                                "com.linkedin.ugc.MemberNetworkVisibility": "PUBLIC"
                            }
                        }
                        """.formatted(this.userInfo.sub(), content, descriptionText, asset, titleText)))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(content);
        System.out.println(descriptionText);
        System.out.println(asset);
        System.out.println(titleText);
        System.out.println(response.body());

        return response.statusCode()==201;
    }

    @Override
    public String toString() {
        return userInfo.toString();
    }
}
