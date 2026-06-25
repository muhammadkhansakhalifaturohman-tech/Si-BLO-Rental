package com.siblo.rent.service;

import com.midtrans.httpclient.SnapApi;
import com.midtrans.httpclient.error.MidtransError;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MidtransService {

    public JSONObject createSnapTransaction(String orderId,
                                             long grossAmount,
                                             String customerName,
                                             String email) throws MidtransError {

        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", orderId);
        transactionDetails.put("gross_amount", grossAmount);

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("first_name", customerName);
        customerDetails.put("email", email);

        Map<String, Object> params = new HashMap<>();
        params.put("transaction_details", transactionDetails);
        params.put("customer_details", customerDetails);

        return SnapApi.createTransaction(params);
    }
}