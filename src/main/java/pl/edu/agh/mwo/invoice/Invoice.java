package pl.edu.agh.mwo.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import pl.edu.agh.mwo.invoice.product.Product;

public class Invoice {
    private Map<Product, Integer> products = new HashMap<Product, Integer>();
    private final LocalDate creationDate;
    private static int invoiceQuantity;
    private final int invoiceNumber;

    public Invoice() {
        creationDate = LocalDate.now();
        invoiceQuantity++;
        String creationDateFotmatDayMonthYear = String.valueOf(creationDate.getDayOfMonth())
                + String.valueOf(creationDate.getMonthValue())
                + String.valueOf(creationDate.getYear());
        this.invoiceNumber = Integer.parseInt(invoiceQuantity + creationDateFotmatDayMonthYear);
    }

    public void addProduct(Product product) {
        addProduct(product, 1);
    }

    public void addProduct(Product product, Integer quantity) {
        if (product == null || quantity <= 0) {
            throw new IllegalArgumentException();
        }
        products.put(product, quantity);
    }

    public BigDecimal getNetTotal() {
        BigDecimal totalNet = BigDecimal.ZERO;
        for (Product product : products.keySet()) {
            BigDecimal quantity = new BigDecimal(products.get(product));
            totalNet = totalNet.add(product.getPrice().multiply(quantity));
        }
        return totalNet;
    }

    public BigDecimal getTaxTotal() {
        return getGrossTotal().subtract(getNetTotal());
    }

    public BigDecimal getGrossTotal() {
        BigDecimal totalGross = BigDecimal.ZERO;
        for (Product product : products.keySet()) {
            BigDecimal quantity = new BigDecimal(products.get(product));
            totalGross = totalGross.add(product.getPriceWithTax().multiply(quantity));
        }
        return totalGross;
    }

    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void printInvoice() {
        System.out.print("Faktura nr: " + invoiceNumber + ", data wystawienia: "
                + this.creationDate + "\n");
        for (Product product : products.keySet()) {
            System.out.print(product + ", Liczba: " + products.get(product)
                    + ", Wartość brutto [PLN]: "
                    + product.getPriceWithTax().multiply(new BigDecimal(products.get(product)))
                    + "\n");
        }
        System.out.print("Liczba pozycji: " + products.size() + "\n");
        System.out.print("Razem: Wartość Netto [PLN]: " + this.getNetTotal()
                + ", Wartość VAT + Akcyza [PLN]: " + this.getTaxTotal()
                + ", Wartość Brutto [PLN]: " + this.getGrossTotal());
    }
}
