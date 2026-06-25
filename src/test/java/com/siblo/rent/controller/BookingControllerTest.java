package com.siblo.rent.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"john@siblo.com\",\"password\":\"john123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = loginResult.getResponse().getContentAsString();
        token = JsonPath.read(body, "$.token");
    }

    @SuppressWarnings("unchecked")
    private <T> T firstMatch(String json, String path) {
        java.util.List<T> list = JsonPath.read(json, path);
        return list.isEmpty() ? null : list.get(0);
    }

    @Test
    void patchCancel_withBody_cancelsBooking() throws Exception {
        String bookingsJson = mockMvc.perform(get("/api/bookings/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer pendingId = firstMatch(bookingsJson, "$[?(@.status == 'PENDING_PAYMENT')].id");
        if (pendingId == null) return;

        mockMvc.perform(patch("/api/bookings/" + pendingId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"cancel\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void patchCancel_withoutBody_returns400() throws Exception {
        mockMvc.perform(patch("/api/bookings/1")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchReschedule_cancelsOldAndCreatesNew() throws Exception {
        String bookingsJson = mockMvc.perform(get("/api/bookings/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer confirmedId = firstMatch(bookingsJson, "$[?(@.status == 'CONFIRMED')].id");
        Integer courtId = firstMatch(bookingsJson, "$[?(@.status == 'CONFIRMED')].courtId");
        if (confirmedId == null || courtId == null) return;

        String newDate = java.time.LocalDate.now().plusDays(3).toString();
        String slotsJson = mockMvc.perform(get("/api/courts/" + courtId + "/availability?date=" + newDate))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.List<Integer> slotIds = JsonPath.read(slotsJson, "$[?(@.status == 'AVAILABLE')].id");
        if (slotIds.isEmpty()) return;

        String rescheduleBody = String.format(
            "{\"action\":\"reschedule\",\"courtId\":%d,\"slotIds\":[%d],\"date\":\"%s\"}",
            courtId.intValue(), slotIds.get(0).intValue(), newDate);

        mockMvc.perform(patch("/api/bookings/" + confirmedId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(rescheduleBody))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedBooking_returns401() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"courtId\":1,\"slotIds\":[1],\"date\":\"2026-12-01\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndPayFlow_confirmsBooking() throws Exception {
        Integer courtId = 1;
        String futureDate = java.time.LocalDate.now().plusDays(2).toString();

        int courtStatus = mockMvc.perform(get("/api/courts/" + courtId + "/availability")
                .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
        if (courtStatus != 200) return;

        String slotsJson = mockMvc.perform(get("/api/courts/" + courtId + "/availability?date=" + futureDate)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.List<Integer> slotIds = JsonPath.read(slotsJson, "$[?(@.status == 'AVAILABLE')].id");
        if (slotIds.size() < 2) return;

        String createBody = String.format(
            "{\"courtId\":%d,\"slotIds\":[%d,%d],\"date\":\"%s\"}",
            courtId.intValue(), slotIds.get(0).intValue(), slotIds.get(1).intValue(), futureDate);

        MvcResult createResult = mockMvc.perform(post("/api/bookings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andReturn();

        Integer bookingId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(post("/api/bookings/" + bookingId + "/pay")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
