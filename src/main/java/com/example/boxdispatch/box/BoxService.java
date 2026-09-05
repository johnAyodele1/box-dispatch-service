package com.example.boxdispatch.box;

import com.example.boxdispatch.box.dto.BatteryResponse;
import com.example.boxdispatch.box.dto.BoxResponse;
import com.example.boxdispatch.box.dto.CreateBoxRequest;
import com.example.boxdispatch.box.dto.LoadItemsRequest;
import com.example.boxdispatch.box.exception.BoxNotFoundException;
import com.example.boxdispatch.box.exception.DuplicateBoxTxrefException;
import com.example.boxdispatch.box.exception.DuplicateItemCodeException;
import com.example.boxdispatch.box.exception.WeightLimitExceededException;
import com.example.boxdispatch.item.Item;
import com.example.boxdispatch.item.dto.ItemResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BoxService {

    private final BoxRepository boxRepository;

    public BoxService(BoxRepository boxRepository) {
        this.boxRepository = boxRepository;
    }

    @Transactional
    public BoxResponse createBox(CreateBoxRequest request) {

        if (boxRepository.findByTxref(request.txref()).isPresent()) {
            throw new DuplicateBoxTxrefException(request.txref());
        }

        Box box = new Box(
            UUID.randomUUID(),
            request.txref(),
            request.weightLimitGrams(),
            request.batteryPercentage()
        );

        Box savedBox = boxRepository.save(box);

        return toResponse(savedBox);
    }

    @Transactional(readOnly = true)
    public List<BoxResponse> getAvailableBoxes() {
        return boxRepository
            .findAllByStateAndBatteryPercentageGreaterThanEqual(
                BoxState.IDLE,
                25
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BatteryResponse getBattery(String txref) {

        Box box = boxRepository.findByTxref(txref)
            .orElseThrow(() -> new BoxNotFoundException(txref));

        return new BatteryResponse(
            box.getTxref(),
            box.getBatteryPercentage()
        );
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getLoadedItems(String txref) {

        Box box = boxRepository.findByTxref(txref)
            .orElseThrow(() -> new BoxNotFoundException(txref));

        return box.getItems()
            .stream()
            .map(this::toItemResponse)
            .toList();
    }

    @Transactional
    public void loadItems(
        String txref,
        LoadItemsRequest request
    ) {
        Box box = boxRepository.findByTxrefForUpdate(txref)
            .orElseThrow(() -> new BoxNotFoundException(txref));

        // Validates:
        // 1. Box must be IDLE
        // 2. Battery must be >= 25%
        // 3. Changes state from IDLE -> LOADING
        box.startLoading();

        int existingWeight = box.getItems()
            .stream()
            .mapToInt(Item::getWeightGrams)
            .sum();

        int incomingWeight = request.items()
            .stream()
            .mapToInt(LoadItemsRequest.LoadItemRequest::weightGrams)
            .sum();

        int totalWeight = existingWeight + incomingWeight;

        if (totalWeight > box.getWeightLimitGrams()) {
            throw new WeightLimitExceededException(
                "Loading these items would exceed the box weight limit"
            );
        }

        Set<String> existingCodes = box.getItems()
            .stream()
            .map(Item::getCode)
            .collect(Collectors.toSet());

        Set<String> incomingCodes = new HashSet<>();

        for (LoadItemsRequest.LoadItemRequest itemRequest : request.items()) {

            if (existingCodes.contains(itemRequest.code())) {
                throw new DuplicateItemCodeException(
                    "Item code '" + itemRequest.code()
                        + "' already exists in this box"
                );
            }

            if (!incomingCodes.add(itemRequest.code())) {
                throw new DuplicateItemCodeException(
                    "Duplicate item code: '" + itemRequest.code() + "'"
                );
            }
        }

        for (LoadItemsRequest.LoadItemRequest itemRequest : request.items()) {

            Item item = new Item(
                UUID.randomUUID(),
                box,
                itemRequest.name(),
                itemRequest.weightGrams(),
                itemRequest.code()
            );

            box.addItem(item);
        }

        box.finishLoading();
    }

    private BoxResponse toResponse(Box box) {
        return new BoxResponse(
            box.getId(),
            box.getTxref(),
            box.getWeightLimitGrams(),
            box.getBatteryPercentage(),
            box.getState(),
            box.getCreatedAt(),
            box.getUpdatedAt()
        );
    }

    private ItemResponse toItemResponse(Item item) {
        return new ItemResponse(
            item.getId(),
            item.getName(),
            item.getWeightGrams(),
            item.getCode()
        );
    }
}
