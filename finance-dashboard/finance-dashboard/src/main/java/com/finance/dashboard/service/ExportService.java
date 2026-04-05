package com.finance.dashboard.service;

import com.finance.dashboard.model.AuditAction;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.TransactionType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.FinancialRecordSpecification;
import com.finance.dashboard.util.SecurityUtils;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final FinancialRecordRepository recordRepo;
    private final AuditService              auditService;

    private static final String[] CSV_HEADER = {
        "ID", "Date", "Type", "Category", "Amount", "Description", "Tags", "Created By", "Created At"
    };

    /**
     * Exports filtered financial records to a CSV string.
     * Applies the same filter criteria as the list endpoint so the
     * user gets exactly what they see on screen — just in spreadsheet form.
     */
    @Transactional(readOnly = true)
    public String exportToCsv(TransactionType type, Category category,
                               LocalDate dateFrom, LocalDate dateTo,
                               String keyword, String ip) {

        Specification<FinancialRecord> spec = FinancialRecordSpecification
                .buildFilter(type, category, dateFrom, dateTo, keyword, null, null);

        List<FinancialRecord> records = recordRepo.findAll(spec,
                Sort.by("date").descending().and(Sort.by("createdAt").descending()));

        StringWriter sw  = new StringWriter();
        try (CSVWriter csv = new CSVWriter(sw)) {
            csv.writeNext(CSV_HEADER);
            DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
            DateTimeFormatter tsFmt   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (FinancialRecord r : records) {
                csv.writeNext(new String[]{
                    String.valueOf(r.getId()),
                    r.getDate().format(dateFmt),
                    r.getType().name(),
                    r.getCategory().name(),
                    r.getAmount().toPlainString(),
                    r.getDescription() != null ? r.getDescription() : "",
                    r.getTags()        != null ? r.getTags()        : "",
                    r.getCreatedBy().getUsername(),
                    r.getCreatedAt() != null ? r.getCreatedAt().format(tsFmt) : ""
                });
            }
        } catch (Exception e) {
            log.error("CSV export failed", e);
            throw new RuntimeException("Export failed: " + e.getMessage());
        }

        auditService.log(SecurityUtils.currentUsername(), AuditAction.EXPORT,
                "FinancialRecord", null,
                "Exported " + records.size() + " records to CSV", null, null, ip);

        return sw.toString();
    }
}
