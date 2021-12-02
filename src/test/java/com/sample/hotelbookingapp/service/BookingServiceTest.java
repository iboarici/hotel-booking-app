package com.sample.hotelbookingapp.service;

import com.sample.hotelbookingapp.dao.BookingDAO;
import com.sample.hotelbookingapp.exception.BusinessException;
import com.sample.hotelbookingapp.model.BookingRequest;
import com.sample.hotelbookingapp.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private PaymentService paymentServiceMock;
    @Mock
    private RoomService roomServiceMock;
    @Mock
    private BookingDAO bookingDAOMock;
    @Mock
    private MailSender mailSenderMock;

    private List<Room> rooms;

    ArgumentCaptor<Double> doubleArgumentCaptor;

    @BeforeEach
    void setUp() {
        rooms = Arrays.asList(new Room("Room-1", 5), new Room("Room-2", 2), new Room("Room-3", 8));
        this.doubleArgumentCaptor = ArgumentCaptor.forClass(Double.class);
    }

    @Test
    void should_CalculateCorrectPrice_When_CorrectInput() {
        //Given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2021, 01, 01), LocalDate.of(2021, 01, 05), 2, false);
        double expected = 4 * 2 * 50.0;

        //When
        double actual = bookingService.calculatePrice(bookingRequest);

        //Then
        assertEquals(expected, actual);
    }

    @Test
    public void should_CountAvailablePlaces() {
        //given
        int expected = 0;

        //when
        int actual = bookingService.getAvailablePlaceCount();

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void should_CountAvailablePlaces_When_OneRoomAvailable() {
        //given
        int expected = 5;
        given(roomServiceMock.getAvailableRooms()).willReturn(rooms.subList(0,1));

        //when
        int actual = bookingService.getAvailablePlaceCount();

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void should_CountAvailablePlaces_When_MultipleRoomsAvailable() {
        //given
        given(roomServiceMock.getAvailableRooms()).willReturn(rooms);
        int expected = 15;

        //when
        int actual = bookingService.getAvailablePlaceCount();

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void should_CountAvailablePlaces_When_CalledMultipleTimes() {
        //given
        given(roomServiceMock.getAvailableRooms()).willReturn(rooms).willReturn(rooms.subList(0, 1));
        int expectedFirstCall = 15;
        int expectedSecondCall = 5;

        //when
        int actualFirst = bookingService.getAvailablePlaceCount();
        int actualSecond = bookingService.getAvailablePlaceCount();

        //then
        assertAll(() -> assertEquals(expectedFirstCall, actualFirst), () -> assertEquals(expectedSecondCall, actualSecond));
    }


    @Test
    public void should_ThrowException_When_NoRoomAvailable() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);
        given(roomServiceMock.findAvailableRoomId(bookingRequest)).willThrow(BusinessException.class);

        //when
        Executable executable = () -> bookingService.makeBooking(bookingRequest);

        //then
        assertThrows(BusinessException.class, executable);
    }

    @Test
    public void should_NotCompleteBooking_When_PriceTooHigh() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);
        given(paymentServiceMock.pay(eq(bookingRequest), anyDouble())).willThrow(BusinessException.class);

        //when
        Executable executable = () -> bookingService.makeBooking(bookingRequest);

        //then
        assertThrows(BusinessException.class, executable);
    }

    @Test
    public void should_InvokePayment_When_Prepaid() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);

        //when
        bookingService.makeBooking(bookingRequest);

        //then
        then(paymentServiceMock).should(times(1)).pay(any(), anyDouble());
        //verify(paymentServiceMock, times(1)).pay(any(), anyDouble());
        verifyNoMoreInteractions(paymentServiceMock);
    }

    @Test
    public void should_NotInvokePayment_When_NotPrepaid() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);

        //when
        bookingService.makeBooking(bookingRequest);

        //then
        then(paymentServiceMock).should(never()).pay(any(), anyDouble());
        verify(paymentServiceMock, never()).pay(any(), anyDouble());
    }

    @Test
    public void should_PayCorrectPrice_When_InputOk() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);

        //when
        bookingService.makeBooking(bookingRequest);

        //then
        then(paymentServiceMock).should(times(1)).pay(eq(bookingRequest), doubleArgumentCaptor.capture());
        //verify(paymentServiceMock, times(1)).pay(eq(bookingRequest), doubleArgumentCaptor.capture());
        double capturedArgument = doubleArgumentCaptor.getValue();
        assertEquals(400.0, capturedArgument);
    }

    @Test
    public void should_PayCorrectPrices_When_MultipleCalls() {
        //given
        BookingRequest bookingRequest1 = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);
        BookingRequest bookingRequest2 = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 02), 2, true);
        List<Double> expectedValues = Arrays.asList(400.0, 100.0);

        //when
        bookingService.makeBooking(bookingRequest1);
        bookingService.makeBooking(bookingRequest2);

        //then
        then(paymentServiceMock).should(times(2)).pay(any(), doubleArgumentCaptor.capture());
        List<Double> capturedArguments = doubleArgumentCaptor.getAllValues();
        assertEquals(expectedValues, capturedArguments);
    }
}