// $Id$
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
public class ExtractText
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/37862159/pdf-reading-via-pdfbox-in-java">
     * pdf reading via pdfbox in java 
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B_Ke2amBgdpedUNwVTR3RVlRTFE/view?usp=sharing">
     * PnL_500010_0314.pdf
     * </a>
     * <p>
     * Indeed, the <code>PDFTextStripper</code> is not even informed about those undecipherable
     * text sections. Essentially the underlying method `PDFTextStreamEngine.showGlyph` filters
     * all unmappable glyphs from composite fonts. 
     * </p>
     */
    @Test
    public void testExtractTestFromPnL_500010_0314() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("PnL_500010_0314.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* PnL_500010_0314.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "PnL_500010_0314.txt").toPath(), Collections.singleton(text));
        }
    }

}
