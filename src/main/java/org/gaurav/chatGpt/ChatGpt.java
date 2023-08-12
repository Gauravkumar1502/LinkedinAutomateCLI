package org.gaurav.chatGpt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gaurav.linkedin.Linkedin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ChatGpt {
    Properties properties = new Properties();
    private final String API_KEY;

    public static enum Role {
        SYSTEM("system"),ASSISTANT("assistant"),USER("user"),FUNCTION("function");
        private final String value;
        private Role(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }
    private final String model;
    private final Role role;
    private final String prompt;
    public ChatGpt(String prompt) throws IOException {
        this(prompt, "gpt-3.5-turbo", Role.USER);
    }
    public ChatGpt(String prompt, String model) throws IOException {
        this(prompt, model, Role.USER);
    }
    public ChatGpt(String prompt, String model, Role role) throws IOException {
        this.prompt = prompt;
        this.model = model;
        this.role = role;
        System.out.println("Model: " + model + " Role: " + role.getValue());
        System.out.println("Prompt: " + this.prompt);
        properties.load(Linkedin.class.getClassLoader().getResourceAsStream("application.properties"));
        this.API_KEY = properties.getProperty("chatGPT.api-key");
    }
    private String makeInputString(){
        return """
                {
                    "model": "%s",
                    "messages": [
                        {
                            "role": "%s",
                            "content": "%s"
                        }
                    ]
                }
                """.formatted(model, role.getValue(), prompt);
    }
    private String askGpt() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(makeInputString()))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    public String getResponse() throws IOException, InterruptedException {
        JsonObject jsonObject = JsonParser.parseString(askGpt()).getAsJsonObject();
        JsonArray choicesArray  = jsonObject.getAsJsonArray("choices");
        String response = "";
        for (JsonElement choiceElement : choicesArray) {
            JsonObject choiceObject = choiceElement.getAsJsonObject();
            JsonObject messageObject = choiceObject.getAsJsonObject("message");
            return messageObject.get("content").getAsString();
        }
        return response;
    }
}
