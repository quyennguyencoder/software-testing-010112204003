package com.phonehub.backend.controller;

import com.phonehub.backend.service.intf.IChatbotConfigService;
import com.phonehub.backend.service.intf.IChatbotConfigService.ChatbotStatusInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatbotAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IChatbotConfigService chatbotConfigService;

    @InjectMocks
    private ChatbotAdminController chatbotAdminController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chatbotAdminController)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().equals(Jwt.class);
                    }

                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                                  org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                                  org.springframework.web.context.request.NativeWebRequest webRequest,
                                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) throws Exception {
                        return Jwt.withTokenValue("mocked-token")
                                .header("alg", "none")
                                .claim("email", "admin@phonehub.com")
                                .claim("sub", "admin")
                                .build();
                    }
                })
                .build();
    }

    @Test
    public void getStatus_ShouldReturnChatbotStatus() throws Exception {
        ChatbotStatusInfo info = new ChatbotStatusInfo(true, "admin@phonehub.com", "2026-05-29T21:30:00", "Testing reason");

        when(chatbotConfigService.getChatbotStatus()).thenReturn(info);

        mockMvc.perform(get("/api/v1/admin/chatbot/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    public void enableChatbot_ShouldEnable() throws Exception {
        // standaloneSetup cannot easily resolve @AuthenticationPrincipal unless mocked.
        // But getAdminEmail handles null gracefully, using "UNKNOWN_ADMIN". Let's verify that fallback path.
        when(chatbotConfigService.enableChatbot(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/chatbot/enable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    public void disableChatbot_ShouldDisable() throws Exception {
        when(chatbotConfigService.disableChatbot(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/chatbot/disable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    public void toggleChatbot_ShouldToggle() throws Exception {
        when(chatbotConfigService.toggleChatbot(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/chatbot/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    public void updateConfig_ShouldUpdate() throws Exception {
        when(chatbotConfigService.enableChatbot(anyString())).thenReturn(true);

        mockMvc.perform(put("/api/v1/admin/chatbot/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":true,\"reason\":\"Load test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    public void updateConfig_MissingEnabled_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/admin/chatbot/config")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Missing enabled field\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.success").value(false));
    }
}
