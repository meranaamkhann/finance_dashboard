package com.finance.dashboard.config;

import com.finance.dashboard.model.CustomCategory;
import com.finance.dashboard.repository.CustomCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CustomCategoryRepository categoryRepository;

    private static final String[] EXPENSE_CATEGORIES = {
            "FOOD",
            "RENT",
            "UTILITIES",
            "TRANSPORT",
            "HEALTHCARE",
            "EDUCATION",
            "ENTERTAINMENT",
            "SHOPPING",
            "TRAVEL",
            "INSURANCE",
            "SAVINGS",
            "EMI",
            "SUBSCRIPTION",
            "PERSONAL_CARE",
            "HOME",
            "OTHER_EXPENSE"
    };

    @Override
    @Transactional
    public void run(String... args) {

        for (String name : EXPENSE_CATEGORIES) {

            if (!categoryRepository.existsByNameAndSystemTrue(name)) {

                categoryRepository.save(
                        CustomCategory.builder()
                                .name(name)
                                .color("#6366f1")
                                .type("EXPENSE")
                                .workspaceId(null)
                                .createdBy(null)
                                .system(true)
                                .build()
                );
            }
        }
    }
}