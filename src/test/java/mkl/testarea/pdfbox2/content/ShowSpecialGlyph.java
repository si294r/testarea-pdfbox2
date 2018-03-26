package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ShowSpecialGlyph {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/49426018/%e2%82%b9-indian-rupee-symbol-symbol-is-printing-as-question-mark-in-pdf-using-apa">
     * ₹ (Indian Rupee Symbol) symbol is printing as ? (question mark) in pdf using Apache PDFBOX
     * </a>
     * <p>
     * This test shows how to successfully show the Indian Rupee symbol
     * based on the OP's source frame and Tilman's proposed font.
     * </p>
     */
    @Test
    public void testIndianRupeeForVandanaSharma() throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDPageContentStream cos=  new PDPageContentStream(doc, page);
        cos.beginText();
        String text = "Deposited Cash of ₹10,00,000/- or more in a Saving Bank Account";
        cos.newLineAtOffset(25, 700);
        cos.setFont(PDType0Font.load(doc, new File("c:/windows/fonts/arial.ttf")), 12);
        cos.showText(text);
        cos.endText();
        cos.close();
        doc.save(new File(RESULT_FOLDER, "IndianRupee.pdf"));
        doc.close();
    }

}
