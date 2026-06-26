package com.phonehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phonehub.backend.dto.request.ChatbotAssistantUserRequest;
import com.phonehub.backend.dto.response.ChatbotAssistantUserResponse;
import com.phonehub.backend.service.IChatbotAssistantUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatbotAssistantUserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IChatbotAssistantUserService chatbotService;

    @InjectMocks
    private ChatbotAssistantUserController chatbotAssistantUserController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatbotAssistantUserController).build();
    }

    @Test
    public void chat_ShouldReturnResponse() throws Exception {
        ChatbotAssistantUserRequest request = new ChatbotAssistantUserRequest();
        request.setMessage("Gợi ý iPhone giá rẻ");

        ChatbotAssistantUserResponse response = ChatbotAssistantUserResponse.builder()
                .aiResponse("Bạn có thể tham khảo iPhone 11")
                .build();

        when(chatbotService.chat(any(ChatbotAssistantUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/chatbot-assistant/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.aiResponse").value("Bạn có thể tham khảo iPhone 11"));
    }

    @Test
    public void chat_EmptyMessage_ShouldReturnBadRequest() throws Exception {
        ChatbotAssistantUserRequest request = new ChatbotAssistantUserRequest();
        request.setMessage("");

        mockMvc.perform(post("/api/v1/chatbot-assistant/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void clearCache_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/v1/chatbot-assistant/clear-cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Success"));
    }
}
