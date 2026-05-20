package com.utephonehub.backend.service.impl;

import com.utephonehub.backend.dto.request.PromotionTemplateRequest;
import com.utephonehub.backend.dto.response.PromotionTemplateResponse;
import com.utephonehub.backend.entity.PromotionTemplate;
import com.utephonehub.backend.exception.ResourceNotFoundException;
import com.utephonehub.backend.repository.PromotionTemplateRepository;
import com.utephonehub.backend.service.IPromotionTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionTemplateServiceImpl implements IPromotionTemplateService {

    private final PromotionTemplateRepository templateRepository;

    @Override
    @Transactional
    public PromotionTemplateResponse createTemplate(PromotionTemplateRequest request) {
        log.info("Creating promotion template with code: {}", request.getCode());

        PromotionTemplate template = PromotionTemplate.builder()
                .code(request.getCode())
                .type(request.getType())
                .createdAt(LocalDateTime.now())
                .build();

        PromotionTemplate saved = templateRepository.save(template);
        log.info("Created template with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PromotionTemplateResponse updateTemplate(String id, PromotionTemplateRequest request) {
        log.info("Updating template ID: {}", id);

        PromotionTemplate template = findTemplateOrThrow(id);
        template.setCode(request.getCode());
        template.setType(request.getType());

        PromotionTemplate updated = templateRepository.save(template);
        log.info("Updated template ID: {}", id);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTemplate(String id) {
        log.info("Deleting template ID: {}", id);

        PromotionTemplate template = findTemplateOrThrow(id);
        
        // Check if template is being used by any promotion
        if (!template.getPromotions().isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete template that is being used by " + template.getPromotions().size() + " promotion(s)"
            );
        }

        templateRepository.delete(template);
        log.info("Deleted template ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionTemplateResponse getTemplateById(String id) {
        log.info("Getting template by ID: {}", id);
        PromotionTemplate template = findTemplateOrThrow(id);
        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionTemplateResponse> getAllTemplates() {
        log.info("Getting all templates");
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PromotionTemplate findTemplateOrThrow(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with ID: " + id));
    }

    private PromotionTemplateResponse mapToResponse(PromotionTemplate template) {
        return PromotionTemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .type(template.getType())
                .createdAt(template.getCreatedAt())
                .build();
    }
}
