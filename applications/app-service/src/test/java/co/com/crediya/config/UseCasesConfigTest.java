
package co.com.crediya.config;

import co.com.crediya.model.debtcapacity.gateways.DebtCapacityRepository;
import co.com.crediya.model.loantype.gateways.LoanTypeRepository;
import co.com.crediya.model.notificationmessage.gateways.NotificationMessageRepository;
import co.com.crediya.model.petition.gateways.PetitionRepository;
import co.com.crediya.model.report.gateways.ReportRepository;
import co.com.crediya.model.status.gateways.StatusRepository;
import co.com.crediya.model.user.gateways.UserRepository;
import co.com.crediya.usecase.mapper.PetitionMapper;
import org.junit.jupiter.api.Test;
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
        public LoanTypeRepository loanTypeRepository() {
            return org.mockito.Mockito.mock(LoanTypeRepository.class);
        }

        @Bean
        public PetitionRepository petitionRepository() {
            return org.mockito.Mockito.mock(PetitionRepository.class);
        }

        @Bean
        public StatusRepository statusRepository() {
            return org.mockito.Mockito.mock(StatusRepository.class);
        }

        @Bean
        public UserRepository userRepository() {
            return org.mockito.Mockito.mock(UserRepository.class);
        }

        @Bean
        public DebtCapacityRepository debtCapacityRepository() {
            return org.mockito.Mockito.mock(DebtCapacityRepository.class);
        }

        @Bean
        public NotificationMessageRepository notificationMessageRepository() {
            return org.mockito.Mockito.mock(NotificationMessageRepository.class);
        }

        @Bean
        public ReportRepository reportRepository() {
            return org.mockito.Mockito.mock(ReportRepository.class);
        }

        @Bean
        public PetitionMapper petitionMapper() {
            return org.mockito.Mockito.mock(PetitionMapper.class);
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}