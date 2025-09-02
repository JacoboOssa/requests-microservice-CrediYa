package co.com.crediya.r2dbc.status;

import co.com.crediya.r2dbc.util.StatusUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusReactiveRepositoryAdapterTest {

    @InjectMocks
    StatusReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    StatusReactiveRepository repository;

    @Test
    void mustReturnStatusByName(){
        when(repository.getStatusByName("PENDING")).thenReturn(Mono.just(StatusUtil.status()));

        StepVerifier.create(repositoryAdapter.getStatusByName("PENDING"))
                .expectNextMatches(status -> status.getDescription().equals(StatusUtil.status().getDescription()))
                .verifyComplete();
    }

    @Test
    void mustNotReturnStatusByName(){
        when(repository.getStatusByName("APPROVED")).thenReturn(Mono.empty());

        StepVerifier.create(repositoryAdapter.getStatusByName("APPROVED"))
                .expectNextCount(0)
                .verifyComplete();
    }
}
