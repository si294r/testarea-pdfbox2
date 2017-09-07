package mkl.testarea.pdfbox2.content;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class RectanglesOverText {
    final static File RESULT_FOLDER = new File("target/test-outputs", "content");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/46080131/text-coordinates-when-stripping-from-pdfbox">
     * Text coordinates when stripping from PDFBox
     * </a>
     * <p>
     * This text applies the OP's code to an arbitrary PDF file and it did work properly
     * (well, it did only cover the text from the baseline upwards but that is to be expected).
     * </p>
     */
    @Test
    public void testCoverTextByRectangles() throws IOException {
        try (   InputStream resource = getClass().getResourceAsStream("input.pdf")  ) {
            PDDocument doc = PDDocument.load(resource);

            myStripper stripper = new myStripper();

            stripper.setStartPage(1); // fix it to first page just to test it
            stripper.setEndPage(1);
            stripper.getText(doc);

            TextLine line = stripper.lines.get(1); // the line i want to paint on

            float minx = -1;
            float maxx = -1;

            for (TextPosition pos: line.textPositions)
            {
                if (pos == null)
                    continue;

                if (minx == -1 || pos.getTextMatrix().getTranslateX() < minx) {
                    minx = pos.getTextMatrix().getTranslateX();
                }
                if (maxx == -1 || pos.getTextMatrix().getTranslateX() > maxx) {
                    maxx = pos.getTextMatrix().getTranslateX();
                }
            }

            TextPosition firstPosition = line.textPositions.get(0);
            TextPosition lastPosition = line.textPositions.get(line.textPositions.size() - 1);

            float x = minx;
            float y = firstPosition.getTextMatrix().getTranslateY();
            float w = (maxx - minx) + lastPosition.getWidth();
            float h = lastPosition.getHeightDir();

            PDPageContentStream contentStream = new PDPageContentStream(doc, doc.getPage(0), PDPageContentStream.AppendMode.APPEND, false);

            contentStream.setNonStrokingColor(Color.RED);
            contentStream.addRect(x, y, w, h);
            contentStream.fill();
            contentStream.close();

            File fileout = new File(RESULT_FOLDER, "input-withRectangles.pdf");
            doc.save(fileout);
            doc.close();
        }
    }

}

/**
 * @see RectanglesOverText#testCoverTextByRectangles()
 * @author samue
 */
class TextLine {
    public List<TextPosition> textPositions = null;
    public String text = "";
}

/**
 * @see RectanglesOverText#testCoverTextByRectangles()
 * @author samue
 */
class myStripper extends PDFTextStripper {
    public myStripper() throws IOException {
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        startOfLine = true;
        super.startPage(page);
    }

    @Override
    protected void writeLineSeparator() throws IOException {
        startOfLine = true;
        super.writeLineSeparator();
    }

    @Override
    public String getText(PDDocument doc) throws IOException {
        lines = new ArrayList<TextLine>();
        return super.getText(doc);
    }

    @Override
    protected void writeWordSeparator() throws IOException {
        TextLine tmpline = null;

        tmpline = lines.get(lines.size() - 1);
        tmpline.text += getWordSeparator();

        super.writeWordSeparator();
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        TextLine tmpline = null;

        if (startOfLine) {
            tmpline = new TextLine();
            tmpline.text = text;
            tmpline.textPositions = textPositions;
            lines.add(tmpline);
        } else {
            tmpline = lines.get(lines.size() - 1);
            tmpline.text += text;
            tmpline.textPositions.addAll(textPositions);
        }

        if (startOfLine) {
            startOfLine = false;
        }
        super.writeString(text, textPositions);
    }

    boolean startOfLine = true;
    public ArrayList<TextLine> lines = null;
}