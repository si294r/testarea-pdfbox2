package mkl.testarea.pdfbox2.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class ExtractImages
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "extract");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0B9izTHWJQ7xlT2ZoQkJfbGRYcFE">
     * 10948.pdf
     * </a>
     * <p>
     * The only special thing about the two images returned for the sample PDF is that
     * one image is merely a mask used for the other image, and the other image is the
     * actual image used on the PDF page. If one only wants the images immediately used
     * in the page content, one also has to scan the page content.
     * </p>
     */
    @Test
    public void testExtractPageImageResources10948() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("10948.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            int page = 1;
            for (PDPage pdPage : document.getPages())
            {
                PDResources resources = pdPage.getResources();
                if (resource != null)
                {
                    int index = 0;
                    for (COSName cosName : resources.getXObjectNames())
                    {
                        PDXObject xobject = resources.getXObject(cosName);
                        if (xobject instanceof PDImageXObject)
                        {
                            PDImageXObject image = (PDImageXObject)xobject;
                            File file = new File(RESULT_FOLDER, String.format("10948-%s-%s.%s", page, index, image.getSuffix()));
                            ImageIO.write(image.getImage(), image.getSuffix(), file);
                            index++;
                        }
                    }
                }
                page++;
            }
        }
    }

}
