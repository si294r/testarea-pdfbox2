package mkl.testarea.pdfbox2.merge;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;

import mkl.testarea.pdfbox2.extract.BoundingBoxFinder;

/**
 * <a href="https://stackoverflow.com/questions/54283827/pdf-files-merge-remove-blank-at-end-of-page-i-am-using-pdfbox-v2-0-13-to-achi">
 * PDF files merge : remove blank at end of page. I am using PDFBox v2.0.13 to achieve that
 * </a>
 * <p>
 * This class allows a dense merging of multiple input PDFs.
 * It is a port of the iText 5 class <code>mkl.testarea.itext5.merge.PdfDenseMergeTool</code>.
 * </p>
 * 
 * @author mkl
 */
public class PdfDenseMergeTool {
    public PdfDenseMergeTool(PDRectangle size, float top, float bottom, float gap)
    {
        this.pageSize = size;
        this.topMargin = top;
        this.bottomMargin = bottom;
        this.gap = gap;
    }

    public void merge(OutputStream outputStream, Iterable<PDDocument> inputs) throws IOException
    {
        try
        {
            openDocument();
            for (PDDocument input: inputs)
            {
                merge(input);
            }
            if (currentContents != null) {
                currentContents.close();
                currentContents = null;
            }
            document.save(outputStream);
        }
        finally
        {
            closeDocument();
        }
        
    }

    void openDocument() throws IOException
    {
        document = new PDDocument();
        newPage();
    }

    void closeDocument() throws IOException
    {
        try
        {
            if (currentContents != null) {
                currentContents.close();
                currentContents = null;
            }
            document.close();
        }
        finally
        {
            this.document = null;
            this.yPosition = 0;
        }
    }
    
    void newPage() throws IOException
    {
        if (currentContents != null) {
            currentContents.close();
            currentContents = null;
        }
        currentPage = new PDPage(pageSize);
        document.addPage(currentPage);
        yPosition = pageSize.getUpperRightY() - topMargin + gap;
        currentContents = new PDPageContentStream(document, currentPage);
    }

    void merge(PDDocument input) throws IOException
    {
        for (PDPage page : input.getPages())
        {
            merge(input, page);
        }
    }

    void merge(PDDocument sourceDoc, PDPage page) throws IOException
    {
        PDRectangle pageSizeToImport = page.getCropBox();
        BoundingBoxFinder boundingBoxFinder = new BoundingBoxFinder(page);
        boundingBoxFinder.processPage(page);
        Rectangle2D boundingBoxToImport = boundingBoxFinder.getBoundingBox();
        double heightToImport = boundingBoxToImport.getHeight();
        float maxHeight = pageSize.getHeight() - topMargin - bottomMargin;
        if (heightToImport > maxHeight)
        {
            throw new IllegalArgumentException(String.format("Page %s content too large; height: %s, limit: %s.", page, heightToImport, maxHeight));
        }

        if (gap + heightToImport > yPosition - (pageSize.getLowerLeftY() + bottomMargin))
        {
            newPage();
        }
        yPosition -= heightToImport + gap;

        LayerUtility layerUtility = new LayerUtility(document);
        PDFormXObject form = layerUtility.importPageAsForm(sourceDoc, page);

        currentContents.saveGraphicsState();
        Matrix matrix = Matrix.getTranslateInstance(0, (float)(yPosition - (boundingBoxToImport.getMinY() - pageSizeToImport.getLowerLeftY())));
        currentContents.transform(matrix);
        currentContents.drawForm(form);
        currentContents.restoreGraphicsState();
    }

    PDDocument document = null;
    PDPage currentPage = null;
    PDPageContentStream currentContents = null;
    float yPosition = 0; 

    final PDRectangle pageSize;
    final float topMargin;
    final float bottomMargin;
    final float gap;
}
