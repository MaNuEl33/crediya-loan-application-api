package co.com.crediya.config;

import co.com.crediya.usecase.approveorrejectloanapplication.ApproveOrRejectLoanApplicationUseCase;
import co.com.crediya.usecase.manualvalidationapplicationreport.ManualValidationApplicationReportUseCase;
import co.com.crediya.usecase.registerloanapplication.RegisterLoanApplicationUseCase;
import co.com.crediya.usecase.updateautomaticvalidationloanapplicationstate.UpdateAutomaticValidationLoanApplicationStateUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'Use Case' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public ManualValidationApplicationReportUseCase manualValidationApplicationReportUseCase() {
            return Mockito.mock(ManualValidationApplicationReportUseCase.class);
        }

        @Bean
        public RegisterLoanApplicationUseCase registerLoanApplicationUseCase() {
            return Mockito.mock(RegisterLoanApplicationUseCase.class);
        }

        @Bean
        public ApproveOrRejectLoanApplicationUseCase  approveOrRejectLoanApplicationUseCase() {
            return Mockito.mock(ApproveOrRejectLoanApplicationUseCase.class);
        }

        @Bean
        public UpdateAutomaticValidationLoanApplicationStateUseCase updateAutomaticValidationLoanApplicationStateUseCase() {
            return Mockito.mock(UpdateAutomaticValidationLoanApplicationStateUseCase.class);
        }
    }

}