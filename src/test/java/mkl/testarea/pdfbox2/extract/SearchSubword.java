// $Id$
package mkl.testarea.pdfbox2.extract;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.Test;

/**
 * @author mkl
 */
public class SearchSubword
{
    /**
     * <a href="http://stackoverflow.com/questions/35937774/how-to-search-some-specific-string-or-a-word-and-there-coordinates-from-a-pdf-do">
     * How to search some specific string or a word and there coordinates from a pdf document in java
     * </a>
     * <br/>
     * Variables.pdf
     * <p>
     * This test demonstrates how one can search for text parts with positions in a PDF.
     * This method actually is quite crude as it expects the whole search term to be
     * forwarded to {@link PDFTextStripper#writeString(String, List<TextPosition>)}
     * in the same call. This is why the methods here are called
     * {@link #printSubwords(PDDocument, String)} and
     * {@link #findSubwords(PDDocument, int, String)} as internally PDFBox calls
     * the portions forwarded here "words", cf. the calling method
     * {@link PDFTextStripper#writeLine(List<WordWithTextPositions>)}.
     * </p>
     */
    @Test
    public void testVariables() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("Variables.pdf");
                PDDocument document = PDDocument.load(resource);    )
        {
            System.out.println("\nVariables.pdf\n-------------\n");
            printSubwords(document, "${var1}");
            printSubwords(document, "${var 2}");
        }
    }
    
    void printSubwords(PDDocument document, String searchTerm) throws IOException
    {
        System.out.printf("* Looking for '%s'\n", searchTerm);
        for (int page = 1; page <= document.getNumberOfPages(); page++)
        {
            List<TextPositionSequence> hits = findSubwords(document, page, searchTerm);
            for (TextPositionSequence hit : hits)
            {
                if (!searchTerm.equals(hit.toString()))
                    System.out.printf("  Invalid (%s) ", hit.toString());
                TextPosition lastPosition = hit.textPositionAt(hit.length() - 1);
                System.out.printf("  Page %s at %s, %s with width %s and last letter '%s' at %s, %s\n",
                        page, hit.getX(), hit.getY(), hit.getWidth(),
                        lastPosition.getUnicode(), lastPosition.getXDirAdj(), lastPosition.getYDirAdj());
            }
        }
    }

    List<TextPositionSequence> findSubwords(PDDocument document, int page, String searchTerm) throws IOException
    {
        final List<TextPositionSequence> hits = new ArrayList<TextPositionSequence>();
        PDFTextStripper stripper = new PDFTextStripper()
        {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException
            {
                System.out.printf("  -- %s\n", text);

                TextPositionSequence word = new TextPositionSequence(textPositions);
                String string = word.toString();

                int fromIndex = 0;
                int index;
                while ((index = string.indexOf(searchTerm, fromIndex)) > -1)
                {
                    hits.add(word.subSequence(index, index + searchTerm.length()));
                    fromIndex = index + 1;
                }
                super.writeString(text, textPositions);
            }
        };
        
        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.getText(document);
        return hits;
    }
}
