package co.com.crediya.r2dbc.helper;

import co.com.crediya.model.loantype.LoanType;
import co.com.crediya.model.petition.Petition;
import co.com.crediya.model.status.Status;
import co.com.crediya.r2dbc.entity.PetitionEntity;
import co.com.crediya.r2dbc.util.PetitionEntityUtil;
import co.com.crediya.r2dbc.util.PetitionUtil;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;


class PetitionMapperTest {
    private PetitionMapper petitionMapper = Mappers.getMapper(PetitionMapper.class);

    @Test
    void mustMapPetitionDomainToEntity() {

        //De dominio a entidad
        PetitionEntity petitionEntity = petitionMapper.toEntity(PetitionUtil.petition());

        assertThat(petitionEntity.getStatusId()).isEqualTo(PetitionEntityUtil.petitionEntity().getStatusId());
        assertThat(petitionEntity.getLoanTypeId()).isEqualTo(PetitionEntityUtil.petitionEntity().getLoanTypeId());
        assertThat(petitionEntity.getId()).isEqualTo(PetitionEntityUtil.petitionEntity().getId());
    }

    @Test
    void mustMapPetitionEntityToDomain() {

        //De entidad a dominio
        Petition petition = petitionMapper.toDomain(PetitionEntityUtil.petitionEntity());

        assertThat(petition.getLoanType()).isNull();
    }

    @Test
    void mustMapPetitionEntityToModel() {
        PetitionEntity entity = PetitionEntityUtil.petitionEntity();

        Petition petition = petitionMapper.toModel(entity);

        assertThat(petition.getId()).isEqualTo(entity.getId());

        // Verificar que el mapper creó el Status correctamente
        Status status = petition.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getId()).isEqualTo(entity.getStatusId());

        // Verificar que el mapper creó el LoanType correctamente
        LoanType loanType = petition.getLoanType();
        assertThat(loanType).isNotNull();
        assertThat(loanType.getId()).isEqualTo(entity.getLoanTypeId());
    }

}
