package com.finance.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dashboard.dto.request.CreateRecordRequest;
import com.finance.dashboard.dto.request.UpdateRecordRequest;
import com.finance.dashboard.dto.response.FinancialRecordResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.UserDetailsImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialRecordService Unit Tests")
class FinancialRecordServiceTest {

    @Mock FinancialRecordRepository recordRepo;
    @Mock UserRepository            userRepo;
    @Mock AuditService              auditService;
    @Mock BudgetAlertService        budgetAlertService;
    @Mock ObjectMapper              objectMapper;

    @InjectMocks FinancialRecordService recordService;

    private User adminUser;
    private FinancialRecord sampleRecord;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(1L).username("admin").email("a@b.com")
                .password("x").role(Role.ADMIN).active(true).build();
        sampleRecord = FinancialRecord.builder().id(10L)
                .amount(new BigDecimal("5000")).type(TransactionType.INCOME)
                .category(Category.SALARY).date(LocalDate.now())
                .description("Test").createdBy(adminUser).deleted(false).build();
        mockSecurityContext(adminUser);
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    @Test
    @DisplayName("create — persists record and returns response")
    void create_Success() {
        CreateRecordRequest req = new CreateRecordRequest();
        req.setAmount(new BigDecimal("5000")); req.setType(TransactionType.INCOME);
        req.setCategory(Category.SALARY);      req.setDate(LocalDate.now().minusDays(1));

        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(recordRepo.save(any())).thenReturn(sampleRecord);

        FinancialRecordResponse res = recordService.create(req, "127.0.0.1");
        assertThat(res.getAmount()).isEqualByComparingTo("5000");
        assertThat(res.getType()).isEqualTo(TransactionType.INCOME);
        verify(recordRepo).save(any(FinancialRecord.class));
    }

    @Test
    @DisplayName("getById — returns record when found")
    void getById_Found() {
        when(recordRepo.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(sampleRecord));
        FinancialRecordResponse res = recordService.getById(10L);
        assertThat(res.getId()).isEqualTo(10L);
        assertThat(res.getCreatedByUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("getById — throws when not found or deleted")
    void getById_NotFound() {
        when(recordRepo.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> recordService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update — modifies only provided fields")
    void update_Partial() {
        UpdateRecordRequest req = new UpdateRecordRequest();
        req.setAmount(new BigDecimal("9999"));
        req.setDescription("Updated");

        when(recordRepo.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(sampleRecord));
        when(recordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FinancialRecordResponse res = recordService.update(10L, req, "127.0.0.1");
        assertThat(res.getAmount()).isEqualByComparingTo("9999");
        assertThat(res.getDescription()).isEqualTo("Updated");
        assertThat(res.getType()).isEqualTo(TransactionType.INCOME); // unchanged
    }

    @Test
    @DisplayName("delete — soft deletes (sets deleted=true)")
    void delete_SoftDelete() {
        when(recordRepo.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(sampleRecord));
        when(recordRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        recordService.delete(10L, "127.0.0.1");
        assertThat(sampleRecord.isDeleted()).isTrue();
    }

    private void mockSecurityContext(User user) {
        UserDetailsImpl principal = new UserDetailsImpl(user);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }
}
