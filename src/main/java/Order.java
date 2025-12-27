public class Order {
    public int orderId;
    public String user;
    public double amount;

    public Order() {
    }

    public Order(int orderId, String user, double amount) {
        this.orderId = orderId;
        this.user = user;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", user='" + user + '\'' +
                ", amount=" + amount +
                '}';
    }
}
