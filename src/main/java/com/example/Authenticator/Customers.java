package com.example.Authenticator;

import lombok.Data;

@Data
public class Customers {

    private String customerId;
    private String name;
    private String emailAddress;
    private String phone;
    private String timeZone;
    private final String oDataType = "#microsoft.graph.bookingCustomerInformation";



}
