package pl.edu.agh.mwo.invoice.product;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("0.00##", otherSymbols);
        DecimalFormat df1 = new DecimalFormat("0.#", otherSymbols);
        return "Nazwa: " + getName() + "; Cena jedn. netto [PLN]: " + df.format(getPrice())
                + "; Stawka VAT: " + df1.format(getTaxPercent().multiply(new BigDecimal("100")))
                + "%" + "; Akcyza [PLN]: " + df.format(exciseTax) + "; Cena jedn. brutto [PLN]: "
                + df.format(getPriceWithTax());
    }
}
