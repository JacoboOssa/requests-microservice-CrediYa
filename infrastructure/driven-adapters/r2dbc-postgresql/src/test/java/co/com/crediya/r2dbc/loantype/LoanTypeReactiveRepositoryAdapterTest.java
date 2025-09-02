package co.com.crediya.r2dbc.loantype;

import co.com.crediya.r2dbc.util.LoanTypeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
class LoanTypeReactiveRepositoryAdapterTest {

    @InjectMocks
    LoanTypeReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    LoanTypeReactiveRepository repository;

    @Test
    void mustFindByName(){
        when(repository.findByName("Personal Loan")).thenReturn(Mono.just(LoanTypeUtil.loanType()));

        StepVerifier.create(repositoryAdapter.findByName("Personal Loan"))
                .expectNextMatches(loanType -> loanType.getName().equals(LoanTypeUtil.loanType().getName()))
                .verifyComplete();
    }

    @Test
    void mustNotFindByName(){
        when(repository.findByName("Business Loan")).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.findByName("Business Loan"))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void mustExistByName(){
        when(repository.existsByName("Personal Loan")).thenReturn(true);

        boolean exists = repositoryAdapter.existsByName("Personal Loan");

        StepVerifier.create(Mono.just(exists))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void mustNotExistByName(){
        when(repository.existsByName("Business Loan")).thenReturn(false);

        boolean exists = repositoryAdapter.existsByName("Business Loan");

        StepVerifier.create(Mono.just(exists))
                .expectNext(false)
                .verifyComplete();
    }


}
