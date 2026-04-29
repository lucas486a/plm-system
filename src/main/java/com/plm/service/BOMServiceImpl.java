package com.plm.service;

import com.plm.config.CacheConfig;
import com.plm.dto.*;
import com.plm.dto.BOMComparisonResult.BOMItemChange;
import com.plm.dto.BOMComparisonResult.BOMItemDiff;
import com.plm.dto.BOMSnapshot.BOMSnapshotItem;
import com.plm.entity.*;
import com.plm.mapper.BOMItemMapper;
import com.plm.mapper.BOMMapper;
import com.plm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BOMServiceImpl implements BOMService {

    private final BOMRepository bomRepository;
    private final BOMItemRepository bomItemRepository;
    private final AssemblyRepository assemblyRepository;
    private final PartRevisionRepository partRevisionRepository;
    private final PartRepository partRepository;
    private final BOMMapper bomMapper;
    private final BOMItemMapper bomItemMapper;

    // In-memory snapshot storage (keyed by BOM ID)
    private final Map<Long, List<BOMSnapshot>> snapshotStore = new ConcurrentHashMap<>();
    private final AtomicLong snapshotIdGenerator = new AtomicLong(1);

    // ==================== BOM CRUD ====================

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.BOMS_CACHE, allEntries = true)
    public BOMDTO createBOM(BOMDTO bomDTO) {
        Assembly assembly = assemblyRepository.findById(bomDTO.getAssemblyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assembly not found with id: " + bomDTO.getAssemblyId()));

        BOM bom = bomMapper.toEntity(bomDTO);
        bom.setAssembly(assembly);
        bom.setStatus(bomDTO.getStatus() != null ? bomDTO.getStatus() : "DRAFT");
        bom.setVersionNumber(bomDTO.getVersionNumber() != null ? bomDTO.getVersionNumber() : 1);

        BOM saved = bomRepository.save(bom);
        return bomMapper.toDTO(saved);
    }

    @Override
    @Cacheable(value = CacheConfig.BOMS_CACHE, key = "#id")
    public BOMDTO getBOMById(Long id) {
        BOM bom = findBOMOrThrow(id);
        return bomMapper.toDTO(bom);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.BOMS_CACHE, allEntries = true)
    public BOMDTO updateBOM(Long id, BOMDTO bomDTO) {
        BOM existing = findBOMOrThrow(id);

        if (bomDTO.getName() != null) {
            existing.setName(bomDTO.getName());
        }
        if (bomDTO.getStatus() != null) {
            existing.setStatus(bomDTO.getStatus());
        }
        if (bomDTO.getComments() != null) {
            existing.setComments(bomDTO.getComments());
        }
        if (bomDTO.getVersionNumber() != null) {
            existing.setVersionNumber(bomDTO.getVersionNumber());
        }

        BOM saved = bomRepository.save(existing);
        return bomMapper.toDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.BOMS_CACHE, allEntries = true)
    public void deleteBOM(Long id) {
        if (!bomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "BOM not found with id: " + id);
        }
        bomRepository.deleteById(id);
    }

    @Override
    public Page<BOMDTO> listBOMs(Pageable pageable) {
        return bomRepository.findAll(pageable).map(bomMapper::toDTO);
    }

    // ==================== BOM Item CRUD ====================

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.BOMS_CACHE, CacheConfig.BOM_ITEMS_CACHE}, allEntries = true)
    public BOMItemDTO addBOMItem(Long bomId, BOMItemDTO itemDTO) {
        BOM bom = findBOMOrThrow(bomId);

        PartRevision partRevision = partRevisionRepository.findById(itemDTO.getPartRevisionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "PartRevision not found with id: " + itemDTO.getPartRevisionId()));

        // Check for circular reference before adding
        checkCircularReference(bom, partRevision);

        BOMItem item = bomItemMapper.toEntity(itemDTO);
        item.setBom(bom);
        item.setPartRevision(partRevision);

        BOMItem saved = bomItemRepository.save(item);
        return bomItemMapper.toDTO(saved);
    }

    @Override
    @Cacheable(value = CacheConfig.BOM_ITEMS_CACHE, key = "#bomId")
    public List<BOMItemDTO> getBOMItems(Long bomId) {
        findBOMOrThrow(bomId);
        return bomItemRepository.findByBomId(bomId).stream()
                .map(bomItemMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.BOMS_CACHE, CacheConfig.BOM_ITEMS_CACHE}, allEntries = true)
    public BOMItemDTO updateBOMItem(Long bomId, Long itemId, BOMItemDTO itemDTO) {
        BOMItem existing = bomItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "BOMItem not found with id: " + itemId));

        if (!existing.getBom().getId().equals(bomId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "BOMItem " + itemId + " does not belong to BOM " + bomId);
        }

        if (itemDTO.getQuantity() != null) {
            existing.setQuantity(itemDTO.getQuantity());
        }
        if (itemDTO.getDesignator() != null) {
            existing.setDesignator(itemDTO.getDesignator());
        }
        if (itemDTO.getFindNumber() != null) {
            existing.setFindNumber(itemDTO.getFindNumber());
        }
        if (itemDTO.getIsMounted() != null) {
            existing.setIsMounted(itemDTO.getIsMounted());
        }
        if (itemDTO.getComment() != null) {
            existing.setComment(itemDTO.getComment());
        }
        if (itemDTO.getScrapFactor() != null) {
            existing.setScrapFactor(itemDTO.getScrapFactor());
        }

        BOMItem saved = bomItemRepository.save(existing);
        return bomItemMapper.toDTO(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConfig.BOMS_CACHE, CacheConfig.BOM_ITEMS_CACHE}, allEntries = true)
    public void removeBOMItem(Long bomId, Long itemId) {
        BOMItem item = bomItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "BOMItem not found with id: " + itemId));

        if (!item.getBom().getId().equals(bomId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "BOMItem " + itemId + " does not belong to BOM " + bomId);
        }

        bomItemRepository.deleteById(itemId);
    }

    // ==================== Multi-level BOM Expansion ====================

    @Override
    public List<BOMExplodeNode> explodeBOM(Long bomId) {
        findBOMOrThrow(bomId);

        List<BOMItem> items = bomItemRepository.findByBomId(bomId);
        Set<Long> visitedAssemblies = new HashSet<>();

        // Pre-fetch all assemblies into a map for O(1) lookup by partNumber
        Map<String, Assembly> assemblyByPartNumber = assemblyRepository.findAll().stream()
                .collect(Collectors.toMap(Assembly::getPartNumber, a -> a, (a, b) -> a));

        // Pre-fetch all BOMs grouped by assemblyId
        Map<Long, List<BOM>> bomsByAssemblyId = bomRepository.findAll().stream()
                .collect(Collectors.groupingBy(bom -> bom.getAssembly().getId()));

        return items.stream()
                .map(item -> buildExplodeNode(item, 0, visitedAssemblies, assemblyByPartNumber, bomsByAssemblyId))
                .collect(Collectors.toList());
    }

    /**
     * Recursively builds the exploded BOM tree.
     * Uses visitedAssemblies set to prevent infinite loops from circular references.
     * Uses pre-fetched maps to avoid N+1 queries during expansion.
     */
    private BOMExplodeNode buildExplodeNode(BOMItem item, int level, Set<Long> visitedAssemblies,
                                             Map<String, Assembly> assemblyByPartNumber,
                                             Map<Long, List<BOM>> bomsByAssemblyId) {
        PartRevision partRevision = item.getPartRevision();
        Part part = partRevision.getPart();

        BOMExplodeNode node = BOMExplodeNode.builder()
                .bomItemId(item.getId())
                .partRevisionId(partRevision.getId())
                .partNumber(part.getPartNumber())
                .partName(part.getName())
                .revision(partRevision.getRevision())
                .quantity(item.getQuantity())
                .scrapFactor(item.getScrapFactor())
                .designator(item.getDesignator())
                .findNumber(item.getFindNumber())
                .isMounted(item.getIsMounted())
                .level(level)
                .children(new ArrayList<>())
                .build();

        // Check if this part has a corresponding assembly with BOMs (sub-explosion)
        Assembly subAssembly = assemblyByPartNumber.get(part.getPartNumber());
        if (subAssembly != null && !visitedAssemblies.contains(subAssembly.getId())) {
            visitedAssemblies.add(subAssembly.getId());
            List<BOM> subBOMs = bomsByAssemblyId.getOrDefault(subAssembly.getId(), Collections.emptyList());
            for (BOM subBOM : subBOMs) {
                List<BOMItem> subItems = bomItemRepository.findByBomId(subBOM.getId());
                for (BOMItem subItem : subItems) {
                    node.getChildren().add(buildExplodeNode(subItem, level + 1, visitedAssemblies,
                            assemblyByPartNumber, bomsByAssemblyId));
                }
            }
        }

        return node;
    }

    // ==================== Where-Used Analysis ====================

    @Override
    public List<BOMDTO> whereUsed(Long partRevisionId) {
        if (!partRevisionRepository.existsById(partRevisionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "PartRevision not found with id: " + partRevisionId);
        }

        List<BOMItem> items = bomItemRepository.findByPartRevisionId(partRevisionId);
        return items.stream()
                .map(BOMItem::getBom)
                .distinct()
                .map(bomMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== BOM Copy ====================

    @Override
    @Transactional
    public BOMDTO copyBOM(Long bomId, String newName) {
        BOM source = findBOMOrThrow(bomId);

        BOM copy = BOM.builder()
                .name(newName)
                .assembly(source.getAssembly())
                .status("DRAFT")
                .versionNumber(1)
                .comments(source.getComments())
                .build();

        BOM savedCopy = bomRepository.save(copy);

        // Deep copy all BOM items
        List<BOMItem> sourceItems = bomItemRepository.findByBomId(bomId);
        for (BOMItem sourceItem : sourceItems) {
            BOMItem copyItem = BOMItem.builder()
                    .bom(savedCopy)
                    .partRevision(sourceItem.getPartRevision())
                    .quantity(sourceItem.getQuantity())
                    .designator(sourceItem.getDesignator())
                    .findNumber(sourceItem.getFindNumber())
                    .isMounted(sourceItem.getIsMounted())
                    .comment(sourceItem.getComment())
                    .scrapFactor(sourceItem.getScrapFactor())
                    .build();
            bomItemRepository.save(copyItem);
        }

        return bomMapper.toDTO(savedCopy);
    }

    // ==================== Circular Reference Detection ====================

    /**
     * Checks if adding a partRevision to a BOM would create a circular reference.
     *
     * Logic: If the part being added is itself an assembly, traverse its BOM tree
     * downward. If the target BOM's assembly appears anywhere in that tree,
     * adding the part would create a cycle.
     */
    private void checkCircularReference(BOM bom, PartRevision partRevision) {
        Part part = partRevision.getPart();

        // Find if the part has a corresponding assembly
        Assembly targetAssembly = assemblyRepository.findByPartNumber(part.getPartNumber()).orElse(null);
        if (targetAssembly == null) {
            // Part is not an assembly — no circular reference possible
            return;
        }

        Long sourceAssemblyId = bom.getAssembly().getId();

        // If the part IS the same assembly, direct self-reference
        if (sourceAssemblyId.equals(targetAssembly.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Circular reference detected: part " + part.getPartNumber() +
                            " is the same assembly as the BOM's parent");
        }

        // Traverse the target assembly's BOM tree to check if sourceAssembly appears
        Set<Long> visited = new HashSet<>();
        if (wouldCreateCycle(sourceAssemblyId, targetAssembly.getId(), visited)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Circular reference detected: adding part " + part.getPartNumber() +
                            " would create a cycle in the BOM structure");
        }
    }

    /**
     * Recursively checks if adding targetAssemblyId to sourceAssemblyId's BOM tree
     * would create a cycle by traversing downward from targetAssemblyId.
     */
    private boolean wouldCreateCycle(Long sourceAssemblyId, Long currentAssemblyId, Set<Long> visited) {
        if (sourceAssemblyId.equals(currentAssemblyId)) {
            return true; // Cycle found
        }

        if (!visited.add(currentAssemblyId)) {
            return false; // Already visited this branch
        }

        List<BOM> boms = bomRepository.findByAssemblyId(currentAssemblyId);
        for (BOM bom : boms) {
            List<BOMItem> items = bomItemRepository.findByBomId(bom.getId());
            for (BOMItem item : items) {
                Part childPart = item.getPartRevision().getPart();
                Assembly childAssembly = assemblyRepository.findByPartNumber(childPart.getPartNumber()).orElse(null);
                if (childAssembly != null) {
                    if (wouldCreateCycle(sourceAssemblyId, childAssembly.getId(), visited)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // ==================== Helpers ====================

    private BOM findBOMOrThrow(Long id) {
        return bomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "BOM not found with id: " + id));
    }

    // ==================== BOM Comparison ====================

    @Override
    public BOMComparisonResult compareBOMs(Long bom1Id, Long bom2Id) {
        BOM bom1 = findBOMOrThrow(bom1Id);
        BOM bom2 = findBOMOrThrow(bom2Id);

        List<BOMItem> items1 = bomItemRepository.findByBomId(bom1Id);
        List<BOMItem> items2 = bomItemRepository.findByBomId(bom2Id);

        // Build maps keyed by partRevisionId for comparison
        Map<Long, BOMItem> map1 = items1.stream()
                .collect(Collectors.toMap(i -> i.getPartRevision().getId(), i -> i, (a, b) -> a));
        Map<Long, BOMItem> map2 = items2.stream()
                .collect(Collectors.toMap(i -> i.getPartRevision().getId(), i -> i, (a, b) -> a));

        // Items in BOM2 but not in BOM1 = added
        List<BOMItemDiff> added = map2.entrySet().stream()
                .filter(e -> !map1.containsKey(e.getKey()))
                .map(e -> toItemDiff(e.getValue()))
                .collect(Collectors.toList());

        // Items in BOM1 but not in BOM2 = removed
        List<BOMItemDiff> removed = map1.entrySet().stream()
                .filter(e -> !map2.containsKey(e.getKey()))
                .map(e -> toItemDiff(e.getValue()))
                .collect(Collectors.toList());

        // Items in both but modified
        List<BOMItemChange> modified = new ArrayList<>();
        for (Map.Entry<Long, BOMItem> entry : map1.entrySet()) {
            BOMItem item2 = map2.get(entry.getKey());
            if (item2 != null) {
                BOMItem item1 = entry.getValue();
                if (isModified(item1, item2)) {
                    modified.add(toItemChange(item1, item2));
                }
            }
        }

        return BOMComparisonResult.builder()
                .bom1Id(bom1Id)
                .bom1Name(bom1.getName())
                .bom2Id(bom2Id)
                .bom2Name(bom2.getName())
                .addedItems(added)
                .removedItems(removed)
                .modifiedItems(modified)
                .totalAdded(added.size())
                .totalRemoved(removed.size())
                .totalModified(modified.size())
                .build();
    }

    private BOMItemDiff toItemDiff(BOMItem item) {
        Part part = item.getPartRevision().getPart();
        return BOMItemDiff.builder()
                .partRevisionId(item.getPartRevision().getId())
                .partNumber(part.getPartNumber())
                .partName(part.getName())
                .revision(item.getPartRevision().getRevision())
                .quantity(item.getQuantity())
                .designator(item.getDesignator())
                .findNumber(item.getFindNumber())
                .build();
    }

    private boolean isModified(BOMItem item1, BOMItem item2) {
        return item1.getQuantity().compareTo(item2.getQuantity()) != 0
                || !Objects.equals(item1.getDesignator(), item2.getDesignator())
                || !Objects.equals(item1.getFindNumber(), item2.getFindNumber());
    }

    private BOMItemChange toItemChange(BOMItem item1, BOMItem item2) {
        Part part = item1.getPartRevision().getPart();
        return BOMItemChange.builder()
                .partRevisionId(item1.getPartRevision().getId())
                .partNumber(part.getPartNumber())
                .partName(part.getName())
                .revision(item1.getPartRevision().getRevision())
                .bom1Quantity(item1.getQuantity())
                .bom2Quantity(item2.getQuantity())
                .bom1Designator(item1.getDesignator())
                .bom2Designator(item2.getDesignator())
                .bom1FindNumber(item1.getFindNumber())
                .bom2FindNumber(item2.getFindNumber())
                .build();
    }

    // ==================== CSV Export ====================

    @Override
    public String exportBOMToCsv(Long bomId) {
        BOM bom = findBOMOrThrow(bomId);
        List<BOMItem> items = bomItemRepository.findByBomId(bomId);

        StringBuilder csv = new StringBuilder();
        // Header
        csv.append("partNumber,partName,revision,quantity,designator,findNumber,isMounted,scrapFactor,comment,unitPrice,currency\n");

        // BOM metadata as comment lines
        csv.append("# BOM: ").append(bom.getName()).append("\n");
        csv.append("# Assembly: ").append(bom.getAssembly().getPartNumber()).append("\n");
        csv.append("# Status: ").append(bom.getStatus()).append("\n");
        csv.append("# Version: ").append(bom.getVersionNumber()).append("\n");

        for (BOMItem item : items) {
            PartRevision pr = item.getPartRevision();
            Part part = pr.getPart();
            csv.append(escapeCsv(part.getPartNumber())).append(",")
               .append(escapeCsv(part.getName())).append(",")
               .append(escapeCsv(pr.getRevision())).append(",")
               .append(item.getQuantity()).append(",")
               .append(escapeCsv(item.getDesignator())).append(",")
               .append(item.getFindNumber() != null ? item.getFindNumber() : "").append(",")
               .append(item.getIsMounted()).append(",")
               .append(item.getScrapFactor()).append(",")
               .append(escapeCsv(item.getComment())).append(",")
               .append(pr.getPrice() != null ? pr.getPrice() : "").append(",")
               .append(pr.getCurrency() != null ? pr.getCurrency() : "").append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ==================== CSV Import ====================

    @Override
    @Transactional
    public BOMDTO importBOMFromCsv(String csvData, Long assemblyId, String bomName) {
        Assembly assembly = assemblyRepository.findById(assemblyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Assembly not found with id: " + assemblyId));

        // Create the BOM
        BOM bom = BOM.builder()
                .name(bomName)
                .assembly(assembly)
                .status("DRAFT")
                .versionNumber(1)
                .build();
        BOM savedBom = bomRepository.save(bom);

        // Parse CSV lines (skip comment lines starting with # and the header)
        String[] lines = csvData.split("\n");
        boolean headerSkipped = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            if (!headerSkipped) {
                headerSkipped = true; // skip header row
                continue;
            }

            String[] fields = parseCsvLine(trimmed);
            if (fields.length < 3) continue; // need at least partNumber, partName, revision

            String partNumber = fields[0].trim();
            String revision = fields[2].trim();
            BigDecimal quantity = fields.length > 3 && !fields[3].trim().isEmpty()
                    ? new BigDecimal(fields[3].trim()) : BigDecimal.ONE;
            String designator = fields.length > 4 ? fields[4].trim() : null;
            Integer findNumber = fields.length > 5 && !fields[5].trim().isEmpty()
                    ? Integer.parseInt(fields[5].trim()) : null;
            Boolean isMounted = fields.length > 6 && !fields[6].trim().isEmpty()
                    ? Boolean.parseBoolean(fields[6].trim()) : true;
            BigDecimal scrapFactor = fields.length > 7 && !fields[7].trim().isEmpty()
                    ? new BigDecimal(fields[7].trim()) : BigDecimal.ZERO;
            String comment = fields.length > 8 ? fields[8].trim() : null;

            // Find the part by part number
            Optional<Part> partOpt = partRepository.findByPartNumber(partNumber);
            if (partOpt.isEmpty()) {
                continue; // skip unknown parts
            }

            // Find the part revision
            Optional<PartRevision> prOpt = partRevisionRepository
                    .findByPartIdAndRevisionAndIteration(partOpt.get().getId(), revision, 1);
            if (prOpt.isEmpty()) {
                // Try latest revision
                prOpt = partRevisionRepository.findByPartIdAndIsLatestRevisionTrue(partOpt.get().getId());
            }
            if (prOpt.isEmpty()) {
                continue;
            }

            BOMItem item = BOMItem.builder()
                    .bom(savedBom)
                    .partRevision(prOpt.get())
                    .quantity(quantity)
                    .designator(designator)
                    .findNumber(findNumber)
                    .isMounted(isMounted)
                    .scrapFactor(scrapFactor)
                    .comment(comment)
                    .build();
            bomItemRepository.save(item);
        }

        return bomMapper.toDTO(savedBom);
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++; // skip escaped quote
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    // ==================== BOM Snapshots ====================

    @Override
    @Transactional
    public BOMSnapshot createSnapshot(Long bomId, String label) {
        BOM bom = findBOMOrThrow(bomId);
        List<BOMItem> items = bomItemRepository.findByBomId(bomId);

        List<BOMSnapshotItem> snapshotItems = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;

        for (BOMItem item : items) {
            PartRevision pr = item.getPartRevision();
            Part part = pr.getPart();
            BigDecimal unitPrice = pr.getPrice() != null ? pr.getPrice() : BigDecimal.ZERO;
            BigDecimal lineCost = unitPrice.multiply(item.getQuantity());

            snapshotItems.add(BOMSnapshotItem.builder()
                    .partRevisionId(pr.getId())
                    .partNumber(part.getPartNumber())
                    .partName(part.getName())
                    .revision(pr.getRevision())
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .lineCost(lineCost)
                    .designator(item.getDesignator())
                    .findNumber(item.getFindNumber())
                    .isMounted(item.getIsMounted())
                    .scrapFactor(item.getScrapFactor())
                    .comment(item.getComment())
                    .build());

            totalCost = totalCost.add(lineCost);
        }

        BOMSnapshot snapshot = BOMSnapshot.builder()
                .id(snapshotIdGenerator.getAndIncrement())
                .bomId(bomId)
                .bomName(bom.getName())
                .snapshotLabel(label != null ? label : "Snapshot")
                .comments(bom.getComments())
                .snapshotDate(OffsetDateTime.now())
                .createdBy(bom.getUpdatedBy())
                .itemCount(items.size())
                .totalCost(totalCost.setScale(4, RoundingMode.HALF_UP))
                .items(snapshotItems)
                .build();

        snapshotStore.computeIfAbsent(bomId, k -> new ArrayList<>()).add(snapshot);
        return snapshot;
    }

    @Override
    public List<BOMSnapshot> listSnapshots(Long bomId) {
        findBOMOrThrow(bomId);
        return snapshotStore.getOrDefault(bomId, Collections.emptyList());
    }

    // ==================== BOM Cost Calculation ====================

    @Override
    public BigDecimal calculateBOMCost(Long bomId) {
        findBOMOrThrow(bomId);
        List<BOMItem> items = bomItemRepository.findByBomId(bomId);

        BigDecimal totalCost = BigDecimal.ZERO;
        for (BOMItem item : items) {
            PartRevision pr = item.getPartRevision();
            BigDecimal unitPrice = pr.getPrice() != null ? pr.getPrice() : BigDecimal.ZERO;
            BigDecimal quantity = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE;
            BigDecimal scrapFactor = item.getScrapFactor() != null ? item.getScrapFactor() : BigDecimal.ZERO;

            // Effective quantity = quantity * (1 + scrapFactor/100)
            BigDecimal effectiveQty = quantity.multiply(
                    BigDecimal.ONE.add(scrapFactor.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)));

            totalCost = totalCost.add(unitPrice.multiply(effectiveQty));
        }

        return totalCost.setScale(4, RoundingMode.HALF_UP);
    }
}
