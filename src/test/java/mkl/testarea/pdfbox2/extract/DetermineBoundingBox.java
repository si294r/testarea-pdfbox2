package mkl.testarea.pdfbox2.extract;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <a href="https://stackoverflow.com/questions/52821421/how-do-determine-location-of-actual-pdf-content-with-pdfbox">
 * How do determine location of actual PDF content with PDFBox?
 * </a>
 * <p>
 * This class tests the {@link BoundingBoxFinder} by applying it to a
 * number of miscellaneous PDFs and stroking the determined boxes.
 * </p>
 * <p> 
 * The {@link BoundingBoxFinder} determines the bounding box of the static
 * content of a page. Beware, it is not very sophisticated; in particular
 * it does not ignore invisible content like a white background rectangle,
 * text drawn in rendering mode "invisible", arbitrary content covered
 * by a white filled path, white parts of bitmap images, ... Furthermore,
 * it ignores clip paths.
 * </p>
 * 
 * @author mkl
 */
public class DetermineBoundingBox {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }


    @Test
    public void test00000000000005fw6q() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("00000000000005fw6q.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "00000000000005fw6q-boundingBoxes.pdf"));
        }
    }

    @Test
    public void test10948() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("10948.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "10948-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testApache() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("apache.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "apache-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testEMPLOYMENTCONTRACTTEMPLATEcoveredAs() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("EMPLOYMENTCONTRACTTEMPLATE.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "EMPLOYMENTCONTRACTTEMPLATE-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testBal_532935_0314() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Bal_532935_0314.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "Bal_532935_0314-boundingBoxes.pdf"));
        }
    }

    @Test
    public void testTest() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("/mkl/testarea/pdfbox2/sign/test.pdf");
                PDDocument pdDocument = PDDocument.load(resource)   ) {
            drawBoundingBoxes(pdDocument);
            pdDocument.save(new File(RESULT_FOLDER, "test-boundingBoxes.pdf"));
        }
    }

    void drawBoundingBoxes(PDDocument pdDocument) throws IOException {
        for (PDPage pdPage : pdDocument.getPages()) {
            drawBoundingBox(pdDocument, pdPage);
        }
    }

    void drawBoundingBox(PDDocument pdDocument, PDPage pdPage) throws IOException {
        BoundingBoxFinder boxFinder = new BoundingBoxFinder(pdPage);
        boxFinder.processPage(pdPage);
        Rectangle2D box = boxFinder.getBoundingBox();
        if (box != null) {
            try (   PDPageContentStream canvas = new PDPageContentStream(pdDocument, pdPage, AppendMode.APPEND, true, true)) {
                canvas.setStrokingColor(Color.magenta);
                canvas.addRect((float)box.getMinX(), (float)box.getMinY(), (float)box.getWidth(), (float)box.getHeight());
                canvas.stroke();
            }
        }
    }
}
