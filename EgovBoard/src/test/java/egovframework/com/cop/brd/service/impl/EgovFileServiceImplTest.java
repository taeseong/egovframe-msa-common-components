package egovframework.com.cop.brd.service.impl;

import egovframework.com.cop.brd.entity.FileDetailId;
import egovframework.com.cop.brd.repository.EgovFileDetailRepository;
import egovframework.com.cop.brd.repository.EgovFileRepository;
import egovframework.com.cop.brd.service.FileVO;
import org.egovframe.rte.fdl.idgnr.EgovIdGnrService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EgovFileServiceImplTest {

    @Test
    void detailFileInfReturnsNullWhenFileDoesNotExist() {
        EgovFileRepository fileRepository = mock(EgovFileRepository.class);
        EgovFileDetailRepository fileDetailRepository = mock(EgovFileDetailRepository.class);
        EgovIdGnrService idGnrService = mock(EgovIdGnrService.class);
        EgovFileServiceImpl service = new EgovFileServiceImpl(
                fileRepository, fileDetailRepository, idGnrService);

        FileVO fileVO = new FileVO();
        fileVO.setAtchFileId("FILE_1");
        fileVO.setFileSn("0");

        FileDetailId fileDetailId = new FileDetailId();
        fileDetailId.setAtchFileId("FILE_1");
        fileDetailId.setFileSn("0");
        when(fileDetailRepository.findById(fileDetailId)).thenReturn(Optional.empty());

        assertThat(service.detailFileInf(fileVO)).isNull();
    }
}
