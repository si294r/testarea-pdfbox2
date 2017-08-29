// $Id$
package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map.Entry;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
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
    public void testPnL_500010_0314() throws IOException
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

    /**
     * <a href="http://stackoverflow.com/questions/37862159/pdf-reading-via-pdfbox-in-java">
     * pdf reading via pdfbox in java 
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B_Ke2amBgdpebm96U05FcWFsSXM/view?usp=sharing">
     * Bal_532935_0314.pdf
     * </a>
     * <p>
     * The issue here is caused by PDFBox guessing an encoding. The underlying method
     * `PDFTextStreamEngine.showGlyph` does this for all unmappable glyphs from simple
     * fonts.
     * </p>
     */
    @Test
    public void testBal_532935_0314() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Bal_532935_0314.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* Bal_532935_0314.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "Bal_532935_0314.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38057338/pdfbox-symbolic-fonts-must-have-a-built-in-encoding-error-when-using-pdftextst">
     * PDFBox �Symbolic fonts must have a built-in encoding� error when using PDFTextStripper.getText()
     * </a>
     * <br/>
     * <a href="http://www.cv-foundation.org/openaccess/content_cvpr_2016/papers/Park_Efficient_and_Robust_CVPR_2016_paper.pdf">
     * Park_Efficient_and_Robust_CVPR_2016_paper.pdf
     * </a>
     * <br/>
     * Issue <a href="https://issues.apache.org/jira/browse/PDFBOX-3403">PDFBOX-3403</a>
     * <p>
     * The issue here is caused by PDFBox not knowing MacExpertEncoding yet. But even
     * if there was no known base encoding, there is no need for the exception at all
     * as all font glyphs are covered in the Differences array.
     * </p>
     */
    @Test
    public void testPark_Efficient_and_Robust_CVPR_2016_paper() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Park_Efficient_and_Robust_CVPR_2016_paper.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* Park_Efficient_and_Robust_CVPR_2016_paper.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "Park_Efficient_and_Robust_CVPR_2016_paper.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/38975091/pdfbox-gettext-not-returning-all-of-the-visible-text">
     * PDFBox getText not returning all of the visible text
     * </a>
     * <br>
     * <a href="https://dl.dropboxusercontent.com/u/14898138/03%20WP%20Enterprise%20BlackBerry%20Compete%20Datasheet_041612%20FINAL%20DRAFT.pdf">
     * 03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.pdf
     * </a>
     * <p>
     * There is some 'writing' actually done using vector graphics, not text,
     * but aside from that all is accounted for.
     * </p>
     */
    @Test
    public void test03WpEnterpriseBlackBerryCompeteDatasheet_041612FinalDraft() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "03 WP Enterprise BlackBerry Compete Datasheet_041612 FINAL DRAFT.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45895768/pdfbox-2-0-7-extracttext-not-working-but-1-8-13-does-and-pdfreader-as-well">
     * PDFBox 2.0.7 ExtractText not working but 1.8.13 does and PDFReader as well
     * </a>
     * <br/>
     * <a href="https://wetransfer.com/downloads/214674449c23713ee481c5a8f529418320170827201941/b2bea6">
     * test-2.pdf
     * </a>
     * <p>
     * Due to the broken <b>ToUnicode</b> maps the output of this test is
     * unsatisfying. It can be improved by removing these <b>ToUnicode</b>
     * maps, cf. {@link #testNoToUnicodeTest2()}.
     * </p>
     */
    @Test
    public void testTest2() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test-2.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test-2.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test-2.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/45895768/pdfbox-2-0-7-extracttext-not-working-but-1-8-13-does-and-pdfreader-as-well">
     * PDFBox 2.0.7 ExtractText not working but 1.8.13 does and PDFReader as well
     * </a>
     * <br/>
     * <a href="https://wetransfer.com/downloads/214674449c23713ee481c5a8f529418320170827201941/b2bea6">
     * test-2.pdf
     * </a>
     * <p>
     * Due to the broken <b>ToUnicode</b> maps the output of immediate text
     * extraction from this document is unsatisfying, cf. {@link #testTest2()}.
     * It can be improved by removing these <b>ToUnicode</b> maps as this test
     * shows.
     * </p>
     */
    @Test
    public void testNoToUnicodeTest2() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test-2.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);

            for (int pageNr = 0; pageNr < document.getNumberOfPages(); pageNr++)
            {
                PDPage page = document.getPage(pageNr);
                PDResources resources = page.getResources();
                removeToUnicodeMaps(resources);
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            System.out.printf("\n*\n* test-2.pdf without ToUnicode\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "test-2_NoToUnicode.txt").toPath(), Collections.singleton(text));
        }
    }

    void removeToUnicodeMaps(PDResources pdResources) throws IOException
    {
        COSDictionary resources = pdResources.getCOSObject();

        COSDictionary fonts = asDictionary(resources, COSName.FONT);
        if (fonts != null)
        {
            for (COSBase object : fonts.getValues())
            {
                while (object instanceof COSObject)
                    object = ((COSObject)object).getObject();
                if (object instanceof COSDictionary)
                {
                    COSDictionary font = (COSDictionary)object;
                    font.removeItem(COSName.TO_UNICODE);
                }
            }
        }

        for (COSName name : pdResources.getXObjectNames())
        {
            PDXObject xobject = pdResources.getXObject(name);
            if (xobject instanceof PDFormXObject)
            {
                PDResources xobjectPdResources = ((PDFormXObject)xobject).getResources();
                removeToUnicodeMaps(xobjectPdResources);
            }
        }
    }

    COSDictionary asDictionary(COSDictionary dictionary, COSName name)
    {
        COSBase object = dictionary.getDictionaryObject(name);
        return object instanceof COSDictionary ? (COSDictionary) object : null;
    }
}
