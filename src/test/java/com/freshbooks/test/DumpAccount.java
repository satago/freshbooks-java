package com.freshbooks.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.OAuthConfig;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.oauth.OAuth10aServiceImpl;
import org.scribe.services.PlaintextSignatureService;
import org.scribe.services.SignatureService;

import com.freshbooks.ApiConnection;
import com.freshbooks.ApiException;
import com.freshbooks.OAuthApiConnection;
import com.freshbooks.model.Category;
import com.freshbooks.model.Client;
import com.freshbooks.model.Expense;
import com.freshbooks.model.Invoice;
import com.freshbooks.model.Item;
import com.freshbooks.model.Payment;
import com.freshbooks.model.Recurring;

public class DumpAccount {

    /**
     * @param args
     * @throws ParseException 
     */
    public static void main(String[] args) throws ParseException {

        String apiHost = "https://satago" + ".freshbooks.com/api/2.1/xml-in";
        String apiKey = "";
        String apiSecret = "";
        String userAgent = "satago";
      

        ServiceBuilder br = new ServiceBuilder();

        Api api = new DefaultApi10a()
        {

            @Override
            public SignatureService getSignatureService()
            {
                return new PlaintextSignatureService();
            }

            private String getBaseUrl()
            {
                StringBuilder builder = new StringBuilder();
                builder.append("https://")
                        .append("satago")
                        .append(".freshbooks.com/oauth");

                return builder.toString();
            }
            @Override
            public String getRequestTokenEndpoint()
            {

                return getBaseUrl() + "/oauth_request.php";
            }

            @Override
            public String getAccessTokenEndpoint()
            {
                return getBaseUrl() + "/oauth_access.php";
            }

            @Override
            public String getAuthorizationUrl(Token requestToken)
            {
                return getBaseUrl() + "/oauth_authorize.php?"
                        + "oauth_token=" + requestToken.getToken();
            }
        };

        br.apiKey("consumer_key").apiSecret("consumer_secret").provider(api);

        OAuth10aServiceImpl service = (OAuth10aServiceImpl) br.build();
        try {
            OAuthApiConnection con = new OAuthApiConnection(service, new Token(apiKey, apiSecret), apiHost, userAgent);
            con.setDebug(true);

            try {
                for(Client client : con.listClients(null, null, null)) {
                    System.out.println("Found client " + client.getFirstName() + " " + client.getLastName() + " at "
                            + client.getOrganization());
                    //con.getClient(client.getId());
//                  con.deleteClient(client.getId());
                }
                System.out.println("Invoices");
                for (Invoice invoice : con.listInvoices(25, new Date(2013, 5, 23, 0, 0), null, null, null))
                {

                    System.out.println("Found invoice " + invoice.getId() + " with amount " + invoice.getAmount());
                   // invoice.
                    //con.getInvoice(invoice.getId());
//                  con.deleteInvoice(invoice.getId());
                }
                for(Payment payment : con.listPayments(null, null, null, null)) {
                    System.out.println("Found payment " + payment.getId() + " on invoice " + payment.getInvoiceId() + " with amount "
                            + payment.getAmount());
//                  con.deletePayment(payment.getId());
                }
                for(Recurring recurring: con.listRecurrings(null, null)) {
                    System.out.println("Found invoice " + recurring.getId() + " with amount " + recurring.getAmount());
                  //con.getInvoice(invoice.getId());
//                con.deleteRecurring(recurring.getId());
              }
                HashMap<Long,String> categoryNames = new HashMap<Long, String>();
                for(Category category : con.listCategories()) {
                    System.out.println("Found category " + category.getId() + ": " + category.getName());
                    //categoryNames.put(category.getId(), category.getName());
                }
                for(Expense expense : con.listExpenses(null, new Date(109,0,1), null, null, null, null)) {
                    expense = con.getExpense(expense.getId());
                    System.out.println("Found expense " + expense.getId() + " with amount " + expense.getAmount() + " and category "
                            + categoryNames.get(expense.getCategoryId()));
                }
                for (Item item : con.listItems(2))
                {
                    System.out.println("Found item " + item.getId() + " with name " + item.getName() + " and description "
                            + item.getDescription());
//                  con.deleteItem(item.getId());
                }
            } catch(Error e) {
                if(e.getCause() instanceof ApiException) {
                    throw (ApiException)e.getCause();
                }
                if(e.getCause() instanceof IOException) {
                    throw (IOException)e.getCause();
                }
            }
            
            
        } catch (MalformedURLException e) {
            System.err.println(apiHost+" is not a valid URL ("+e.getLocalizedMessage()+")");
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
