package mkl.testarea.pdfbox2.easytable;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vandeseer.easytable.TableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.CellText;

/**
 * @author mkl
 */
public class RepeatingTableHeaders {
    final static File RESULT_FOLDER = new File("target/test-outputs", "easytable");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54233886/pdfbox-header-in-all-pages-using-easytable">
     * PDFBOX - header in all pages using easytable
     * </a>
     * <p>
     * This is the code from the test routine the OP referred to. Indeed,
     * it does not create the header rows on each page, there simply is no
     * explicit support for table headers in easytable. But it is possible
     * to add repeating headers nonetheless, cf. the test method
     * {@link #createTwoPageTableRepeatingHeader()}.
     * </p>
     */
    @Test
    public void createTwoPageTable() throws IOException {
        final Table.TableBuilder tableBuilder = Table.builder()
                .addColumnOfWidth(200)
                .addColumnOfWidth(200);

        CellText dummyHeaderCell = CellText.builder()
                .text("Header dummy")
                .backgroundColor(Color.BLUE)
                .textColor(Color.WHITE)
                .borderWidth(1F)
                .build();

        CellText dummyCell = CellText.builder()
                .text("dummy")
                .borderWidth(1F)
                .build();

        tableBuilder.addRow(
                Row.builder()
                        .add(dummyHeaderCell)
                        .add(dummyHeaderCell)
                        .build());

        for (int i = 0; i < 50; i++) {
            tableBuilder.addRow(
                    Row.builder()
                            .add(dummyCell)
                            .add(dummyCell)
                            .build());
        }

        final PDDocument document = new PDDocument();

        TableDrawer drawer = TableDrawer.builder()
                .table(tableBuilder.build())
                .startX(50)
                .startY(100F)
                .endY(50F) // note: if not set, table is drawn over the end of the page
                .build();

        do {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                drawer.contentStream(contentStream).draw();
            }

            drawer.startY(page.getMediaBox().getHeight() - 50);
        } while (!drawer.isFinished());

        document.save(new File(RESULT_FOLDER, "twoPageTable.pdf"));
        document.close();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54233886/pdfbox-header-in-all-pages-using-easytable">
     * PDFBOX - header in all pages using easytable
     * </a>
     * <p>
     * This is derived from the code from the test routine the OP referred
     * to. It has been changed to put the table headers in a separate table
     * which is drawn again and again. Indeed, it does create the header
     * rows on each page, unlike the original code, cf. the test method
     * {@link #createTwoPageTable()}.
     * </p>
     */
    @Test
    public void createTwoPageTableRepeatingHeader() throws IOException {
        final Table.TableBuilder tableHeaderBuilder = Table.builder()
                .addColumnOfWidth(200)
                .addColumnOfWidth(200);

        CellText dummyHeaderCell = CellText.builder()
                .text("Header dummy")
                .backgroundColor(Color.BLUE)
                .textColor(Color.WHITE)
                .borderWidth(1F)
                .build();

        tableHeaderBuilder.addRow(
                Row.builder()
                        .add(dummyHeaderCell)
                        .add(dummyHeaderCell)
                        .build());

        final Table.TableBuilder tableBuilder = Table.builder()
                .addColumnOfWidth(200)
                .addColumnOfWidth(200);

        CellText dummyCell = CellText.builder()
                .text("dummy")
                .borderWidth(1F)
                .build();

        for (int i = 0; i < 50; i++) {
            tableBuilder.addRow(
                    Row.builder()
                            .add(dummyCell)
                            .add(dummyCell)
                            .build());
        }

        Table tableHeader = tableHeaderBuilder.build();

        final PDDocument document = new PDDocument();

        float startY = 100F;

        TableDrawer drawer = TableDrawer.builder()
                .table(tableBuilder.build())
                .startX(50)
                .endY(50F) // note: if not set, table is drawn over the end of the page
                .build();

        do {
            TableDrawer headerDrawer = TableDrawer.builder()
                    .table(tableHeader)
                    .startX(50)
                    .build();

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                headerDrawer.startY(startY);
                headerDrawer.contentStream(contentStream).draw();
                drawer.startY(startY - tableHeader.getHeight());
                drawer.contentStream(contentStream).draw();
            }

            startY = page.getMediaBox().getHeight() - 50;
        } while (!drawer.isFinished());

        document.save(new File(RESULT_FOLDER, "twoPageTable-repeatingHeader.pdf"));
        document.close();
    }
}
