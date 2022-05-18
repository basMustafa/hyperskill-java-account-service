package account.service;

import account.exception.PaymentNotFoundException;
import account.model.payment.Payment;
import account.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import javax.transaction.Transactional;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void savePayments(List<Payment> payroll) {
        payroll.forEach(p -> {
            if (paymentRepository.findByEmailAndPeriod(p.getEmail(), p.getPeriod()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        });
        paymentRepository.saveAll(payroll);
    }

    @Transactional
    public void updatePayment(Payment paymentUpdate) {
        Payment payment = paymentRepository
                .findByEmailAndPeriod(paymentUpdate.getEmail(), paymentUpdate.getPeriod())
                .orElseThrow(PaymentNotFoundException::new);
        payment.setSalary(paymentUpdate.getSalary());
        paymentRepository.save(payment);
    }

    public Payment getPayment(String email, String period) {
        return paymentRepository.findByEmailAndPeriod(email, YearMonth.parse(period,
                        DateTimeFormatter.ofPattern("MM-yyyy")).atDay(1))
                .orElseThrow(PaymentNotFoundException::new);
    }
}
