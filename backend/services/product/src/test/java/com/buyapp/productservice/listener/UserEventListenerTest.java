package com.buyapp.productservice.listener;

import com.buyapp.common.event.UserEvent;
import com.buyapp.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    @Mock
    private ProductService productService;

    private UserEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserEventListener(productService);
    }

    @Test
    void whenUserDeleted_thenDeletesAllProducts() {
        // Arrange: create a USER_DELETED event
        UserEvent event = new UserEvent();
        event.setUserId("seller-123");
        event.setEventType(UserEvent.EventType.USER_DELETED);
        event.setEmail("seller@example.com");
        event.setRole("seller");

        // Act: call handler directly (no Kafka needed)
        listener.handleUserEvent(event);

        // Assert: verify the service was called to delete all products for this seller
        verify(productService, times(1)).deleteProductsByUserId("seller-123");
    }

    @Test
    void whenUserUpdated_thenNoProductDeletion() {
        // Arrange
        UserEvent event = new UserEvent();
        event.setUserId("user-456");
        event.setEventType(UserEvent.EventType.USER_UPDATED);

        // Act
        listener.handleUserEvent(event);

        // Assert: verify no deletion happened
        verify(productService, never()).deleteProductsByUserId(anyString());
    }

    @Test
    void whenUserCreated_thenNoProductDeletion() {
        // Arrange
        UserEvent event = new UserEvent();
        event.setUserId("user-789");
        event.setEventType(UserEvent.EventType.USER_CREATED);

        // Act
        listener.handleUserEvent(event);

        // Assert: verify no deletion happened
        verify(productService, never()).deleteProductsByUserId(anyString());
    }

    @Test
    void whenUserDeletedAndServiceThrows_thenErrorIsLogged() {
        // Arrange
        UserEvent event = new UserEvent();
        event.setUserId("seller-error");
        event.setEventType(UserEvent.EventType.USER_DELETED);

        doThrow(new RuntimeException("Product deletion failed"))
                .when(productService).deleteProductsByUserId("seller-error");

        // Act: should not throw - error is caught and logged
        listener.handleUserEvent(event);

        // Assert: verify the service was still called despite the error
        verify(productService, times(1)).deleteProductsByUserId("seller-error");
    }
}
