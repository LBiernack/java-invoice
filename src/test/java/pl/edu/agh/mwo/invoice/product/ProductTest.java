package pl.edu.agh.mwo.invoice.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ProductTest {
    @Test
    public void testProductNameIsCorrect() {
        Product product = new OtherProduct("buty", new BigDecimal("100.0"));
        Assert.assertEquals("buty", product.getName());
    }

    @Test
    public void testProductPriceAndTaxWithDefaultTax() {
        Product product = new OtherProduct("Ogorki", new BigDecimal("100.0"));
        Assert.assertThat(new BigDecimal("100"), Matchers.comparesEqualTo(product.getPrice()));
        Assert.assertThat(new BigDecimal("0.23"), Matchers.comparesEqualTo(product.getTaxPercent()));
    }

    @Test
    public void testProductPriceAndTaxWithDairyProduct() {
        Product product = new DairyProduct("Szarlotka", new BigDecimal("100.0"));
        Assert.assertThat(new BigDecimal("100"), Matchers.comparesEqualTo(product.getPrice()));
        Assert.assertThat(new BigDecimal("0.08"), Matchers.comparesEqualTo(product.getTaxPercent()));
    }

    @Test
    public void testPriceWithTax() {
        Product product = new DairyProduct("Oscypek", new BigDecimal("100.0"));
        Assert.assertThat(new BigDecimal("108"), Matchers.comparesEqualTo(product.getPriceWithTax()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProductWithNullName() {
        new OtherProduct(null, new BigDecimal("100.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProductWithEmptyName() {
        new TaxFreeProduct("", new BigDecimal("100.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProductWithNullPrice() {
        new DairyProduct("Banany", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProductWithNegativePrice() {
        new TaxFreeProduct("Mandarynki", new BigDecimal("-1.00"));
    }

    @Test
    public void testPrintingInformationWithTaxFreeProduct() {
        Product product = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        String textInformation = "Nazwa: Owoce; Cena jedn. netto [PLN]: 200,00; Stawka VAT: 0%; Cena jedn. brutto [PLN]: 200,00";
        Assert.assertEquals(textInformation, product.toString());
    }

    @Test
    public void testPrintingInformationWithDairyProduct() {
        Product product = new DairyProduct("Szarlotka", new BigDecimal("100"));
        String textInformation = "Nazwa: Szarlotka; Cena jedn. netto [PLN]: 100,00; Stawka VAT: 8%; Cena jedn. brutto [PLN]: 108,00";
        Assert.assertEquals(textInformation, product.toString());
    }

    @Test
    public void testExciseTaxValueIsCorrect() {
        Product product = new BottleOfWine("Wino", new BigDecimal("10.0"));
        Assert.assertThat(new BigDecimal("5.56"), Matchers.comparesEqualTo(product.exciseTaxValue));
    }

    @Test
    public void testProductPriceAndTaxWithBottleOfWineProduct() {
        Product product = new BottleOfWine("Wino", new BigDecimal("10.0"));
        Assert.assertThat(new BigDecimal("10.0"), Matchers.comparesEqualTo(product.getPrice()));
        Assert.assertThat(new BigDecimal("0.23"), Matchers.comparesEqualTo(product.getTaxPercent()));
        Assert.assertThat(new BigDecimal("17.86"), Matchers.comparesEqualTo(product.getPriceWithTax()));
    }

    @Test
    public void testBottleOfWineProductHasNoDiscount() {
        LocalDate discountDay = LocalDate.of(2024, 03, 05);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(discountDay);
            Product product = new BottleOfWine("Wino", new BigDecimal("10.0"));
            Assert.assertThat(new BigDecimal("10.0"), Matchers.comparesEqualTo(product.getPrice()));
            Assert.assertThat(new BigDecimal("0.23"), Matchers.comparesEqualTo(product.getTaxPercent()));
            Assert.assertThat(new BigDecimal("17.86"), Matchers.comparesEqualTo(product.getPriceWithTax()));
        }
    }

    @Test
    public void testPrintingInformationWithBottleOfWineProduct() {
        Product product = new BottleOfWine("Wino", new BigDecimal("10"));
        String textInformation = "Nazwa: Wino; Cena jedn. netto [PLN]: 10,00; Stawka VAT: 23%; Akcyza [PLN]: 5,56; Cena jedn. brutto [PLN]: 17,86";
        Assert.assertEquals(textInformation, product.toString());
    }

    @Test
    public void testDiscountDayForExciseTaxProductsIsCorrect() {
        Product product = new BottleOfWine("Wino", new BigDecimal("10"));

        Map<Integer, Integer> discountDate = Map.of(
                3, 5
        );
        Assert.assertEquals(discountDate, product.getDiscountDate());
    }

    @Test
    public void testGetDiscountMethod() {
        Product product = new BottleOfWine("Wino", new BigDecimal("10"));
        LocalDate normalDate1 = LocalDate.of(2024, 03, 04);
        LocalDate normalDate2 = LocalDate.of(2024, 02, 05);
        LocalDate discountDay1 = LocalDate.of(2024, 03, 05);
        LocalDate discountDay2 = LocalDate.of(2025, 03, 05);
        Assert.assertThat(BigDecimal.ONE, Matchers.comparesEqualTo(product.getDiscount(normalDate1)));
        Assert.assertThat(BigDecimal.ONE, Matchers.comparesEqualTo(product.getDiscount(normalDate2)));
        Assert.assertThat(BigDecimal.ZERO, Matchers.comparesEqualTo(product.getDiscount(discountDay1)));
        Assert.assertThat(BigDecimal.ZERO, Matchers.comparesEqualTo(product.getDiscount(discountDay2)));
    }

    @Test
    public void testProductPriceAndTaxWithFuelCanisterProductNoDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 06);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Product product = new FuelCanister("Benzyna", new BigDecimal("6"));
            Assert.assertThat(new BigDecimal("6"), Matchers.comparesEqualTo(product.getPrice()));
            Assert.assertThat(new BigDecimal("0.23"), Matchers.comparesEqualTo(product.getTaxPercent()));
            Assert.assertThat(new BigDecimal("12.94"), Matchers.comparesEqualTo(product.getPriceWithTax()));
        }
    }

    @Test
    public void testProductPriceAndTaxWithFuelCanisterProductDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 05);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Product product = new FuelCanister("Benzyna", new BigDecimal("6"));
            Assert.assertThat(new BigDecimal("6"), Matchers.comparesEqualTo(product.getPrice()));
            Assert.assertThat(new BigDecimal("0.23"), Matchers.comparesEqualTo(product.getTaxPercent()));
            Assert.assertThat(new BigDecimal("7.38"), Matchers.comparesEqualTo(product.getPriceWithTax()));
        }
    }

    @Test
    public void testProductPriceAndTaxWithFuelCanisterProductDiscountEveryYear() {
        LocalDate noDiscountDay = LocalDate.of(2025, 03, 05);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Product product = new FuelCanister("Benzyna", new BigDecimal("6"));
            Assert.assertThat(new BigDecimal("6"), Matchers.comparesEqualTo(product.getPrice()));
            Assert.assertThat(new BigDecimal("0.23"), Matchers.comparesEqualTo(product.getTaxPercent()));
            Assert.assertThat(new BigDecimal("7.38"), Matchers.comparesEqualTo(product.getPriceWithTax()));
        }
    }

    @Test
    public void testPrintingInformationFuelCanisterProductNoDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 06);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Product product = new FuelCanister("Benzyna", new BigDecimal("6"));
            String textInformation = "Nazwa: Benzyna; Cena jedn. netto [PLN]: 6,00; Stawka VAT: 23%; Akcyza [PLN]: 5,56; Cena jedn. brutto [PLN]: 12,94";
            Assert.assertEquals(textInformation, product.toString());
        }
    }

    @Test
    public void testPrintingInformationFuelCanisterProductDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 05);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Product product = new FuelCanister("Benzyna", new BigDecimal("6"));
            String textInformation = "Nazwa: Benzyna; Cena jedn. netto [PLN]: 6,00; Stawka VAT: 23%; Akcyza [PLN]: 0,00; Cena jedn. brutto [PLN]: 7,38";
            Assert.assertEquals(textInformation, product.toString());
        }
    }
}

