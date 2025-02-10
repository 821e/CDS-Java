package com.cds.automation.model;

public class DeclarationData {
    private final String referenceId;
    private final String recipientAddress1;
    private final String recipientAddress2;
    private final String recipientCity;
    private final String recipientState;
    private final String recipientPostCode;
    private final String recipientCountry;
    private final String recipientName;
    private final String recipientTelephone;
    private final String recipientEmail;
    private final String itemDescription;
    private final String quantity;
    private final String weight;
    private final String itemValue;
    private final String currency;

    // Constructor
    public DeclarationData(String referenceId, String recipientAddress1, String recipientAddress2,
                          String recipientCity, String recipientState, String recipientPostCode,
                          String recipientCountry, String recipientName, String recipientTelephone,
                          String recipientEmail, String itemDescription, String quantity,
                          String weight, String itemValue, String currency) {
        this.referenceId = referenceId;
        this.recipientAddress1 = recipientAddress1;
        this.recipientAddress2 = recipientAddress2;
        this.recipientCity = recipientCity;
        this.recipientState = recipientState;
        this.recipientPostCode = recipientPostCode;
        this.recipientCountry = recipientCountry;
        this.recipientName = recipientName;
        this.recipientTelephone = recipientTelephone;
        this.recipientEmail = recipientEmail;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.weight = weight;
        this.itemValue = itemValue;
        this.currency = currency;
    }

    // Getters
    public String getReferenceId() { return referenceId; }
    public String getRecipientAddress1() { return recipientAddress1; }
    public String getRecipientAddress2() { return recipientAddress2; }
    public String getRecipientCity() { return recipientCity; }
    public String getRecipientState() { return recipientState; }
    public String getRecipientPostCode() { return recipientPostCode; }
    public String getRecipientCountry() { return recipientCountry; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientTelephone() { return recipientTelephone; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getItemDescription() { return itemDescription; }
    public String getQuantity() { return quantity; }
    public String getWeight() { return weight; }
    public String getItemValue() { return itemValue; }
    public String getCurrency() { return currency; }
}