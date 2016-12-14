package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.pdfbox.util.Matrix;
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

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B9izTHWJQ7xlYi1XN1BxMmZEUGc">
     * 10948.pdf
     * </a>, renamed "10948-new.pdf" here to prevent a collision
     * <p>
     * Here the code extracts no image at all because the images are not immediate page
     * resources but wrapped in form xobjects.
     * </p>
     */
    @Test
    public void testExtractPageImageResources10948New() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("10948-new.pdf"))
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
                            File file = new File(RESULT_FOLDER, String.format("10948-new-%s-%s.%s", page, index, image.getSuffix()));
                            ImageIO.write(image.getImage(), image.getSuffix(), file);
                            index++;
                        }
                    }
                }
                page++;
            }
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B9izTHWJQ7xlYi1XN1BxMmZEUGc">
     * 10948.pdf
     * </a>, renamed "10948-new.pdf" here to prevent a collision
     * <p>
     * The PDFBox tool {@link org.apache.pdfbox.tools.ExtractImages} does extract
     * the images (if you have included com.github.jai-imageio:jai-imageio-core
     * that is). Unfortunately it does not include the page it finds the respective
     * images on.
     * </p>
     */
    @Test
    public void testExtractPageImagesTool10948New() throws IOException
    {
        org.apache.pdfbox.tools.ExtractImages.main(new String[]{ "-prefix", new File(RESULT_FOLDER, "10948-new-tool").toString(),
                "src/test/resources/mkl/testarea/pdfbox2/extract/10948-new.pdf" });
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B9izTHWJQ7xlYi1XN1BxMmZEUGc">
     * 10948.pdf
     * </a>, renamed "10948-new.pdf" here to prevent a collision
     * <p>
     * Here we adopt the technique from the PDFBox tool {@link org.apache.pdfbox.tools.ExtractImages}
     * and name the exported images properly.
     * </p>
     */
    @Test
    public void testExtractPageImages10948New() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("10948-new.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractPageImages(document, "10948-new-engine-%s-%s%s.%s");
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B9izTHWJQ7xlY1lpVERlZm9kRkk">
     * t1_edited.pdf
     * </a>
     * <p>
     * In contrast to the OP's observation the extraction works properly here.
     * </p>
     */
    @Test
    public void testExtractPageImagesT1Edited() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("t1_edited.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractPageImages(document, "t1_edited-engine-%s-%s%s.%s");
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B9izTHWJQ7xldkVPRHFWU1picGs">
     * 1604-Orange_flat_2_edited.pdf
     * </a>
     * <p>
     * This image comes in stripes. Nothing unusual.
     * </p>
     */
    @Test
    public void testExtractPageImages1604OrangeFlat2Edited() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("1604-Orange_flat_2_edited.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractPageImages(document, "1604-Orange_flat_2_edited-engine-%s-%s%s.%s");
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <br/>
     * <a href="https://drive.google.com/open?id=0B9izTHWJQ7xlQkRHMmtIU2ZPUDA">
     * test_fact.pdf
     * </a>
     * <p>
     * The logo actually is the only bitmp image here.
     * </p>
     */
    @Test
    public void testExtractPageImagesTestFact() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test_fact.pdf"))
        {
            PDDocument document = PDDocument.load(resource);
            extractPageImages(document, "test_fact-engine-%s-%s%s.%s");
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/40531871/how-can-i-check-if-pdf-page-is-imagescanned-by-pdfbox-xpdf">
     * How can I check if PDF page is image(scanned) by PDFBOX, XPDF
     * </a>
     * <p>
     * Here we adopt the technique from the PDFBox tool {@link org.apache.pdfbox.tools.ExtractImages}
     * and name the exported images properly.
     * </p>
     */
    void extractPageImages(PDDocument document, String fileNameFormat) throws IOException
    {
        int page = 1;
        for (final PDPage pdPage : document.getPages())
        {
            final int currentPage = page;
            PDFGraphicsStreamEngine pdfGraphicsStreamEngine = new PDFGraphicsStreamEngine(pdPage)
            {
                int index = 0;
                
                @Override
                public void drawImage(PDImage pdImage) throws IOException
                {
                    if (pdImage instanceof PDImageXObject)
                    {
                        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
                        String flips = "";
                        if (ctm.getScaleX() < 0)
                            flips += "h";
                        if (ctm.getScaleY() < 0)
                            flips += "v";
                        if (flips.length() > 0)
                            flips = "-" + flips;
                        PDImageXObject image = (PDImageXObject)pdImage;
                        File file = new File(RESULT_FOLDER, String.format(fileNameFormat, currentPage, index, flips, image.getSuffix()));
                        ImageIOUtil.writeImage(image.getImage(), image.getSuffix(), new FileOutputStream(file));
                        index++;
                    }
                }

                @Override
                public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException { }

                @Override
                public void clip(int windingRule) throws IOException { }

                @Override
                public void moveTo(float x, float y) throws IOException {  }

                @Override
                public void lineTo(float x, float y) throws IOException { }

                @Override
                public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException {  }

                @Override
                public Point2D getCurrentPoint() throws IOException { return null; }

                @Override
                public void closePath() throws IOException { }

                @Override
                public void endPath() throws IOException { }

                @Override
                public void strokePath() throws IOException { }

                @Override
                public void fillPath(int windingRule) throws IOException { }

                @Override
                public void fillAndStrokePath(int windingRule) throws IOException { }

                @Override
                public void shadingFill(COSName shadingName) throws IOException { }
            };
            pdfGraphicsStreamEngine.processPage(pdPage);
            page++;
        }
    }
}
