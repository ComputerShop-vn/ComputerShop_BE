package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.entity.Brand;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    Brand toBrand(BrandCreationRequest request);

    BrandResponse toBrandResponse(Brand brand);

    void updateBrand(@MappingTarget Brand brand, BrandUpdateRequest request);
}
