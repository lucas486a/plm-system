package com.plm.service;

import com.plm.dto.AssemblyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssemblyService {

    AssemblyDTO createAssembly(AssemblyDTO assemblyDTO);

    AssemblyDTO getAssemblyById(Long id);

    AssemblyDTO updateAssembly(Long id, AssemblyDTO assemblyDTO);

    void deleteAssembly(Long id);

    Page<AssemblyDTO> listAssemblies(Pageable pageable);
}
