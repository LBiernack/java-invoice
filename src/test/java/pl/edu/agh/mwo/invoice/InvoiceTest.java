package pl.edu.agh.mwo.invoice;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import pl.edu.agh.mwo.invoice.Invoice;
import pl.edu.agh.mwo.invoice.product.*;

public class InvoiceTest {
    private Invoice invoice;

    private Invoice invoice1;

    private final PrintStream standardOut = System.out;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @Before
    public void createEmptyInvoiceForTheTest() {
        invoice = new Invoice();
        invoice1 = new Invoice();
    }

    @Test
    public void testEmptyInvoiceHasEmptySubtotal() {
        Assert.assertThat(BigDecimal.ZERO, Matchers.comparesEqualTo(invoice.getNetTotal()));
    }

    @Test
    public void testEmptyInvoiceHasEmptyTaxAmount() {
        Assert.assertThat(BigDecimal.ZERO, Matchers.comparesEqualTo(invoice.getTaxTotal()));
    }

    @Test
    public void testEmptyInvoiceHasEmptyTotal() {
        Assert.assertThat(BigDecimal.ZERO, Matchers.comparesEqualTo(invoice.getGrossTotal()));
    }

    @Test
    public void testInvoiceSubtotalWithTwoDifferentProducts() {
        Product onions = new TaxFreeProduct("Warzywa", new BigDecimal("10"));
        Product apples = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        invoice.addProduct(onions);
        invoice.addProduct(apples);
        Assert.assertThat(new BigDecimal("20"), Matchers.comparesEqualTo(invoice.getNetTotal()));
    }

    @Test
    public void testInvoiceSubtotalWithManySameProducts() {
        Product onions = new TaxFreeProduct("Warzywa", BigDecimal.valueOf(10));
        invoice.addProduct(onions, 100);
        Assert.assertThat(new BigDecimal("1000"), Matchers.comparesEqualTo(invoice.getNetTotal()));
    }

    @Test
    public void testInvoiceHasTheSameSubtotalAndTotalIfTaxIsZero() {
        Product taxFreeProduct = new TaxFreeProduct("Warzywa", new BigDecimal("199.99"));
        invoice.addProduct(taxFreeProduct);
        Assert.assertThat(invoice.getNetTotal(), Matchers.comparesEqualTo(invoice.getGrossTotal()));
    }

    @Test
    public void testInvoiceHasProperSubtotalForManyProducts() {
        invoice.addProduct(new TaxFreeProduct("Owoce", new BigDecimal("200")));
        invoice.addProduct(new DairyProduct("Maslanka", new BigDecimal("100")));
        invoice.addProduct(new OtherProduct("Wino", new BigDecimal("10")));
        Assert.assertThat(new BigDecimal("310"), Matchers.comparesEqualTo(invoice.getNetTotal()));
    }

    @Test
    public void testInvoiceHasProperTaxValueForManyProduct() {
        // tax: 0
        invoice.addProduct(new TaxFreeProduct("Pampersy", new BigDecimal("200")));
        // tax: 8
        invoice.addProduct(new DairyProduct("Kefir", new BigDecimal("100")));
        // tax: 2.30
        invoice.addProduct(new OtherProduct("Piwko", new BigDecimal("10")));
        Assert.assertThat(new BigDecimal("10.30"), Matchers.comparesEqualTo(invoice.getTaxTotal()));
    }

    @Test
    public void testInvoiceHasProperTotalValueForManyProduct() {
        // price with tax: 200
        invoice.addProduct(new TaxFreeProduct("Maskotki", new BigDecimal("200")));
        // price with tax: 108
        invoice.addProduct(new DairyProduct("Maslo", new BigDecimal("100")));
        // price with tax: 12.30
        invoice.addProduct(new OtherProduct("Chipsy", new BigDecimal("10")));
        Assert.assertThat(new BigDecimal("320.30"), Matchers.comparesEqualTo(invoice.getGrossTotal()));
    }

