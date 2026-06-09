package com.wms.shoppingsys.controller;

import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.service.AuthService;

import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerTest {
    MockMvc mvc;

    @Autowired WebApplicationContext context;
    @Autowired AuthService authService;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void registersAndLogsInUser() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"alice\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void duplicateUsernameReturnsBadRequest() throws Exception {
        String body = "{\"username\":\"duplicate\",\"password\":\"pass123\"}";

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void usernamesAreCaseInsensitiveAndTrimmed() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"  CaseUser  \",\"password\":\"pass123\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"caseuser\",\"password\":\"pass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"CASEUSER\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("caseuser"));
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wrong-password\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wrong-password\",\"password\":\"badpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void requireAdminRejectsUserAndAllowsAdmin() {
        CurrentUser user = new CurrentUser(1L, "regular", UserRole.USER);
        CurrentUser admin = new CurrentUser(2L, "admin", UserRole.ADMIN);

        assertThatThrownBy(() -> authService.requireAdmin(user))
                .isInstanceOf(BusinessException.class);

        assertThatCode(() -> authService.requireAdmin(admin))
                .doesNotThrowAnyException();
    }
}
