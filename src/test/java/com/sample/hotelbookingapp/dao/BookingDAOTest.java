package com.sample.hotelbookingapp.dao;

import com.sample.hotelbookingapp.dao.BookingDAO;
import com.sample.hotelbookingapp.exception.BusinessException;
import com.sample.hotelbookingapp.model.BookingRequest;
import com.sample.hotelbookingapp.model.Room;
import com.sample.hotelbookingapp.service.BookingService;
import com.sample.hotelbookingapp.service.MailSender;
import com.sample.hotelbookingapp.service.PaymentService;
import com.sample.hotelbookingapp.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingDAOTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private PaymentService paymentServiceMock;
    @Mock
    private RoomService roomServiceMock;
    @Spy
    private BookingDAO bookingDAOMock;
    @Mock
    private MailSender mailSenderMock;

    @Test
    public void should_MakeBooking_When_InputOk() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);

        //when
        String bookingId = bookingService.makeBooking(bookingRequest);

        //then
        verify(bookingDAOMock).save(bookingRequest);
        System.out.println("bookingId: " + bookingId);
    }

    @Test
    public void should_CancelBooking_When_InputOk() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);
        bookingRequest.setRoomId("1.3");
        String bookingId = "1";

        //when
        doReturn(bookingRequest).when(bookingDAOMock).get(bookingId);

        //then
        bookingService.cancelBooking(bookingId);
        verify(roomServiceMock).unbookRoom(bookingRequest.getRoomId());
    }
}