    @Test
    public void testInvoiceHasPropoerSubtotalWithQuantityMoreThanOne() {
        // 2x kubek - price: 10
        invoice.addProduct(new TaxFreeProduct("Kubek", new BigDecimal("5")), 2);
        // 3x kozi serek - price: 30
        invoice.addProduct(new DairyProduct("Kozi Serek", new BigDecimal("10")), 3);
        // 1000x pinezka - price: 10
        invoice.addProduct(new OtherProduct("Pinezka", new BigDecimal("0.01")), 1000);
        Assert.assertThat(new BigDecimal("50"), Matchers.comparesEqualTo(invoice.getNetTotal()));
    }

    @Test
    public void testInvoiceHasPropoerTotalWithQuantityMoreThanOne() {
        // 2x chleb - price with tax: 10
        invoice.addProduct(new TaxFreeProduct("Chleb", new BigDecimal("5")), 2);
        // 3x chedar - price with tax: 32.40
        invoice.addProduct(new DairyProduct("Chedar", new BigDecimal("10")), 3);
        // 1000x pinezka - price with tax: 12.30
        invoice.addProduct(new OtherProduct("Pinezka", new BigDecimal("0.01")), 1000);
        Assert.assertThat(new BigDecimal("54.70"), Matchers.comparesEqualTo(invoice.getGrossTotal()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvoiceWithZeroQuantity() {
        invoice.addProduct(new TaxFreeProduct("Tablet", new BigDecimal("1678")), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvoiceWithNegativeQuantity() {
        invoice.addProduct(new DairyProduct("Zsiadle mleko", new BigDecimal("5.55")), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddingNullProduct() {
        invoice.addProduct(null);
    }

    @Test
    public void testInvoiceNumberDifferentThanZero() {
        Assert.assertNotEquals(0, invoice.getInvoiceNumber());
    }

    @Test
    public void testInvoiceNumberGreaterThanZero() {
        Assert.assertTrue(invoice.getInvoiceNumber() > 0);
    }

    @Test
    public void testSecondInvoiceNumberGreaterThanFirstInvoiceNumber() {
        Assert.assertTrue(invoice1.getInvoiceNumber() > invoice.getInvoiceNumber());
    }

    @Test
    public  void testInvoiceCreationDate() {
        Assert.assertEquals(LocalDate.now(), invoice.getCreationDate());
    }

    @Test
    public void testPrintInvoiceWithOneProducts() {
        System.setOut(new PrintStream(outputStreamCaptor));

        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits);
        String text = "Faktura nr: " + invoice.getInvoiceNumber() + ", data wystawienia: " + invoice.getCreationDate() + "\n"
                + "Nazwa: Owoce, Cena jedn. netto [PLN]: 200, Stawka VAT: 0%, "
                + "Cena jedn. brutto [PLN]: 200, Liczba: 1, Wartość brutto [PLN]: 200\n"
                + "Liczba pozycji: 1\n"
                + "Razem: Wartość Netto [PLN]: 200, Wartość VAT + Akcyza [PLN]: 0, Wartość Brutto [PLN]: 200";
        invoice.printInvoice();
        Assert.assertEquals(text, outputStreamCaptor.toString()
                .trim());

        System.setOut(standardOut);
    }

    @Test
    public void testPrintInvoiceWithOneProductsWithQuantityMoreThanOne() {
        System.setOut(new PrintStream(outputStreamCaptor));

        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        invoice.addProduct(milkProduct, 10);
        String text = "Faktura nr: " + invoice.getInvoiceNumber() + ", data wystawienia: " + invoice.getCreationDate() + "\n"
                + "Nazwa: Maslanka, Cena jedn. netto [PLN]: 100, Stawka VAT: 8.00%, "
                + "Cena jedn. brutto [PLN]: 108.00, Liczba: 10, Wartość brutto [PLN]: 1080.00\n"
                + "Liczba pozycji: 1\n"
                + "Razem: Wartość Netto [PLN]: 1000, Wartość VAT + Akcyza [PLN]: 80.00, Wartość Brutto [PLN]: 1080.00";
        invoice.printInvoice();
        Assert.assertEquals(text, outputStreamCaptor.toString()
                .trim());

        System.setOut(standardOut);
    }

    @Test
    public void testCompareProductsWithDifferentNamePrice() {
        Product onions = new TaxFreeProduct("Warzywa", new BigDecimal("10"));
        Product apples = new TaxFreeProduct("Owoce", new BigDecimal("100"));
        Assert.assertFalse(invoice.compareProductObject(onions, apples));
    }

    @Test
    public void testCompareProductsWithDifferentName() {
        Product onions = new TaxFreeProduct("Warzywa", new BigDecimal("10"));
        Product apples = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        Assert.assertFalse(invoice.compareProductObject(onions, apples));
    }

    @Test
    public void testCompareProductsWithDifferentPrice() {
        Product onions1 = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        Product onions2 = new TaxFreeProduct("Owoce", new BigDecimal("11"));
        Assert.assertFalse(invoice.compareProductObject(onions1, onions2));
    }

    @Test
    public void testCompareProductsWithTheSameNamePriceDifferentTax() {
        Product onions = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        Product exoticFruit = new DairyProduct("Owoce", new BigDecimal("10"));
        Assert.assertFalse(invoice.compareProductObject(onions, exoticFruit));
    }

    @Test
    public void testCompareTheSameProducts() {
        Product onions = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        Assert.assertTrue(invoice.compareProductObject(onions, onions));
    }

    @Test
    public void testCompareProductsWithTheSameNamePriceTax() {
        Product onions1 = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        Product onions2 = new TaxFreeProduct("Owoce", new BigDecimal("10"));
        //Przypadek błędnego utworzenia dwóch obiektów o tych samych atrybutach
        Assert.assertTrue(invoice.compareProductObject(onions1, onions2));
    }

    @Test
    public void testInvoiceHasProperSizeWithTwoDuplicateProducts() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits);
        invoice.addProduct(fruits);
        Assert.assertEquals(1, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperSizeWithTwoDuplicateProductsWithQuantityMoreThanOne() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits);
        invoice.addProduct(fruits, 10);
        Assert.assertEquals(1, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperSizeWithThreeDuplicateProductsWithQuantityMoreThanOne() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits);
        invoice.addProduct(fruits, 10);
        invoice.addProduct(fruits, 5);
        Assert.assertEquals(1, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithTwoDuplicateProducts() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits);
        invoice.addProduct(fruits);
        Assert.assertEquals(2, (int) invoice.getProducts().get(fruits));
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithTwoDuplicateProductsWithQuantityMoreThanOne() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits, 2);
        invoice.addProduct(fruits, 5);
        Assert.assertEquals(7, (int) invoice.getProducts().get(fruits));
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithThreeDuplicateProductsWithQuantityMoreThanOne() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        invoice.addProduct(fruits, 2);
        invoice.addProduct(fruits, 5);
        invoice.addProduct(fruits);
        Assert.assertEquals(8, (int) invoice.getProducts().get(fruits));
    }

    @Test
    public void testInvoiceHasProperSizeWithDuplicateProductsForManyProducts() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product alcohol = new OtherProduct("Wino", new BigDecimal("10"));
        invoice.addProduct(fruits);
        invoice.addProduct(milkProduct);
        invoice.addProduct(alcohol);
        invoice.addProduct(fruits);
        invoice.addProduct(alcohol);
        invoice.addProduct(fruits);
        Assert.assertEquals(3, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperSizeWithDuplicateProductsForManyProductsWithQuantityMoreThanOne() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product alcohol = new OtherProduct("Wino", new BigDecimal("10"));
        invoice.addProduct(fruits, 10);
        invoice.addProduct(milkProduct, 5);
        invoice.addProduct(alcohol, 20);
        invoice.addProduct(fruits);
        invoice.addProduct(alcohol, 30);
        invoice.addProduct(fruits, 5);
        Assert.assertEquals(3, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithDuplicateProductsForManyProducts() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product alcohol = new OtherProduct("Wino", new BigDecimal("10"));
        invoice.addProduct(fruits);
        invoice.addProduct(milkProduct);
        invoice.addProduct(alcohol);
        invoice.addProduct(fruits);
        invoice.addProduct(alcohol);
        invoice.addProduct(fruits);
        Assert.assertEquals(3, (int) invoice.getProducts().get(fruits));
        Assert.assertEquals(1, (int) invoice.getProducts().get(milkProduct));
        Assert.assertEquals(2, (int) invoice.getProducts().get(alcohol));
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithDuplicateProductsForManyProductsWithQuantityMoreThanOne() {
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product alcohol = new OtherProduct("Wino", new BigDecimal("10"));
        invoice.addProduct(fruits, 5);
        invoice.addProduct(milkProduct, 15);
        invoice.addProduct(alcohol, 9);
        invoice.addProduct(fruits, 10);
        invoice.addProduct(alcohol);
        invoice.addProduct(fruits, 5);
        Assert.assertEquals(20, (int) invoice.getProducts().get(fruits));
        Assert.assertEquals(15, (int) invoice.getProducts().get(milkProduct));
        Assert.assertEquals(10, (int) invoice.getProducts().get(alcohol));
    }

    @Test
    public void testInvoiceHasProperSizeWithWrongCreatedDuplicateProductsForManyProductsWithQuantityMoreThanOne() {
        //Przypadek błędnego utworzenia obiektów o tych samych atrybutach
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product fruits1 = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product milkProduct1 = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product milkProduct2 = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product alcohol = new OtherProduct("Wino", new BigDecimal("10"));
        invoice.addProduct(fruits);
        invoice.addProduct(fruits1, 19);
        invoice.addProduct(milkProduct, 5);
        invoice.addProduct(milkProduct1, 7);
        invoice.addProduct(milkProduct2, 3);
        invoice.addProduct(alcohol);
        Assert.assertEquals(3, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithWrongCreatedDuplicateProductsForManyProductsWithQuantityMoreThanOne() {
        //Przypadek błędnego utworzenia obiektów o tych samych atrybutach
        Product fruits = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product fruits1 = new TaxFreeProduct("Owoce", new BigDecimal("200"));
        Product milkProduct = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product milkProduct1 = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product milkProduct2 = new DairyProduct("Maslanka", new BigDecimal("100"));
        Product alcohol = new OtherProduct("Wino", new BigDecimal("10"));
        invoice.addProduct(fruits);
        invoice.addProduct(fruits1, 19);
        invoice.addProduct(milkProduct, 5);
        invoice.addProduct(milkProduct1, 7);
        invoice.addProduct(milkProduct2, 3);
        invoice.addProduct(alcohol);
        Assert.assertEquals(20, (int) invoice.getProducts().get(fruits));
        Assert.assertEquals(15, (int) invoice.getProducts().get(milkProduct));
        Assert.assertEquals(1, (int) invoice.getProducts().get(alcohol));
    }

    @Test
    public void testCompareProductsWithTheSameNamePriceTaxDifferentType() {
        Product alcohol = new BottleOfWine("Etanol", new BigDecimal("6"));
        Product gasoline = new FuelCanister("Etanol", new BigDecimal("6"));
        Assert.assertFalse(invoice.compareProductObject(alcohol, gasoline));
    }

    @Test
    public void testInvoiceHasProperSizeWithThreeDuplicateExciseTaxProductsWithQuantityMoreThanOne() {
        Product gasoline = new FuelCanister("Benzyna", new BigDecimal("6"));
        invoice.addProduct(gasoline);
        invoice.addProduct(gasoline, 10);
        invoice.addProduct(gasoline, 5);
        Assert.assertEquals(1, invoice.getProducts().size());
    }

    @Test
    public void testInvoiceHasProperProductQuantityWithThreeDuplicateExciseTaxProductsWithQuantityMoreThanOne() {
        Product gasoline = new FuelCanister("Benzyna", new BigDecimal("6"));
        invoice.addProduct(gasoline, 2);
        invoice.addProduct(gasoline, 5);
        invoice.addProduct(gasoline);
        Assert.assertEquals(8, (int) invoice.getProducts().get(gasoline));
    }

    @Test
    public void testInvoiceHasPropoerSubtotalWithQuantityMoreThanOneExciseTaxNoDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 04, 04);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Invoice invoice2 = new Invoice();
            // 2x kubek - price: 10
            invoice2.addProduct(new TaxFreeProduct("Kubek", new BigDecimal("5")), 2);
            // 3x kozi serek - price: 30
            invoice2.addProduct(new DairyProduct("Kozi Serek", new BigDecimal("10")), 3);
            // 1000x pinezka - price: 10
            invoice2.addProduct(new OtherProduct("Pinezka", new BigDecimal("0.01")), 1000);
            // 10x wino - price: 10
            invoice2.addProduct(new BottleOfWine("Wino", new BigDecimal("10")), 10);
            // 15x benzyna - price: 6
            invoice2.addProduct(new FuelCanister("Benzyna", new BigDecimal("6")), 15);
            Assert.assertThat(new BigDecimal("240"), Matchers.comparesEqualTo(invoice2.getNetTotal()));
        }
    }

    @Test
    public void testInvoiceHasPropoerSubtotalWithQuantityMoreThanOneExciseTaxDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 05);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Invoice invoice2 = new Invoice();
            // 2x kubek - price: 10
            invoice2.addProduct(new TaxFreeProduct("Kubek", new BigDecimal("5")), 2);
            // 3x kozi serek - price: 30
            invoice2.addProduct(new DairyProduct("Kozi Serek", new BigDecimal("10")), 3);
            // 1000x pinezka - price: 10
            invoice2.addProduct(new OtherProduct("Pinezka", new BigDecimal("0.01")), 1000);
            // 10x wino - price: 10
            invoice2.addProduct(new BottleOfWine("Wino", new BigDecimal("10")), 10);
            // 15x benzyna - price: 6
            invoice2.addProduct(new FuelCanister("Benzyna", new BigDecimal("6")), 15);
            Assert.assertThat(new BigDecimal("240"), Matchers.comparesEqualTo(invoice2.getNetTotal()));
        }
    }

    @Test
    public void testInvoiceHasPropoerTotalWithQuantityMoreThanOneExciseTaxNoDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 16);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Invoice invoice2 = new Invoice();
            // 2x kubek - price: 10
            invoice2.addProduct(new TaxFreeProduct("Kubek", new BigDecimal("5")), 2);
            // 3x kozi serek - price: 30
            invoice2.addProduct(new DairyProduct("Kozi Serek", new BigDecimal("10")), 3);
            // 1000x pinezka - price: 10
            invoice2.addProduct(new OtherProduct("Pinezka", new BigDecimal("0.01")), 1000);
            // 10x wino - price: 10
            invoice2.addProduct(new BottleOfWine("Wino", new BigDecimal("10")), 10);
            // 15x benzyna - price: 6
            invoice2.addProduct(new FuelCanister("Benzyna", new BigDecimal("6")), 15);
            Assert.assertThat(new BigDecimal("427.4"), Matchers.comparesEqualTo(invoice2.getGrossTotal()));
        }
    }

    @Test
    public void testInvoiceHasPropoerTotalWithQuantityMoreThanOneExciseTaxDiscount() {
        LocalDate noDiscountDay = LocalDate.of(2024, 03, 05);
        try (MockedStatic<LocalDate> mockedStatic = Mockito.mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(noDiscountDay);
            Invoice invoice2 = new Invoice();
            // 2x kubek - price: 10
            invoice2.addProduct(new TaxFreeProduct("Kubek", new BigDecimal("5")), 2);
            // 3x kozi serek - price: 30
            invoice2.addProduct(new DairyProduct("Kozi Serek", new BigDecimal("10")), 3);
            // 1000x pinezka - price: 10
            invoice2.addProduct(new OtherProduct("Pinezka", new BigDecimal("0.01")), 1000);
            // 10x wino - price: 10
            invoice2.addProduct(new BottleOfWine("Wino", new BigDecimal("10")), 10);
            // 15x benzyna - price: 6
            invoice2.addProduct(new FuelCanister("Benzyna", new BigDecimal("6")), 15);
            Assert.assertThat(new BigDecimal("344"), Matchers.comparesEqualTo(invoice2.getGrossTotal()));
        }
    }
}
