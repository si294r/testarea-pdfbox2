package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractVisibleText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/47358127/remove-invisible-text-from-pdf-using-pdfbox">
     * remove invisible text from pdf using pdfbox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1F8vrzcABwxVGdN5W-7etQggY5xKtGplU/view">
     * RevTeaser09072016.pdf
     * </a>
     * <p>
     * This class tests the {@link PDFVisibleTextStripper} to ignore text hidden
     * by clipping or by covering with a filled path in the OP's sample document.
     * </p>
     */
    @Test
    public void testExtractFromRevTeaser09072016() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("RevTeaser09072016.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* RevTeaser09072016.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "RevTeaser09072016.txt").toPath(), Collections.singleton(text));
        }
    }

}
