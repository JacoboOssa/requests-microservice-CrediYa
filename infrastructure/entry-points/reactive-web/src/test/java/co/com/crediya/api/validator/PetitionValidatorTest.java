package co.com.crediya.api.validator;

import co.com.crediya.api.util.PetitionUtil;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetitionValidatorTest {
    @Mock
    Validator validator;

    @InjectMocks
    PetitionValidator petitionValidator;

    @Test
    void mustValidateSuccessfully() {
        when(validator.validate(PetitionUtil.createPetitionDTO())).thenReturn(Collections.emptySet());

        StepVerifier.create(petitionValidator.validate(PetitionUtil.createPetitionDTO()))
                .expectNext(PetitionUtil.createPetitionDTO())
                .verifyComplete();
    }

    @Test
    void mustValidateSuccessfullyWithErrors() {
        when(validator.validate(PetitionUtil.createPetitionDTO())).thenReturn(Collections.singleton(null));

        StepVerifier.create(petitionValidator.validate(PetitionUtil.createPetitionDTO()))
                .expectError()
                .verify();
    }
}
