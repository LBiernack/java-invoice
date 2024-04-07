package pl.edu.agh.mwo.invoice;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

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
        } else {
            for (Product existingProduct : products.keySet()) {
                if (compareProductObject(existingProduct, product)) {
                    products.put(existingProduct, products.get(existingProduct) + quantity);
                    return;
                }
            }
            products.put(product, quantity);
        }
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

    public Map<Product, Integer> getProducts() {
        return products;
    }

    public void printInvoice() {
        TreeMap<String, Product> productsSorted = new TreeMap<>();
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("0.00##", otherSymbols);

        for (Product product : products.keySet()) {
            productsSorted.put(product.getName(), product);
        }

        System.out.print("Faktura nr: " + invoiceNumber + "; data wystawienia: "
                + this.creationDate + "\n");
        for (String productName : productsSorted.keySet()) {
            Product product = productsSorted.get(productName);
            System.out.print(product + "; Liczba: " + products.get(product)
                    + "; Wartość brutto [PLN]: "
                    + df.format(product.getPriceWithTax()
                    .multiply(new BigDecimal(products.get(product))))
                    + "\n");
        }
        System.out.print("Liczba pozycji: " + products.size() + "\n");
        System.out.print("Razem: Wartość Netto [PLN]: " + df.format(this.getNetTotal())
                + "; Wartość VAT + Akcyza [PLN]: " + df.format(this.getTaxTotal())
                + "; Wartość Brutto [PLN]: " + df.format(this.getGrossTotal()));
    }

    public boolean compareProductObject(Product product1, Product product2) {
        String productName1 = product1.getName();
        BigDecimal productPrice1 = product1.getPrice();
        BigDecimal productTax1 = product1.getTaxPercent();

        String productName2 = product2.getName();
        BigDecimal productPrice2 = product2.getPrice();
        BigDecimal productTax2 = product2.getTaxPercent();

        Boolean condition1 = productName1.equals(productName2);
        Boolean condition2 = productPrice1.equals(productPrice2);
        Boolean condition3 = productTax1.equals(productTax2);
        Boolean condition4 = product1.getClass().equals(product2.getClass());

        return condition1 && condition2 && condition3 && condition4;
    }
}
