package pl.edu.agh.mwo.invoice.product;

import java.math.BigDecimal;

public class BottleOfWine extends Product {

    private final BigDecimal exciseTax;

    public BottleOfWine(String name, BigDecimal price) {
        super(name, price, new BigDecimal("0.23"));
        this.exciseTax = exciseTaxValue;
    }

    @Override
    public BigDecimal getPriceWithTax() {
        return this.getPrice().multiply(this.getTaxPercent()).add(this.getPrice()).add(exciseTax);
    }

    @Override
    public String toString() {
        return "Nazwa: " + getName() + ", Cena jedn. netto [PLN]: " + getPrice() + ", Stawka VAT: "
                + getTaxPercent().multiply(new BigDecimal("100")) + "%" + ", Akcyza: "
                + exciseTax + ", Cena jedn. brutto [PLN]: " + getPriceWithTax();
    }

}
