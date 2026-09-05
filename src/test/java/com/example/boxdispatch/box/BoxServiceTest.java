package com.example.boxdispatch.box;

import com.example.boxdispatch.box.dto.CreateBoxRequest;
import com.example.boxdispatch.box.dto.LoadItemsRequest;
import com.example.boxdispatch.box.exception.DuplicateBoxTxrefException;
import com.example.boxdispatch.box.exception.DuplicateItemCodeException;
import com.example.boxdispatch.box.exception.InsufficientBatteryException;
import com.example.boxdispatch.box.exception.InvalidBoxStateException;
import com.example.boxdispatch.box.exception.WeightLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    private BoxService boxService;

    @BeforeEach
    void setUp() {
        boxService = new BoxService(boxRepository);
    }

    @Test
    void createsBoxWhenTxrefIsUnique() {
        CreateBoxRequest request =
            new CreateBoxRequest("BOX-TEST", 500, 80);

        when(boxRepository.findByTxref("BOX-TEST"))
            .thenReturn(Optional.empty());

        when(boxRepository.save(any(Box.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = boxService.createBox(request);

        assertEquals("BOX-TEST", response.txref());
        assertEquals(500, response.weightLimitGrams());
        assertEquals(80, response.batteryPercentage());
        assertEquals(BoxState.IDLE, response.state());

        verify(boxRepository).save(any(Box.class));
    }

    @Test
    void rejectsDuplicateBoxTxref() {
        Box existing = new Box(
            UUID.randomUUID(),
            "BOX-001",
            500,
            80
        );

        when(boxRepository.findByTxref("BOX-001"))
            .thenReturn(Optional.of(existing));

        CreateBoxRequest request =
            new CreateBoxRequest("BOX-001", 500, 80);

        assertThrows(
            DuplicateBoxTxrefException.class,
            () -> boxService.createBox(request)
        );

        verify(boxRepository, never()).save(any(Box.class));
    }

    @Test
    void rejectsLoadingWhenBatteryIsBelowTwentyFivePercent() {
        Box box = new Box(
            UUID.randomUUID(),
            "LOW-BATTERY",
            500,
            24
        );

        when(boxRepository.findByTxrefForUpdate("LOW-BATTERY"))
            .thenReturn(Optional.of(box));

        LoadItemsRequest request = items(
            "ITEM_001",
            100
        );

        assertThrows(
            InsufficientBatteryException.class,
            () -> boxService.loadItems("LOW-BATTERY", request)
        );

        assertEquals(BoxState.IDLE, box.getState());
    }

    @Test
    void acceptsLoadingWhenBatteryIsExactlyTwentyFivePercent() {
        Box box = new Box(
            UUID.randomUUID(),
            "BATTERY-25",
            500,
            25
        );

        when(boxRepository.findByTxrefForUpdate("BATTERY-25"))
            .thenReturn(Optional.of(box));

        boxService.loadItems(
            "BATTERY-25",
            items("ITEM_001", 100)
        );

        assertEquals(BoxState.LOADED, box.getState());
        assertEquals(1, box.getItems().size());
        assertEquals(100, box.getItems().get(0).getWeightGrams());
    }

    @Test
    void rejectsLoadingWhenBoxIsNotIdle() {
        Box box = new Box(
            UUID.randomUUID(),
            "LOADED-BOX",
            500,
            80
        );

        box.startLoading();
        box.finishLoading();

        when(boxRepository.findByTxrefForUpdate("LOADED-BOX"))
            .thenReturn(Optional.of(box));

        assertThrows(
            InvalidBoxStateException.class,
            () -> boxService.loadItems(
                "LOADED-BOX",
                items("ITEM_001", 100)
            )
        );
    }

    @Test
    void rejectsItemsThatExceedWeightLimit() {
        Box box = new Box(
            UUID.randomUUID(),
            "WEIGHT-TEST",
            500,
            80
        );

        when(boxRepository.findByTxrefForUpdate("WEIGHT-TEST"))
            .thenReturn(Optional.of(box));

        assertThrows(
            WeightLimitExceededException.class,
            () -> boxService.loadItems(
                "WEIGHT-TEST",
                items("ITEM_001", 501)
            )
        );

        assertEquals(0, box.getItems().size());
    }

    @Test
    void rejectsDuplicateCodesWithinSameRequest() {
        Box box = new Box(
            UUID.randomUUID(),
            "DUPLICATE-REQUEST",
            500,
            80
        );

        when(boxRepository.findByTxrefForUpdate("DUPLICATE-REQUEST"))
            .thenReturn(Optional.of(box));

        LoadItemsRequest request = new LoadItemsRequest(
            java.util.List.of(
                new LoadItemsRequest.LoadItemRequest(
                    "ItemA",
                    50,
                    "DUP_001"
                ),
                new LoadItemsRequest.LoadItemRequest(
                    "ItemB",
                    50,
                    "DUP_001"
                )
            )
        );

        assertThrows(
            DuplicateItemCodeException.class,
            () -> boxService.loadItems(
                "DUPLICATE-REQUEST",
                request
            )
        );

        assertEquals(0, box.getItems().size());
    }

   @Test
void rejectsCodeAlreadyPresentInBox() {
    Box box = new Box(
        UUID.randomUUID(),
        "EXISTING-CODE",
        500,
        80
    );

    box.addItem(
        new com.example.boxdispatch.item.Item(
            UUID.randomUUID(),
            box,
            "ExistingItem",
            50,
            "ITEM_001"
        )
    );

    when(boxRepository.findByTxrefForUpdate("EXISTING-CODE"))
        .thenReturn(Optional.of(box));

    assertThrows(
        DuplicateItemCodeException.class,
        () -> boxService.loadItems(
            "EXISTING-CODE",
            items("ITEM_001", 50)
        )
    );

    assertEquals(1, box.getItems().size());
}
    @Test
    void successfulLoadAddsAllItemsAndMarksBoxLoaded() {
        Box box = new Box(
            UUID.randomUUID(),
            "SUCCESSFUL-LOAD",
            500,
            80
        );

        when(boxRepository.findByTxrefForUpdate("SUCCESSFUL-LOAD"))
            .thenReturn(Optional.of(box));

        LoadItemsRequest request = new LoadItemsRequest(
            java.util.List.of(
                new LoadItemsRequest.LoadItemRequest(
                    "ItemA",
                    100,
                    "ITEM_A"
                ),
                new LoadItemsRequest.LoadItemRequest(
                    "ItemB",
                    150,
                    "ITEM_B"
                )
            )
        );

        boxService.loadItems("SUCCESSFUL-LOAD", request);

        assertEquals(BoxState.LOADED, box.getState());
        assertEquals(2, box.getItems().size());
        assertEquals(100, box.getItems().get(0).getWeightGrams());
        assertEquals(150, box.getItems().get(1).getWeightGrams());
    }

    private LoadItemsRequest items(String code, int weight) {
        return new LoadItemsRequest(
            java.util.List.of(
                new LoadItemsRequest.LoadItemRequest(
                    "TestItem",
                    weight,
                    code
                )
            )
        );
    }
}
