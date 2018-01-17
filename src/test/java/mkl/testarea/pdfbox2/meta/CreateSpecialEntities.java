package mkl.testarea.pdfbox2.meta;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateSpecialEntities {
    final static File RESULT_FOLDER = new File("target/test-outputs", "meta");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/48291169/manipulating-acrofields-with-pdfbox-changes-encoding-of-checkboxes-onvalue">
     * Manipulating acrofields with pdfbox changes encoding of checkboxes onValue
     * </a>
     * <p>
     * This simple PDF illustrates the PDFBox issues with interesting
     * PDF Name objects. The name <code>/äöüß</code> created below in
     * the output appears as <code>/#3F#3F#3F#3F</code>...
     * </p>
     */
    @Test
    public void testCreateNonAsciiCosName() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        document.getDocumentCatalog().getCOSObject().setString(COSName.getPDFName("äöüß"), "äöüß");
        document.save(new File(RESULT_FOLDER, "non-ascii-name.pdf"));
        document.close();
    }

}
