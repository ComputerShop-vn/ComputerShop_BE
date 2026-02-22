package sp26.group3.computer.sba301_computershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sp26.group3.computer.sba301_computershop.dto.request.BrandCreationRequest;
import sp26.group3.computer.sba301_computershop.dto.request.BrandUpdateRequest;
import sp26.group3.computer.sba301_computershop.dto.response.BrandResponse;
import sp26.group3.computer.sba301_computershop.entity.Brand;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(target = "brandId", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    Brand toBrand(BrandCreationRequest request);

    BrandResponse toBrandResponse(Brand brand);

    @Mapping(target = "brandId", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    void updateBrand(@MappingTarget Brand brand, BrandUpdateRequest request);
}
