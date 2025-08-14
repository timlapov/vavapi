package art.lapov.vavapi.service.receipt;

import art.lapov.vavapi.dto.ReservationDTO;
import art.lapov.vavapi.mapper.ReservationMapper;
import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReceiptFacade {
    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;
    private final HtmlToPdfReceiptService htmlToPdfReceiptService;

    public byte[] buildReceiptPdf(String reservationId, User me) {
        Reservation r = reservationService.getOwnedOrClientReservation(reservationId, me);
        ReservationDTO dto = reservationMapper.map(r);
        return htmlToPdfReceiptService.generateReceiptPdf(dto);
    }
}
