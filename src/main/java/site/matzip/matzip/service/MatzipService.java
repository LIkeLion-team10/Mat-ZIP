package site.matzip.matzip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.matzip.matzip.domain.Matzip;
import site.matzip.matzip.dto.MatzipCreationDTO;
import site.matzip.matzip.repository.MatzipRepository;
import site.matzip.matzip.rsData.RsData;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatzipService {
    private final MatzipRepository matzipRepository;

    public RsData create(MatzipCreationDTO creationDTO) {
        Matzip matzip = Matzip.builder()
                .matzipName(creationDTO.getMatzipName())
                .address(creationDTO.getAddress())
                .description(creationDTO.getDescription())
                .matzipType(creationDTO.getMatzipTypeEnum())
                .build();

        Matzip savedMatzip = matzipRepository.save(matzip);
        return RsData.of("S-1", "맛집이 등록 되었습니다.");
    }

    public List<Matzip> findAll() {
        List<Matzip> list = matzipRepository.findAll();
        return list;
    }
}
