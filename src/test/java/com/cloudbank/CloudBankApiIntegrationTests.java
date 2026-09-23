package com.cloudbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CloudBankApiIntegrationTests {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void coreBankingFlowSupportsAuthAccountsMoneyMovementHistoryAndIdempotency() throws Exception {
        UserSession alice = registerLoginAndCreateAccount("alice@example.com");
        UserSession bob = registerLoginAndCreateAccount("bob@example.com");

        JsonNode deposit = postJson("/api/v1/accounts/" + alice.accountId + "/deposits", alice.token, "dep-1", "{\"amount\":\"100.00\",\"description\":\"Initial\"}", 200);
        assertThat(deposit.get("balanceAfter").decimalValue()).isEqualByComparingTo("100.00");

        JsonNode duplicateDeposit = postJson("/api/v1/accounts/" + alice.accountId + "/deposits", alice.token, "dep-1", "{\"amount\":\"100.00\",\"description\":\"Initial\"}", 200);
        assertThat(duplicateDeposit.get("transactionId").asText()).isEqualTo(deposit.get("transactionId").asText());

        postJson("/api/v1/accounts/" + alice.accountId + "/deposits", alice.token, "dep-1", "{\"amount\":\"101.00\"}", 409);

        JsonNode withdrawal = postJson("/api/v1/accounts/" + alice.accountId + "/withdrawals", alice.token, "wd-1", "{\"amount\":\"40.00\"}", 200);
        assertThat(withdrawal.get("balanceAfter").decimalValue()).isEqualByComparingTo("60.00");

        postJson("/api/v1/accounts/" + alice.accountId + "/withdrawals", alice.token, "wd-2", "{\"amount\":\"999.00\"}", 422);

        JsonNode transfer = postJson("/api/v1/transfers", alice.token, "tx-1", "{\"sourceAccountId\":\"" + alice.accountId + "\",\"destinationAccountId\":\"" + bob.accountId + "\",\"amount\":\"25.00\",\"description\":\"Shared bill\"}", 200);
        assertThat(transfer.get("sourceBalanceAfter").decimalValue()).isEqualByComparingTo("35.00");

        String historyBody = mockMvc.perform(get("/api/v1/accounts/" + alice.accountId + "/transactions")
                .header("Authorization", "Bearer " + alice.token))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode history = objectMapper.readTree(historyBody);
        assertThat(history.get("content").size()).isEqualTo(3);

        mockMvc.perform(get("/api/v1/accounts/" + alice.accountId)
                .header("Authorization", "Bearer " + bob.token))
            .andExpect(status().isNotFound());
    }

    @Test
    void protectedEndpointRequiresToken() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void openApiDocsAreAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());
    }

    private UserSession registerLoginAndCreateAccount(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"StrongPassword123!\",\"firstName\":\"Test\",\"lastName\":\"User\"}"))
            .andExpect(status().isCreated());

        String loginBody = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"StrongPassword123!\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        String token = objectMapper.readTree(loginBody).get("accessToken").asText();

        String accountBody = mockMvc.perform(post("/api/v1/accounts")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        UUID accountId = UUID.fromString(objectMapper.readTree(accountBody).get("id").asText());
        return new UserSession(token, accountId);
    }

    private JsonNode postJson(String path, String token, String idempotencyKey, String json, int expectedStatus) throws Exception {
        String response = mockMvc.perform(post(path)
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().is(expectedStatus))
            .andReturn()
            .getResponse()
            .getContentAsString();
        return response.isBlank() ? objectMapper.createObjectNode() : objectMapper.readTree(response);
    }

    private record UserSession(String token, UUID accountId) {
    }
}
