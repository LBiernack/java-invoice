package pl.edu.agh.mwo.invoice.product;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public abstract class Product {
    public static final int discountMonth1 = 3;

    public static final int discountDay1 = 5;

    private final String name;

    private final BigDecimal price;

    private final BigDecimal taxPercent;

    protected final BigDecimal exciseTaxValue = new BigDecimal("5.56");

    // discountDate [month, day]
    Map<Integer, Integer> discountDate = Map.of(
            discountMonth1, discountDay1
    );

    protected Product(String name, BigDecimal price, BigDecimal tax) {
        if (name == null || name.equals("")
                || price == null || tax == null || tax.compareTo(new BigDecimal(0)) < 0
                || price.compareTo(new BigDecimal(0)) < 0) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.price = price;
        this.taxPercent = tax;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public BigDecimal getPriceWithTax() {
        return price.multiply(taxPercent).add(price);
    }

    public Map<Integer, Integer> getDiscountDate() {
        return discountDate;
    }

    public BigDecimal getDiscount(LocalDate actualDate) {

        BigDecimal discount = BigDecimal.ONE;

        for (int discountMonth : discountDate.keySet()) {
            int discountDay = discountDate.get(discountMonth);
            if (actualDate.getDayOfMonth() == discountDay
                    && actualDate.getMonthValue() == discountMonth) {
                discount = BigDecimal.ZERO;
                break;
            }
        }
        return discount;
    }

    @Override
    public String toString() {
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("0.00##", otherSymbols);
        DecimalFormat df1 = new DecimalFormat("0.#", otherSymbols);
        return "Nazwa: " + name + "; Cena jedn. netto [PLN]: " + df.format(price) + "; Stawka VAT: "
                + df1.format(taxPercent.multiply(new BigDecimal("100"))) + "%"
                + "; Cena jedn. brutto [PLN]: " + df.format(getPriceWithTax());
    }
}
