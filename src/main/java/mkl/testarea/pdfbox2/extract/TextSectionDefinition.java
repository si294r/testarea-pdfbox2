package mkl.testarea.pdfbox2.extract;

import java.util.List;
import java.util.function.Predicate;

import org.apache.pdfbox.text.TextPosition;

/**
 * <a href="http://stackoverflow.com/questions/41654518/how-to-read-pdf-departmentsheader-abstract-refrences-with-pdfbox">
 * How to read PDF departments(header,abstract,refrences) With PDFBox?
 * </a>
 * <br/>
 * <a href="http://merlot.usc.edu/csac-f06/papers/Wang05a.pdf">
 * Wang05a.pdf
 * </a>
 * <p>
 * This class defines a section for extraction of semantic text sections which are recognizable
 * by their characteristics of each line alone, e.g. the sample document supplied by the OP.
 * </p>
 * 
 * @author mkl
 */
public class TextSectionDefinition
{
    public enum MultiLine
    {
        singleLine,         // A single line without text body, e.g. title
        multiLine,          // Multiple lines, all match predicate, e.g. emails  
        multiLineHeader,    // Multiple lines, first line matches as header, e.g. h1
        multiLineIntro      // Multiple lines, first line matches inline, e.g. abstract
    }

    public TextSectionDefinition(String name, Predicate<List<List<TextPosition>>> matchPredicate, MultiLine multiLine, boolean multiple)
    {
        this.name = name;
        this.matchPredicate = matchPredicate;
        this.multiLine = multiLine;
        this.multiple = multiple;
    }

    final String name;
    final Predicate<List<List<TextPosition>>> matchPredicate;
    final MultiLine multiLine;
    final boolean multiple;
}
