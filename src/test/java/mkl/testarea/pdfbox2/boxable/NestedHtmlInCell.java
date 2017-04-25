package mkl.testarea.pdfbox2.boxable;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.BeforeClass;
import org.junit.Test;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Row;

/**
 * @author mkl
 */
public class NestedHtmlInCell
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "boxable");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/43447829/apache-pdfbox-boxable-html-ordered-unordered-list-is-incorrectly-displayed">
     * Apache PDFBox, Boxable - HTML ordered/unordered list is incorrectly displayed
     * </a>
     * <p>
     * Using the 1.5-RC artifact like the OP did, one can indeed observe the incorrect nesting.
     * </p>
     * <p>
     * Compiling and using the current (2017-04-25) master branch (which contains numerous changes
     * added since the RC has been released but is still versioned 1.4!), though, the nested list
     * is properly displayed.
     * </p>
     */
    @Test
    public void testNestedHtmlListsInCell() throws IOException
    {
        PDDocument document = new PDDocument();
        PDPage myPage  = new PDPage(PDRectangle.A4);
        document.addPage(myPage);
        float yPosition = 777;
        float margin = 40;
        float bottomMargin = 40;
        float yStartNewPage = myPage.getMediaBox().getHeight() - (margin);
        float tableWidth = myPage.getMediaBox().getWidth() - (2 * margin);
        BaseTable table = new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin, document, myPage, true, true);
        Row<PDPage> row = table.createRow(10f);
        /*Cell cell = */row.createCell((100 / 3f),"<ul><li>hello</li><li>hello 2</li><ol><li>nested</li><li>nested 2</li></ol></ul>", be.quodlibet.boxable.HorizontalAlignment.get("left"), be.quodlibet.boxable.VerticalAlignment.get("top"));
//        /*Cell cell = */row.createCell((100 / 3f),"<ul><li>hello</li><li>hello 2<ol><li>nested</li><li>nested 2</li></ol></li></ul>", be.quodlibet.boxable.HorizontalAlignment.get("left"), be.quodlibet.boxable.VerticalAlignment.get("top"));
//        /*Cell cell = */row.createCell((100 / 3f),"<ul><li>hello</li><li>hello 2<ul><li>nested</li><li>nested 2</li></ul></li></ul>", be.quodlibet.boxable.HorizontalAlignment.get("left"), be.quodlibet.boxable.VerticalAlignment.get("top"));
//        /*Cell cell = */row.createCell((100 / 3f),"not bold <i>italic</i> <b>once bold <b>twice bold</b> once bold again <i>italic</i> </b> not bold", be.quodlibet.boxable.HorizontalAlignment.get("left"), be.quodlibet.boxable.VerticalAlignment.get("top"));
        table.draw();
        document.save(new File(RESULT_FOLDER, "NestedHtmlListsInCell.pdf"));
        document.close();
    }

}
