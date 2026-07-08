package com.finance.dashboard.service;
import com.finance.dashboard.dto.request.BudgetRequest;
import com.finance.dashboard.exception.*;
import com.finance.dashboard.model.*;
import com.finance.dashboard.model.enums.*;
import com.finance.dashboard.repository.*;
import com.finance.dashboard.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {
    @Mock BudgetRepository budgetRepository;
    @Mock FinancialRecordRepository recordRepository;
    @Mock SecurityUtils securityUtils;
    @Mock AuditService auditService;
    @InjectMocks BudgetService budgetService;
    private User user;
    @BeforeEach void setUp() {
        user = User.builder().id(1L).username("admin").role(Role.ADMIN).active(true).build();
        lenient().when(securityUtils.getCurrentUser()).thenReturn(user);
        lenient().when(securityUtils.getCurrentUserId()).thenReturn(1L);
        lenient().when(securityUtils.getCurrentUsername()).thenReturn("admin");
    }
    @Test void create_noOverlap_succeeds() {
        when(budgetRepository.existsByUserIdAndCategoryAndActiveTrueAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(any(),any(),any(),any())).thenReturn(false);
        when(recordRepository.spentByUserCategoryAndPeriod(any(),any(),any(),any())).thenReturn(BigDecimal.ZERO);
        when(budgetRepository.save(any())).thenAnswer(i->{Budget b=i.getArgument(0);b.setId(1L);return b;});
        BudgetRequest req = new BudgetRequest(); req.setCategory(Category.FOOD); req.setLimitAmount(new BigDecimal("5000"));
        req.setPeriodStart(LocalDate.now().withDayOfMonth(1)); req.setPeriodEnd(LocalDate.now().withDayOfMonth(28));
        assertThat(budgetService.create(req,"ip").getStatus()).isEqualTo("ON_TRACK");
    }
    @Test void create_overlap_throwsBadRequest() {
        when(budgetRepository.existsByUserIdAndCategoryAndActiveTrueAndPeriodStartLessThanEqualAndPeriodEndGreaterThanEqual(any(),any(),any(),any())).thenReturn(true);
        BudgetRequest req = new BudgetRequest(); req.setCategory(Category.FOOD); req.setLimitAmount(new BigDecimal("5000"));
        req.setPeriodStart(LocalDate.now()); req.setPeriodEnd(LocalDate.now().plusDays(10));
        assertThatThrownBy(()->budgetService.create(req,"ip")).isInstanceOf(BadRequestException.class);
    }
    @Test void getById_notFound_throws() {
        when(budgetRepository.findByIdAndUserIdAndActiveTrue(99L,1L)).thenReturn(Optional.empty());
        assertThatThrownBy(()->budgetService.getById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
