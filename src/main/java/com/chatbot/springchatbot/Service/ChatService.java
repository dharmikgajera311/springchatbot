package com.chatbot.springchatbot.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    // this is a link from the response comes back here
    private static final String OPENAI_URL = "https://openrouter.ai/api/v1/chat/completions";

    public String getChatGPTResponse(String userMessage) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost httpPost = new HttpPost(OPENAI_URL);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + apiKey);

            // JSON body
            String requestBody = "{\n" +
                    "  \"model\": \"gpt-3.5-turbo\",\n" +
                    "  \"messages\": [\n" +
                    "    {\"role\": \"system\", \"content\": \"You are a helpful assistant for LearnXpert users.\"},\n" +
                    "    {\"role\": \"user\", \"content\": \"" + userMessage + "\"}\n" +
                    "  ]\n" +
                    "}";

            httpPost.setEntity(new StringEntity(requestBody));

            return httpClient.execute(httpPost, response -> {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.getEntity().getContent());

                System.out.println("RAW RESPONSE:\n" + jsonResponse.toPrettyString());

                // Handle API errors
                if (jsonResponse.has("error")) {
                    String errorCode = jsonResponse.path("error").path("code").asText();
                    String errorMessage = jsonResponse.path("error").path("message").asText();

                    switch (errorCode) {
                        case "insufficient_quota":
                            return "❌ You have exceeded your OpenAI quota. Please check your billing settings.";
                        case "invalid_api_key":
                            return "❌ Your API key is invalid. Please verify it in the configuration.";
                        case "rate_limit_exceeded":
                            return "⏳ Rate limit exceeded. Please wait and try again.";
                        case "model_not_found":
                            return "⚠️ The specified model is not available. Please verify the model name.";
                        default:
                            return "🚨 OpenAI API Error: " + errorMessage;
                    }
                }

                // Handle successful response
                JsonNode choicesNode = jsonResponse.path("choices");
                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode messageContent = choicesNode.get(0).path("message").path("content");
                    if (!messageContent.isMissingNode()) {
                        return messageContent.asText().trim();
                    } else {
                        return "⚠️ Error: ChatGPT responded without content.";
                    }
                } else {
                    return "⚠️ Error: Empty or missing choices in ChatGPT response.";
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
            return "🌐 Network error: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "🚧 Unexpected error: " + e.getMessage();
        }
    }
}

// 15000 next 25000 = high
// down 10000 = low

//20 grid
//1 quentity
