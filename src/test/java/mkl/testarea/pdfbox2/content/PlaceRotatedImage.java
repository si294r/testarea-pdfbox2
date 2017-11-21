package mkl.testarea.pdfbox2.content;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class PlaceRotatedImage {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/47383506/pdfbox-obtain-bounding-box-of-rotated-image">
     * PDFBox - obtain bounding box of rotated image
     * </a>
     * <p>
     * This test demonstrates how to position images given their dimensions,
     * rotation angle, and the coordinates of the lower left corner of their
     * bounding box. The work horse is {@link #placeImage(PDDocument, PDPage,
     * PDImageXObject, float, float, float, float, float)}. 
     * </p>
     */
    @Test
    public void testPlaceByBoundingBox() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("Willi-1.jpg");
                PDDocument document = new PDDocument()  ) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDRectangle mediaBox = page.getMediaBox();
            float bbLowerLeftX = 50;
            float bbLowerLeftY = 100;

            try (   PDPageContentStream contentStream = new PDPageContentStream(document, page)   ) {
                contentStream.moveTo(bbLowerLeftX, mediaBox.getLowerLeftY());
                contentStream.lineTo(bbLowerLeftX, mediaBox.getUpperRightY());
                contentStream.moveTo(mediaBox.getLowerLeftX(), bbLowerLeftY);
                contentStream.lineTo(mediaBox.getUpperRightX(), bbLowerLeftY);
                contentStream.stroke();
            }

            PDImageXObject image = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(resource), "Image");
            placeImage(document, page, image, bbLowerLeftX, bbLowerLeftY, image.getWidth(), image.getHeight(), (float)(Math.PI/4));
            placeImage(document, page, image, bbLowerLeftX, bbLowerLeftY, .5f*image.getWidth(), .5f*image.getHeight(), 0);
            placeImage(document, page, image, bbLowerLeftX, bbLowerLeftY, .25f*image.getWidth(), .25f*image.getHeight(), (float)(9*Math.PI/8));

            document.save(new File(RESULT_FOLDER, "rotatedImagesByBoundingBox.pdf"));
        }
    }

    /**
     * @see #testPlaceByBoundingBox()
     */
    void placeImage(PDDocument document, PDPage page, PDImageXObject image, float bbLowerLeftX, float bbLowerLeftY, float width, float height, float angle) throws IOException {
        try (   PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, true)   ) {
            float bbWidth = (float)(Math.abs(Math.sin(angle))*height + Math.abs(Math.cos(angle))*width);
            float bbHeight = (float)(Math.abs(Math.sin(angle))*width + Math.abs(Math.cos(angle))*height);
            contentStream.transform(Matrix.getTranslateInstance((bbLowerLeftX + .5f*bbWidth), (bbLowerLeftY + .5f*bbHeight)));
            contentStream.transform(Matrix.getRotateInstance(angle, 0, 0));
            contentStream.drawImage(image, -.5f*width, -.5f*height, width, height);
        }
    }
}
