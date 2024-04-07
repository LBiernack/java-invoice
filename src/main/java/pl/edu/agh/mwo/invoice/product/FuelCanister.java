package pl.edu.agh.mwo.invoice.product;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class FuelCanister extends Product {

    private final BigDecimal exciseTax;

    public FuelCanister(String name, BigDecimal price) {
        super(name, price, new BigDecimal("0.23"));
        this.exciseTax = exciseTaxValue.multiply(getDiscount(LocalDate.now()));
    }

    @Override
    public BigDecimal getPriceWithTax() {
        return this.getPrice().multiply(this.getTaxPercent()).add(this.getPrice()).add(exciseTax);
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.00##");
        DecimalFormat df1 = new DecimalFormat("0.#");
        return "Nazwa: " + getName() + "; Cena jedn. netto [PLN]: " + df.format(getPrice())
                + "; Stawka VAT: " + df1.format(getTaxPercent().multiply(new BigDecimal("100")))
                + "%" + "; Akcyza [PLN]: " + df.format(exciseTax) + "; Cena jedn. brutto [PLN]: "
                + df.format(getPriceWithTax());
    }
}
