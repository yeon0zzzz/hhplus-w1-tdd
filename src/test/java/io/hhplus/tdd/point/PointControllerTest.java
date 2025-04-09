package io.hhplus.tdd.point;

import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void clear() {
        // 메모리 초기화
        userPointRepository.insertOrUpdate(1L, 0L);
    }

    @Test
    @DisplayName("포인트_충전_성공_테스트")
    void success_charge_point_test() throws Exception {
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("500"))
//                .andDo(print()) // 응답 확인용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(500));
    }

    @Test
    @DisplayName("포인트_충전_실패_테스트")
    void fail_charge_point_test() throws Exception {
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("100000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(MessageConstants.EXCEEDS_POINT_LIMIT));
    }

    @Test
    @DisplayName("포인트_사용_성공_테스트")
    void success_use_point_test() throws Exception {

        userPointRepository.insertOrUpdate(1L, 700L);

        mockMvc.perform(patch("/point/1/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(200));
    }

    @Test
    @DisplayName("포인트_사용_실패_테스트")
    void fail_use_point_test() throws Exception {

        userPointRepository.insertOrUpdate(1L, 700L);

        mockMvc.perform(patch("/point/1/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("10000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value(MessageConstants.INSUFFICIENT_POINTS));
    }

    @Test
    @DisplayName("사용자_포인트_조회_테스트")
    void get_point_test() throws Exception {
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("사용자_포인트_내역_조회_테스트")
    void get_list_point_history_test() throws Exception {

        long updateMillis = System.currentTimeMillis();

        pointHistoryRepository.insert(2L, 200L, TransactionType.CHARGE, updateMillis);
        pointHistoryRepository.insert(2L, 100L, TransactionType.USE, updateMillis);

        mockMvc.perform(get("/point/2/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].type").value("USE"))
                .andExpect(jsonPath("$[1].updateMillis").value(updateMillis));
    }
}