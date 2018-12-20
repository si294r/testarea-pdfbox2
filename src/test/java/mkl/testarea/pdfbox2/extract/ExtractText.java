package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
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

    /**
     * <a href="https://stackoverflow.com/questions/47515609/invalid-block-type-while-using-pdfbox-2-0-8">
     * Invalid block type while using pdfbox 2.0.8
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/s/xjeksj0cay4x3vo/NoTemplateInError.pdf?dl=0">
     * NoTemplateInError.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced.
     * </p>
     */
    @Test
    public void testNoTemplateInError() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("NoTemplateInError.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* NoTemplateInError.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "NoTemplateInError.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/48828500/again-having-invisible-text-coming-from-pdftextstripper">
     * Again having invisible text coming from PdfTextStripper
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1P1oFu8cpZnzy9LF4wiGWPrk3PfL6dktt">
     * testFailed.pdf
     * </a>
     * <p>
     * The extracted, invisible text is rendered WHITE on WHITE.
     * </p>
     */
    @Test
    public void testTestFailed() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testFailed.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testFailed.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testFailed.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49746202/read-pdf-file-using-pdfbox-in-utf-8-in-java-scala">
     * Read pdf file using pdfbox in UTF-8 in java/scala
     * </a>
     * <br/>
     * <a href="https://1drv.ms/b/s!AmHcFaD-gMGyhg6eyqSy2gu9sLWl">
     * test.pdf
     * </a> as testKabirManandhar.pdf
     * <p>
     * The issue can be reproduced. The cause are incomplete ToUnicode
     * maps. There is an option, though: The embedded font programs
     * appear to include more complete mappings, so repairing the
     * ToUnicode table seems feasible.
     * </p>
     */
    @Test
    public void testTestKabirManandhar() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testKabirManandhar.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testKabirManandhar.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testKabirManandhar.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/50044892/pdfbox-invisible-text-from-pdftextstripper-not-clip-path-or-color-issue">
     * PDFBox: Invisible text from PdfTextStripper (not clip path or color issue)
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=1jOMq4sO393JSD60KoMX9WdzMJtY7ppze">
     * test.pdf
     * </a> as testSeparation.pdf
     * <p>
     * To retrieve the separation color values, one must tell the stripper
     * to look for the generic color operators cs and scn.
     * </p>
     */
    @Test
    public void testTestSeparation() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("testSeparation.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper() {
                @Override
                protected void processTextPosition(TextPosition text) {
                    PDGraphicsState gs = getGraphicsState();
                    PDColor color = gs.getNonStrokingColor();
                    float[] currentComponents = color.getComponents();
                    if (!Arrays.equals(components, currentComponents)) {
                        System.out.print(Arrays.toString(currentComponents));
                        components = currentComponents;
                    }
                    System.out.print(text.getUnicode());
                    super.processTextPosition(text);
                }
                
                float[] components;
            };
            stripper.addOperator(new SetNonStrokingColorSpace());
            stripper.addOperator(new SetNonStrokingColorN());
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* testSeparation.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "testSeparation.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/51672080/pdfdomtree-does-not-detecting-white-spaces-while-converting-a-pdf-file-to-html">
     * PDFDomTree does not detecting white spaces while converting a pdf file to html
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1SZNFCvGVbQzCxJiRr8HlW99ravC_Cm71/view?usp=sharing">
     * demo.pdf
     * </a>
     * <p>
     * PDFBox shows no issue extracting the text from the given file.
     * </p>
     */
    @Test
    public void testDemo() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("demo.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* demo.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "demo.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53382793/java-8-pdfbox-cant-gettext-of-pdf-file">
     * Java 8 PDFbox can't getText of pdf file
     * </a>
     * <br/>
     * <a href="http://www.o-cha.net/english/cup/pdf/29.pdf">
     * 29.pdf
     * </a>
     * <p>
     * Cannot reproduce any issue.
     * </p>
     */
    @Test
    public void test29() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("29.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 29.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "29.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53551335/java-does-pdfbox-have-an-option-to-open-file-instead-of-loading-it">
     * Java- Does pdfBox have an option to open file instead of loading it?
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/osyk2ieoq6od2p8/10-million-password-list-top-1000000.pdf?dl=0">
     * 10-million-password-list-top-1000000.pdf
     * </a>
     * <p>
     * In contrast to the OP I did not need to fiddle with the memory
     * settings at all for a plain extraction. Furthermore, I got 999999
     * lines with words and 3 empty lines from the file, not 10000000
     * passwords.
     * </p>
     */
    @Test
    public void test10MillionPasswordListTop1000000() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("10-million-password-list-top-1000000.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 10-million-password-list-top-1000000.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "10-million-password-list-top-1000000.txt").toPath(), Collections.singleton(text));
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/53837294/performance-for-loading-pdf-using-pdfbox">
     * Performance for loading pdf using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1KW5tVtF1gtcPNv2R7pIdDFMph8FhafT2?usp=sharing">
     * 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf
     * </a>
     * <p>
     * I cannot reproduce the enormous time required for text
     * extraction claimed by the OP.
     * </p>
     * @see #test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2()
     */
    @Test
    public void test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.txt").toPath(), Collections.singleton(text));
        }

        int runs = 10;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf")    )
            {
                PDDocument document = PDDocument.load(resource);
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.getText(document);
            }
        }
        long duration = System.currentTimeMillis() - start;
        System.out.printf("\nExtract %d times from '284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1.pdf' took %dms.\n", runs, duration);
    }

    /**
     * <a href="https://stackoverflow.com/questions/53837294/performance-for-loading-pdf-using-pdfbox">
     * Performance for loading pdf using PDFBox
     * </a>
     * <br/>
     * <a href="https://drive.google.com/drive/folders/1KW5tVtF1gtcPNv2R7pIdDFMph8FhafT2?usp=sharing">
     * 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf
     * </a>
     * <p>
     * I cannot reproduce the enormous time required for text
     * extraction claimed by the OP.
     * </p>
     * @see #test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V1()
     */
    @Test
    public void test284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf")    )
        {
            PDDocument document = PDDocument.load(resource);
            PDFTextStripper stripper = new PDFTextStripper();
            //stripper.setSortByPosition(true);
            String text = stripper.getText(document);

            System.out.printf("\n*\n* 284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf\n*\n%s\n", text);
            Files.write(new File(RESULT_FOLDER, "284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.txt").toPath(), Collections.singleton(text));
        }

        int runs = 10;
        long start = System.currentTimeMillis();
        for (int i = 0; i < runs; i++) {
            try (   InputStream resource = getClass().getResourceAsStream("284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf")    )
            {
                PDDocument document = PDDocument.load(resource);
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.getText(document);
            }
        }
        long duration = System.currentTimeMillis() - start;
        System.out.printf("\nExtract %d times from '284527_7605_CDM_PALET_MEDITERRANEEN_SURGELE_300G_FR_V2.pdf' took %dms.\n", runs, duration);
    }
}
