package com.example.boxdispatch.box;

import com.example.boxdispatch.box.dto.BatteryResponse;
import com.example.boxdispatch.box.dto.BoxResponse;
import com.example.boxdispatch.box.dto.CreateBoxRequest;
import com.example.boxdispatch.box.dto.LoadItemsRequest;
import com.example.boxdispatch.item.dto.ItemResponse;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boxes")
public class BoxController {

    private final BoxService boxService;

    public BoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    @PostMapping
    public ResponseEntity<BoxResponse> createBox(
        @Valid @RequestBody CreateBoxRequest request
    ) {
        BoxResponse response = boxService.createBox(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<BoxResponse>> getAvailableBoxes() {
        return ResponseEntity.ok(boxService.getAvailableBoxes());
    }
    @GetMapping("/{txref}/battery")
    public ResponseEntity<BatteryResponse> getBattery(
        @PathVariable String txref
    ) {
        return ResponseEntity.ok(boxService.getBattery(txref));
    }

    @GetMapping("/{txref}/items")
    public ResponseEntity<List<ItemResponse>> getLoadedItems(
        @PathVariable String txref
    ) {
        return ResponseEntity.ok(boxService.getLoadedItems(txref));
    }

    @PostMapping("/{txref}/items")
    public ResponseEntity<Void> loadItems(
        @PathVariable String txref,
        @Valid @RequestBody LoadItemsRequest request
    ) {
        boxService.loadItems(txref, request);

        return ResponseEntity.noContent().build();
    }
}
