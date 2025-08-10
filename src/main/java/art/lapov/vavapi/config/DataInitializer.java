package art.lapov.vavapi.config;

import art.lapov.vavapi.model.ConnectorType;
import art.lapov.vavapi.model.Location;
import art.lapov.vavapi.model.Payment;
import art.lapov.vavapi.model.PricingInterval;
import art.lapov.vavapi.model.Reservation;
import art.lapov.vavapi.model.ReservationStatus;
import art.lapov.vavapi.model.Review;
import art.lapov.vavapi.model.Station;
import art.lapov.vavapi.model.User;
import art.lapov.vavapi.repository.LocationRepository;
import art.lapov.vavapi.repository.PaymentRepository;
import art.lapov.vavapi.repository.PricingIntervalRepository;
import art.lapov.vavapi.repository.ReservationRepository;
import art.lapov.vavapi.repository.ReviewRepository;
import art.lapov.vavapi.repository.StationRepository;
import art.lapov.vavapi.repository.UserRepository;
import art.lapov.vavapi.service.RatingService;
import lombok.AllArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final Faker faker;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final PaymentRepository paymentRepository;
    private final PricingIntervalRepository pricingIntervalRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final RatingService ratingService;


    @Override
    public void run(String... args) throws Exception {

        if (userRepository.count() == 0) {
            List<User> users = createUsers();
            userRepository.saveAll(users);

            List<Location> locations = createLocations(users);
            locationRepository.saveAll(locations);

            List<Station> stations = createStations(locations);
            stationRepository.saveAll(stations);

            List<Reservation> reservations = createReservations(users, stations);
            reservationRepository.saveAll(reservations);

            // CREATE REVIEWS ONLY FOR COMPLETED RESERVATIONS
            createReviewsForCompletedReservations();

            List<PricingInterval> pricingIntervals = createPricingIntervals(stations);
            pricingIntervalRepository.saveAll(pricingIntervals);

            // RECALCULATE RATINGS AFTER CREATING REVIEWS
            recalculateAllRatings();
        }
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        User admin = new User();
        admin.setEmail("admin@admin.com");
        admin.setPassword(passwordEncoder.encode("12345678"));
        admin.setRole("ROLE_ADMIN");
        admin.setFirstName(faker.name().firstName());
        admin.setLastName(faker.name().lastName());
        admin.setPhone(faker.phoneNumber().phoneNumber());
        admin.setAddress(faker.address().streetAddress());
        admin.setCity(faker.address().city());
        admin.setCountry("France");
        admin.setPostalCode(faker.number().numberBetween(10000, 99999));
        admin.setValidated(true);
        users.add(admin);

        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setEmail(faker.internet().emailAddress());
            user.setPassword(passwordEncoder.encode("11111111"));
            user.setRole("ROLE_USER");
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setPhone(faker.phoneNumber().phoneNumber());
            user.setAddress(faker.address().streetAddress());
            user.setCity(faker.address().city());
            user.setCountry("France");
            user.setPostalCode(faker.number().numberBetween(10000, 99999));
            user.setValidated(true);
            users.add(user);
        }

        return users;
    }

    private List<Location> createLocations(List<User> users) {
        List<Location> locations = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Location location = new Location();
            location.setName(faker.funnyName().name());
            location.setDescription(faker.lorem().paragraph());
            location.setAddress(faker.address().streetAddress());
            location.setCity(faker.address().city());
            location.setCountry("France");
            location.setPostalCode(faker.number().numberBetween(10000, 99999));
            location.setLatitude(faker.number().randomDouble(2, 48, 50));
            location.setLongitude(faker.number().randomDouble(2, 2, 4));
            location.setOwner(users.get(faker.random().nextInt(users.size())));
            locations.add(location);
        }

        return locations;
    }

    private List<Station> createStations(List<Location> locations) {
        List<Station> stations = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Station station = new Station();
            station.setMaxPowerWatt(faker.number().numberBetween(10000L, 22000L));
            station.setConnectorType(ConnectorType.TYPE2S);
            station.setEnabled(true);
            station.setDescription(faker.lorem().sentence());
            station.setLocation(locations.get(faker.random().nextInt(locations.size())));
            stations.add(station);
        }

        return stations;
    }

    private List<Reservation> createReservations(List<User> users, List<Station> stations) {
        List<Reservation> reservations = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            Payment payment = new Payment();
            payment.setAmountInCents(faker.number().numberBetween(1000, 5000));
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(faker.beer().name());
            Payment savedPayment = paymentRepository.save(payment);

            Reservation reservation = new Reservation();

            // Making some reservations complete to create reviews
            if (i < 8) {
                reservation.setStatus(ReservationStatus.COMPLETED);
                // Completed reservations in the past
                reservation.setStartDate(LocalDateTime.now().minusDays(faker.number().numberBetween(5, 15)));
                reservation.setEndDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 4)));
            } else {
                reservation.setStatus(ReservationStatus.values()[new Random().nextInt(ReservationStatus.values().length)]);
                reservation.setStartDate(LocalDateTime.now().plusDays(faker.number().numberBetween(1, 5)));
                reservation.setEndDate(LocalDateTime.now().plusDays(faker.number().numberBetween(6, 10)));
            }

            reservation.setTotalCostInCents(payment.getAmountInCents());
            reservation.setClient(users.get(faker.random().nextInt(users.size())));
            reservation.setStation(stations.get(faker.random().nextInt(stations.size())));
            reservation.setPayment(savedPayment);

            reservations.add(reservation);
        }

        return reservations;
    }

    private void createReviewsForCompletedReservations() {
        // Receiving completed reservations
        List<Reservation> completedReservations = reservationRepository.findByStatus(ReservationStatus.COMPLETED);

        // Create testimonials for 70% of completed reservations
        for (int i = 0; i < completedReservations.size() * 0.7; i++) {
            Reservation reservation = completedReservations.get(i);

            Review review = new Review();
            review.setRating(faker.number().numberBetween(1, 5));
            review.setComment(faker.lorem().sentence());
            review.setAuthor(reservation.getClient()); // Author = reservation client
            review.setReservation(reservation); // Connecting with the reservation

            reviewRepository.save(review);
        }
    }

    private List<PricingInterval> createPricingIntervals(List<Station> stations) {
        List<PricingInterval> pricingIntervals = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            PricingInterval pricingInterval = new PricingInterval();
            pricingInterval.setStartHour(LocalTime.of(10, 0));
            pricingInterval.setEndHour(LocalTime.of(22, 0));
            pricingInterval.setHourlyPriceInCents(faker.number().numberBetween(10, 50));
            pricingInterval.setStation(stations.get(faker.random().nextInt(stations.size())));
            pricingIntervals.add(pricingInterval);
        }

        return pricingIntervals;
    }

    private void recalculateAllRatings() {
        System.out.println("DataInitializing | We're starting to recalculate the ratings...");

        List<Station> allStations = stationRepository.findAll();
        for (Station station : allStations) {
            System.out.println("DataInitializing | Recalculating the station's rating: " + station.getId());
            ratingService.recalculateStationRating(station.getId());
        }

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            System.out.println("DataInitializing | Recalculating the user's rating: " + user.getId());
            ratingService.recalculateOwnerRating(user.getId());
        }

        System.out.println("The recalculation of the ratings is complete!");
    }
}
