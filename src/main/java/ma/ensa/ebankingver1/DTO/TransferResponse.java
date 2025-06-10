package ma.ensa.ebankingver1.DTO;

// DTO pour la réponse de virement
public  class TransferResponse {
    private boolean success;
    private String message;
    private String transactionId;
    private double newBalance;

    public TransferResponse(boolean success, String message, String transactionId, double newBalance) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
        this.newBalance = newBalance;
    }

    // Getters et setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public double getNewBalance() { return newBalance; }
    public void setNewBalance(double newBalance) { this.newBalance = newBalance; }
}