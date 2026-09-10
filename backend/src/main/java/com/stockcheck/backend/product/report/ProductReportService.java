package com.stockcheck.backend.product.report;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.stockcheck.backend.product.Product;
import com.stockcheck.backend.product.ProductRepository;
import com.stockcheck.backend.security.SecurityUtils;
import com.stockcheck.backend.tenant.Tenant;
import com.stockcheck.backend.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Generates the "Product Inventory Report" PDF shown/downloaded from the
 * Products page. Only ever includes products belonging to the currently
 * authenticated user's own tenant - the tenant is resolved from the
 * security context, never accepted from the caller.
 *
 * <p>Text is rendered with an embedded Unicode TTF font (Lora, SIL OFL
 * licensed - see {@code /fonts/OFL.txt} on the classpath) rather than a
 * standard Base-14 PDF font. Base-14 fonts such as Helvetica only cover
 * Latin-1 and render Cyrillic characters as blank/invisible glyphs, which
 * is unacceptable for a fully Russian-localized report. Embedding a real
 * Unicode font with {@link BaseFont#IDENTITY_H} encoding also makes the PDF
 * self-contained: it renders identically regardless of what fonts happen
 * to be installed on the server (Render's Linux container included).
 */
@Service
public class ProductReportService {

    private static final Color HEADER_BACKGROUND = new Color(30, 41, 59); // slate-800, matches app palette
    private static final DateTimeFormatter GENERATED_AT_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String REGULAR_FONT_RESOURCE = "/fonts/Lora-Regular.ttf";
    private static final String BOLD_FONT_RESOURCE = "/fonts/Lora-Bold.ttf";

    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    private final BaseFont regularBaseFont;
    private final BaseFont boldBaseFont;

    public ProductReportService(ProductRepository productRepository, TenantRepository tenantRepository) {
        this.productRepository = productRepository;
        this.tenantRepository = tenantRepository;
        this.regularBaseFont = loadEmbeddedBaseFont(REGULAR_FONT_RESOURCE);
        this.boldBaseFont = loadEmbeddedBaseFont(BOLD_FONT_RESOURCE);
    }

    @Transactional(readOnly = true)
    public byte[] generateInventoryReportPdf() {
        UUID tenantId = SecurityUtils.getCurrentTenantId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated tenant context is required"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found: " + tenantId));

        List<Product> products = productRepository.findByShopTenantId(tenantId).stream()
                .filter(Product::isActive)
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        try {
            return render(tenant.getName(), products);
        } catch (DocumentException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сформировать отчет");
        }
    }

    private byte[] render(String tenantName, List<Product> products) throws DocumentException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 48);
        PdfWriter writer = PdfWriter.getInstance(document, output);
        writer.setPageEvent(new PageNumberFooter(regularBaseFont));
        document.open();

        Font titleFont = new Font(boldBaseFont, 18, Font.NORMAL, Color.BLACK);
        Font subtitleFont = new Font(regularBaseFont, 10, Font.NORMAL, new Color(100, 116, 139));
        Font headerFont = new Font(boldBaseFont, 10, Font.NORMAL, Color.WHITE);
        Font cellFont = new Font(regularBaseFont, 10, Font.NORMAL, Color.BLACK);
        Font emptyFont = new Font(regularBaseFont, 11, Font.NORMAL, new Color(100, 116, 139));

        document.add(new Paragraph("Отчет по товарам", titleFont));
        document.add(new Paragraph(
                tenantName + "  \u2022  Сформировано: " + LocalDateTime.now().format(GENERATED_AT_FORMAT),
                subtitleFont
        ));
        document.add(Chunk.NEWLINE);

        if (products.isEmpty()) {
            document.add(new Paragraph("Товаров пока нет", emptyFont));
        } else {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{42, 16, 21, 21});
            table.setHeaderRows(1);

            addHeaderCell(table, "Товар", headerFont);
            addHeaderCell(table, "Кол-во", headerFont);
            addHeaderCell(table, "Цена закупки", headerFont);
            addHeaderCell(table, "Цена продажи", headerFont);

            for (Product product : products) {
                table.addCell(dataCell(product.getName(), cellFont, Element.ALIGN_LEFT));
                table.addCell(dataCell(String.valueOf(product.getQuantity()), cellFont, Element.ALIGN_CENTER));
                table.addCell(dataCell(formatPrice(product.getPurchasePrice()), cellFont, Element.ALIGN_RIGHT));
                table.addCell(dataCell(formatPrice(product.getDefaultSalePrice()), cellFont, Element.ALIGN_RIGHT));
            }

            document.add(table);
        }

        document.close();
        return output.toByteArray();
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_BACKGROUND);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static PdfPCell dataCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    /** Prices are optional; a missing price is shown as "—", never as 0 or blank. */
    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return "\u2014";
        }
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        return format.format(price) + " сом";
    }

    /**
     * Loads a TTF from the classpath as raw bytes and builds a
     * Unicode-encoded {@link BaseFont} from them. Reading the bytes
     * ourselves (rather than passing a file path to OpenPDF) avoids any
     * assumption about the font being reachable as a plain filesystem
     * path - it works identically whether running from an IDE, a flat
     * classpath in tests, or a Spring Boot fat JAR on Render where
     * resources live inside a nested {@code BOOT-INF/classes} archive.
     * IDENTITY_H + embedded=true guarantees the glyphs travel with the PDF
     * itself, so rendering never depends on fonts installed on the server.
     */
    private static BaseFont loadEmbeddedBaseFont(String classpathResource) {
        try (InputStream in = ProductReportService.class.getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException("Font resource not found on classpath: " + classpathResource);
            }
            byte[] fontBytes = in.readAllBytes();
            return BaseFont.createFont(classpathResource, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
        } catch (IOException | DocumentException e) {
            throw new IllegalStateException("Failed to load embedded PDF font: " + classpathResource, e);
        }
    }

    /** Draws "Стр. N" centered at the bottom of every page. */
    private static final class PageNumberFooter extends PdfPageEventHelper {
        private final Font footerFont;

        PageNumberFooter(BaseFont baseFont) {
            this.footerFont = new Font(baseFont, 8, Font.NORMAL, new Color(148, 163, 184));
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Phrase footer = new Phrase("Стр. " + writer.getPageNumber(), footerFont);
            ColumnText.showTextAligned(
                    cb,
                    Element.ALIGN_CENTER,
                    footer,
                    (document.right() + document.left()) / 2,
                    document.bottom() - 20,
                    0
            );
        }
    }
}
