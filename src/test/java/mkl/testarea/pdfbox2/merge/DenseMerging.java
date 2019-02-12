package mkl.testarea.pdfbox2.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class DenseMerging {
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54283827/pdf-files-merge-remove-blank-at-end-of-page-i-am-using-pdfbox-v2-0-13-to-achi">
     * PDF files merge : remove blank at end of page. I am using PDFBox v2.0.13 to achieve that
     * </a>
     * <p>
     * This test checks the {@link PdfDenseMergeTool} which allows
     * a dense merging of multiple input PDFs.
     * </p>
     */
    @Test
    public void testWithText() throws IOException {
        PDDocument document1 = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(30, 300),
                "Doc 1 line 1", "Doc 1 line 2", "Doc 1 line 3");
        document1.save(new File(RESULT_FOLDER, "Test Text 1.pdf"));
        PDDocument document2 = createTextDocument(new PDRectangle(0, 0, 400, 600), 
                Matrix.getTranslateInstance(40, 400),
                "Doc 2 line 1", "Doc 2 line 2", "Doc 2 line 3");
        document2.save(new File(RESULT_FOLDER, "Test Text 2.pdf"));
        PDDocument document3 = createTextDocument(new PDRectangle(0, -300, 400, 600), 
                Matrix.getTranslateInstance(50, -100),
                "Doc 3 line 1", "Doc 3 line 2", "Doc 3 line 3");
        document3.save(new File(RESULT_FOLDER, "Test Text 3.pdf"));
        PDDocument document4 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getTranslateInstance(-140, -100),
                "Doc 4 line 1", "Doc 4 line 2", "Doc 4 line 3");
        document4.save(new File(RESULT_FOLDER, "Test Text 4.pdf"));
        PDDocument document5 = createTextDocument(new PDRectangle(-200, -300, 400, 600), 
                Matrix.getRotateInstance(Math.PI / 4, -120, 0),
                "Doc 5 line 1", "Doc 5 line 2", "Doc 5 line 3");
        document5.save(new File(RESULT_FOLDER, "Test Text 5.pdf"));

        PdfDenseMergeTool tool = new PdfDenseMergeTool(PDRectangle.A4, 30, 30, 10);
        tool.merge(new FileOutputStream(new File(RESULT_FOLDER, "Merge with Text.pdf")),
                Arrays.asList(document1, document2, document3, document4, document5,
                        document1, document2, document3, document4, document5,
                        document1, document2, document3, document4, document5));
    }

    PDDocument createTextDocument(PDRectangle size, Matrix textMatrix, String... lines) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(size);
        document.addPage(page);

        try (PDPageContentStream canvas = new PDPageContentStream(document, page)) {
            canvas.beginText();
            canvas.setTextMatrix(textMatrix);
            canvas.setFont(PDType1Font.HELVETICA_BOLD, 12);
            canvas.setLeading(14);
            for (String line : lines) {
                canvas.showText(line);
                canvas.newLine();
            }
            canvas.endText();
        }

        return document;
    }
}
