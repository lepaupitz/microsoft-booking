package com.example.Authenticator;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

@Service
public class GraphService {


    public String GraphApiClient() {
        String clientId = "0591864c-76c4-45de-82b8-3dc60a3d84a4";
        String clientSecret = "vBL8Q~OywfBRSxUj0kG1QwGd.sqWx~Do2jDgIb6r";
        String tenantId = "e6438358-ebaa-4847-b7b4-a2995349390d";
        ConfidentialClientApplication app = null;
        try {
            ConfidentialClientApplication.builder(
                            clientId,
                            ClientCredentialFactory.createFromSecret(clientSecret))
                    .authority("https://login.microsoftonline.com/" + tenantId)
                    .build();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String scope = "https://graph.microsoft.com/.default";
        ClientCredentialParameters parameters = ClientCredentialParameters.builder(Set.of(scope)).build();
        String accessToken = null;
        try{
            accessToken = app.acquireToken(parameters).get().accessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return response.body();
    }
}
