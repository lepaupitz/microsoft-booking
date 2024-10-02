package com.example.Authenticator;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.*;
import com.microsoft.graph.requests.BookingAppointmentCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserCollectionPage;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class GraphService {

    @Autowired
    AuthProperties authProperties;



    private GraphServiceClient<Request> createGraphClient() {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(authProperties.getClientId())
                .clientSecret(authProperties.getClientSecret())
                .tenantId(authProperties.getTenantId())
                .build();

        String[] scopes = {"https://graph.microsoft.com/.default"};

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(Arrays.asList(scopes), clientSecretCredential);

        return GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();
    }

    public List<User> getAllUsers() {

        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(authProperties.getClientId())
                .clientSecret(authProperties.getClientSecret())
                .tenantId(authProperties.getTenantId())
                .build();

        String[] scopes = {"https://graph.microsoft.com/.default"};

        TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(Arrays.asList(scopes), clientSecretCredential);

        GraphServiceClient<Request> graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(authProvider)
                .buildClient();

        UserCollectionPage usersPage = graphClient.users()
                .buildRequest()
                .get();

        return usersPage.getCurrentPage();
    }



    public List<BookingAppointment> getAllAppointments(String bookingBusinessId) throws IOException, ExecutionException, InterruptedException {
        List<BookingAppointment> appointments = new ArrayList<>();

        BookingAppointmentCollectionPage appointmentPage = createGraphClient().solutions()
                .bookingBusinesses(bookingBusinessId)
                .appointments()
                .buildRequest()
                .get();

        appointments.addAll(appointmentPage.getCurrentPage());

        while (appointmentPage.getNextPage() != null) {
            appointmentPage = appointmentPage.getNextPage().buildRequest().get();
            appointments.addAll(appointmentPage.getCurrentPage());
        }

        return appointments;
    }

    public BookingAppointment createAppointment(String businessId, AppointmentDTO appointmentDTO) {

        GraphServiceClient<Request> graphClient = createGraphClient();
        BookingAppointment appointment = new BookingAppointment();

        appointment.startDateTime = new DateTimeTimeZone();
        appointment.startDateTime.dateTime = appointmentDTO.getStartDateTime().getDateTime();
        appointment.startDateTime.timeZone = appointmentDTO.getStartDateTime().getTimeZone();
        appointment.endDateTime = new DateTimeTimeZone();
        appointment.endDateTime.dateTime = appointmentDTO.getEndDateTime().getDateTime();
        appointment.endDateTime.timeZone = appointmentDTO.getEndDateTime().getTimeZone();

        appointment.serviceId = appointmentDTO.getServiceId();
        appointment.staffMemberIds = appointmentDTO.getStaffMemberIds();

        List<BookingCustomerInformationBase> customers = appointmentDTO.getCustomers().stream().map(customerDTO -> {
            BookingCustomerInformation customer = new BookingCustomerInformation();
            customer.oDataType = "#microsoft.graph.bookingCustomerInformation";
            customer.customerId = customerDTO.getCustomerId();
            customer.name = customerDTO.getName();
            customer.emailAddress = customerDTO.getEmailAddress();
            customer.phone = customerDTO.getPhone();
            customer.timeZone = customerDTO.getTimeZone();
            return customer;
        }).collect(Collectors.toList());
        appointment.customers = customers;

        appointment.isLocationOnline = true;

        BookingAppointment createdAppointment = graphClient
                .solutions()
                .bookingBusinesses(businessId)
                .appointments()
                .buildRequest()
                .post(appointment);

        return  createdAppointment;
    }

    public List<String> getStaffMemberIds (String businessId, String serviceId) {

        GraphServiceClient<Request> graphClient = createGraphClient();

        BookingService bookingService = graphClient
                .solutions()
                .bookingBusinesses(businessId)
                .services(serviceId)
                .buildRequest()
                .get();

        List<String> staffMemberIds = bookingService.staffMemberIds;

        return staffMemberIds;
    }

}
