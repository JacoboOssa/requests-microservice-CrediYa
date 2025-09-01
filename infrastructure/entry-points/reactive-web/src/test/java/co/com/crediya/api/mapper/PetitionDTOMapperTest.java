package co.com.crediya.api.mapper;

import co.com.crediya.api.dto.response.PetitionResponseDTO;
import co.com.crediya.api.util.PetitionUtil;
import co.com.crediya.model.petition.Petition;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PetitionDTOMapperTest {
    private final PetitionDTOMapper petitionDTOMapper = Mappers.getMapper(PetitionDTOMapper.class);

    @Test
    void mustMapToDomain() {

        Petition mappedPetition = petitionDTOMapper.toPetition(PetitionUtil.createPetitionDTO());

        assertThat(mappedPetition).isNotNull();
        assertThat(mappedPetition.getAmount()).isEqualTo(PetitionUtil.createPetitionDTO().amount());

    }

    @Test
    void mustMapToDto() {
        PetitionResponseDTO mappedPetitionResponseDTO = petitionDTOMapper.toPetitionResponseDTO(PetitionUtil.petition());

        assertThat(mappedPetitionResponseDTO).isNotNull();
        assertThat(mappedPetitionResponseDTO.email()).isEqualTo(PetitionUtil.petition().getEmail());
    }
}
