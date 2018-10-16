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
            PDFTextStripper stripper = new PDFVisibleTextStripper(true);
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* RevTeaser09072016.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "RevTeaser09072016.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/47908124/pdfbox-removing-invisible-text-by-clip-filling-paths-issue">
     * PDFBox - Removing invisible text (by clip/filling paths issue)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1xcZOusx3cEdZX4AT8QAVDqZe33YWla0H">
     * test.pdf
     * </a> as testDmitryK.pdf
     * <p>
     * Indeed, using the original {@link PDFVisibleTextStripper} implementation
     * a lot of visible characters where dropped. This was due to the incorrect
     * calculation of the <code>end</code> of the character baseline in the methods
     * {@link PDFVisibleTextStripper#processTextPosition(org.apache.pdfbox.text.TextPosition)}
     * and {@link PDFVisibleTextStripper#deleteCharsInPath()}.
     * </p>
     * <p>
     * After patching those {@link PDFVisibleTextStripper} methods to make use of
     * <code>end</code> only optionally, running the test with that option results
     * in a decent extraction of visible text.
     * </p>
     */
    @Test
    public void testTestDmitryK() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("testDmitryK.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testDmitryK.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testDmitryK.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/47908124/pdfbox-removing-invisible-text-by-clip-filling-paths-issue">
     * PDFBox - Removing invisible text (by clip/filling paths issue)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1l0Yt9BJXs09bXcBD7pDbxFiZQQqnuaan">
     * test2.pdf
     * </a> as test2DmitryK.pdf
     * <p>
     * Indeed, even the {@link PDFVisibleTextStripper} implementation as originally
     * improved for {@link #testTestDmitryK()} failed for this document. The cause
     * is another normalization by PDFBox text stripping moving the origin into the
     * lower left corner of the crop box.
     * </p>
     * <p>
     * Patching the {@link PDFVisibleTextStripper} methods to add the lower left
     * crop box coordinate values again results in a decent extraction of visible
     * text.
     * </p>
     */
    @Test
    public void testTest2DmitryK() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("test2DmitryK.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test2DmitryK.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test2DmitryK.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://github.com/mkl-public/testarea-pdfbox2/issues/3">
     * One case fails to remove invisible texts or symbols
     * </a>
     * <br/>
     * <a href="https://github.com/mkl-public/testarea-pdfbox2/files/2481423/00000000000005fw6q.pdf">
     * 00000000000005fw6q.pdf
     * </a>
     * <p>
     * The "hidden text" recognized by Adobe here is only "hidden"
     * because it uses a glyph (page 1, Font F9, code 0000) for which
     * the embedded font draws nothing but which ToUnicode maps to
     * U+DBD0, a High Private Use Surrogate which by itself in general
     * makes no sense.
     * </p>
     */
    @Test
    public void test00000000000005fw6q() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("00000000000005fw6q.pdf")  ) {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFVisibleTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 00000000000005fw6q.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "00000000000005fw6q.txt").toPath(), Collections.singleton(text));
        }
    }
}
